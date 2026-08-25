package com.linkit.company.domain.usecase

import com.linkit.company.domain.model.auth.Auth
import com.linkit.company.domain.model.common.CursorPage
import com.linkit.company.domain.model.tripplan.TripPlanDetail
import com.linkit.company.domain.model.tripplan.TripPlanItemOrder
import com.linkit.company.domain.model.tripplan.TripPlanSummary
import com.linkit.company.domain.model.video.DiscoverChannel
import com.linkit.company.domain.model.video.DiscoverVideo
import com.linkit.company.domain.model.video.VideoAnalysis
import com.linkit.company.domain.model.video.VideoAnalysisStatus
import com.linkit.company.domain.model.video.YouTubeVideoMetadata
import com.linkit.company.domain.repository.AuthRepository
import com.linkit.company.domain.repository.TripPlanRepository
import com.linkit.company.domain.repository.VideoRepository
import com.linkit.company.domain.runImmediateSuspend
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class StartVideoScheduleCreationUseCaseTest {

    @Test
    fun rejectsUnsupportedUrlBeforeAccessingRepositories() = runImmediateSuspend {
        val tripPlanRepository = VideoTripPlanRepositoryFake(emptyMap())
        val videoRepository = VideoRepositoryFake()
        val authRepository = VideoAuthRepositoryFake()
        val useCase = createUseCase(authRepository, tripPlanRepository, videoRepository)

        val result = useCase("https://example.com/video")

        assertIs<StartVideoScheduleCreationResult.InvalidFormat>(result)
        assertEquals(0, authRepository.loginCallCount)
        assertEquals(emptyList(), tripPlanRepository.requestedCursors)
        assertEquals(emptyList(), videoRepository.analyzedUrls)
    }

    @Test
    fun findsSameYoutubeVideoAcrossEverySavedSchedulePage() = runImmediateSuspend {
        val tripPlanRepository = VideoTripPlanRepositoryFake(
            pages = mapOf(
                null to CursorPage(
                    items = listOf(summary("first", "https://youtu.be/another")),
                    nextCursor = "next",
                    hasNext = true,
                ),
                "next" to CursorPage(
                    items = listOf(summary("existing", "https://youtu.be/AbC_123?t=10")),
                    nextCursor = null,
                    hasNext = false,
                ),
            ),
        )
        val videoRepository = VideoRepositoryFake()
        val useCase = createUseCase(
            authRepository = VideoAuthRepositoryFake(isLoggedIn = false),
            tripPlanRepository = tripPlanRepository,
            videoRepository = videoRepository,
        )

        val result = useCase("https://www.youtube.com/watch?feature=share&v=AbC_123")

        assertEquals(
            StartVideoScheduleCreationResult.ExistingSchedule(
                tripPlanId = "existing",
                title = "Title existing",
            ),
            result,
        )
        assertEquals(listOf(null, "next"), tripPlanRepository.requestedCursors)
        assertEquals(emptyList(), videoRepository.analyzedUrls)
    }

    @Test
    fun startsAnalysisWhenThereIsNoSavedScheduleForVideo() = runImmediateSuspend {
        val tripPlanRepository = VideoTripPlanRepositoryFake(
            pages = mapOf(
                null to CursorPage(
                    items = listOf(summary("other", "https://youtube.com/shorts/other")),
                    nextCursor = null,
                    hasNext = false,
                ),
            ),
        )
        val videoRepository = VideoRepositoryFake()
        val useCase = createUseCase(
            authRepository = VideoAuthRepositoryFake(),
            tripPlanRepository = tripPlanRepository,
            videoRepository = videoRepository,
        )

        val result = useCase(" https://youtube.com/shorts/new-video ")

        assertEquals(
            YouTubeVideoMetadata(
                title = "영상 제목",
                thumbnailUrl = "https://i.ytimg.com/vi/video/hqdefault.jpg",
            ),
            assertIs<StartVideoScheduleCreationResult.AnalysisStarted>(result).metadata,
        )
        assertEquals(listOf("https://youtube.com/shorts/new-video"), videoRepository.analyzedUrls)
        assertEquals(listOf("https://youtube.com/shorts/new-video"), videoRepository.metadataUrls)
    }

    @Test
    fun skipsDuplicateLookupWhenUserConfirmsCreatingAnotherSchedule() = runImmediateSuspend {
        val tripPlanRepository = VideoTripPlanRepositoryFake(emptyMap())
        val videoRepository = VideoRepositoryFake()
        val useCase = createUseCase(
            authRepository = VideoAuthRepositoryFake(),
            tripPlanRepository = tripPlanRepository,
            videoRepository = videoRepository,
        )

        val result = useCase(
            youtubeUrl = "https://youtu.be/same-video",
            allowDuplicate = true,
        )

        assertIs<StartVideoScheduleCreationResult.AnalysisStarted>(result)
        assertEquals(emptyList(), tripPlanRepository.requestedCursors)
        assertEquals(listOf("https://youtu.be/same-video"), videoRepository.analyzedUrls)
    }

    @Test
    fun startsAnalysisWithoutMetadataWhenYouTubeLookupFails() = runImmediateSuspend {
        val videoRepository = VideoRepositoryFake(metadataError = IllegalStateException("unavailable"))
        val useCase = createUseCase(
            authRepository = VideoAuthRepositoryFake(),
            tripPlanRepository = VideoTripPlanRepositoryFake(emptyMap()),
            videoRepository = videoRepository,
        )

        val result = useCase(
            youtubeUrl = "https://youtu.be/new-video",
            allowDuplicate = true,
        )

        assertEquals(
            null,
            assertIs<StartVideoScheduleCreationResult.AnalysisStarted>(result).metadata,
        )
    }

    private fun createUseCase(
        authRepository: AuthRepository,
        tripPlanRepository: TripPlanRepository,
        videoRepository: VideoRepository,
    ) = StartVideoScheduleCreationUseCase(
        ensureAuthenticated = EnsureAuthenticatedUseCase(authRepository),
        tripPlanRepository = tripPlanRepository,
        videoRepository = videoRepository,
    )
}

private class VideoAuthRepositoryFake(
    private var isLoggedIn: Boolean = true,
) : AuthRepository {
    var loginCallCount = 0

    override suspend fun login(): Auth {
        loginCallCount += 1
        isLoggedIn = true
        return Auth(memberId = "member", accessToken = "token")
    }

    override suspend fun isLoggedIn(): Boolean = isLoggedIn

    override suspend fun logout() {
        isLoggedIn = false
    }
}

private class VideoTripPlanRepositoryFake(
    private val pages: Map<String?, CursorPage<TripPlanSummary>>,
) : TripPlanRepository {
    val requestedCursors = mutableListOf<String?>()

    override suspend fun getTripPlans(cursor: String?): CursorPage<TripPlanSummary> {
        requestedCursors += cursor
        return checkNotNull(pages[cursor]) { "No page fixture for cursor=$cursor" }
    }

    override suspend fun getTripPlan(tripPlanId: String): TripPlanDetail =
        error("Not used in this test")

    override suspend fun updateTripPlan(
        tripPlanId: String,
        title: String?,
        items: List<TripPlanItemOrder>?,
    ): TripPlanDetail = error("Not used in this test")

    override suspend fun deleteTripPlan(tripPlanId: String) = error("Not used in this test")
}

private class VideoRepositoryFake(
    private val metadataError: Throwable? = null,
) : VideoRepository {
    val analyzedUrls = mutableListOf<String>()
    val metadataUrls = mutableListOf<String>()

    override suspend fun analyzeVideo(youtubeUrl: String): VideoAnalysis {
        analyzedUrls += youtubeUrl
        return VideoAnalysis(
            id = "analysis-id",
            youtubeUrl = youtubeUrl,
            isValid = true,
            status = VideoAnalysisStatus.PENDING,
            summary = "",
            estimatedMinCost = null,
            estimatedMaxCost = null,
            costBasis = null,
            placeEnrichmentCompleted = false,
            timelines = emptyList(),
            itineraryItems = emptyList(),
        )
    }

    override suspend fun getVideoAnalysis(videoAnalysisTaskId: String): VideoAnalysis =
        error("Not used in this test")

    override suspend fun getYouTubeVideoMetadata(youtubeUrl: String): YouTubeVideoMetadata {
        metadataUrls += youtubeUrl
        metadataError?.let { throw it }
        return YouTubeVideoMetadata(
            title = "영상 제목",
            thumbnailUrl = "https://i.ytimg.com/vi/video/hqdefault.jpg",
        )
    }

    override suspend fun getDiscoverVideosByTheme(
        theme: String,
        cursor: String?,
    ): CursorPage<DiscoverVideo> = error("Not used in this test")

    override suspend fun getDiscoverChannels(): List<DiscoverChannel> =
        error("Not used in this test")

    override suspend fun getDiscoverVideosByCountry(country: String): List<DiscoverVideo> =
        error("Not used in this test")

    override suspend fun getDiscoverVideosByRegion(region: String): List<DiscoverVideo> =
        error("Not used in this test")
}

private fun summary(id: String, youtubeUrl: String) = TripPlanSummary(
    id = id,
    title = "Title $id",
    videoAnalysisTaskId = "task-$id",
    youtubeUrl = youtubeUrl,
    itemCount = 1,
    nights = 1,
    days = 2,
    hashtags = emptyList(),
    createdAt = "2026-07-25T00:00:00Z",
    updatedAt = "2026-07-25T00:00:00Z",
)

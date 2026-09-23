package com.linkit.company.domain.usecase

import com.linkit.company.domain.exception.LinkTripApiException
import com.linkit.company.domain.exception.LinkTripErrorCode
import com.linkit.company.domain.fake.RecordingAuthRepository
import com.linkit.company.domain.fake.ScriptedTripPlanRepository
import com.linkit.company.domain.fake.tripPlanSummary
import com.linkit.company.domain.model.common.CursorPage
import com.linkit.company.domain.model.video.DiscoverChannel
import com.linkit.company.domain.model.video.DiscoverVideo
import com.linkit.company.domain.model.video.VideoAnalysis
import com.linkit.company.domain.model.video.VideoAnalysisStatus
import com.linkit.company.domain.model.video.YouTubeVideoMetadata
import com.linkit.company.domain.repository.VideoRepository
import com.linkit.company.domain.runImmediateSuspend
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

class CreateOnboardingScheduleUseCaseTest {

    private val recommended = listOf(
        "https://www.youtube.com/watch?v=rec1",
        "https://youtu.be/rec2",
    )

    @Test
    fun rejectsMalformedLinkBeforeTouchingRepositories() = runImmediateSuspend {
        val auth = RecordingAuthRepository()
        val video = ScriptedVideoRepository()
        val tripPlans = ScriptedTripPlanRepository()

        val result = createUseCase(auth, video, tripPlans)("https://example.com/rec1", recommended)

        assertIs<CreateOnboardingScheduleResult.InvalidFormat>(result)
        assertEquals(emptyList(), auth.events)
        assertEquals(emptyList(), video.analyzedUrls)
        assertEquals(emptyList(), tripPlans.requestedCursors)
    }

    @Test
    fun rejectsValidLinkThatIsNotRecommended() = runImmediateSuspend {
        val video = ScriptedVideoRepository()

        val result = createUseCase(RecordingAuthRepository(), video, ScriptedTripPlanRepository())(
            "https://youtu.be/other",
            recommended,
        )

        assertIs<CreateOnboardingScheduleResult.NotRecommended>(result)
        assertEquals(emptyList(), video.analyzedUrls)
    }

    @Test
    fun acceptsShortUrlAndExtraParametersAsSameRecommendedVideo() = runImmediateSuspend {
        val tripPlans = ScriptedTripPlanRepository(
            pages = mapOf(null to page(tripPlanSummary("plan-1", videoAnalysisTaskId = "analysis-rec1"))),
        )
        val video = ScriptedVideoRepository(analyzeResults = listOf(completed("analysis-rec1")))

        val result = createUseCase(RecordingAuthRepository(), video, tripPlans)(
            " https://youtu.be/rec1?t=30&feature=share ",
            recommended,
        )

        assertEquals(CreateOnboardingScheduleResult.Created("plan-1", "Title plan-1"), result)
        assertEquals(listOf("https://youtu.be/rec1?t=30&feature=share"), video.analyzedUrls)
        assertEquals(listOf("markUnchecked(plan-1)"), tripPlans.events)
    }

    @Test
    fun reportsNotReadyWhenAnalysisIsStillPending() = runImmediateSuspend {
        val tripPlans = ScriptedTripPlanRepository()
        val video = ScriptedVideoRepository(
            analyzeResults = listOf(completed("analysis-rec1").copy(status = VideoAnalysisStatus.PENDING)),
        )

        val result = createUseCase(RecordingAuthRepository(), video, tripPlans)(recommended[0], recommended)

        assertIs<CreateOnboardingScheduleResult.NotReady>(result)
        assertEquals(emptyList(), tripPlans.requestedCursors)
        assertEquals(emptyList(), tripPlans.events)
    }

    @Test
    fun matchesGeneratedTripPlanAcrossPages() = runImmediateSuspend {
        val tripPlans = ScriptedTripPlanRepository(
            pages = mapOf(
                null to CursorPage(
                    items = listOf(tripPlanSummary("plan-0", videoAnalysisTaskId = "other")),
                    nextCursor = "next",
                    hasNext = true,
                ),
                "next" to page(tripPlanSummary("plan-1", videoAnalysisTaskId = "analysis-rec1")),
            ),
        )
        val video = ScriptedVideoRepository(analyzeResults = listOf(completed("analysis-rec1")))

        val result = createUseCase(RecordingAuthRepository(), video, tripPlans)(recommended[0], recommended)

        assertEquals(CreateOnboardingScheduleResult.Created("plan-1", "Title plan-1"), result)
        assertEquals(listOf(null, "next"), tripPlans.requestedCursors)
        assertEquals(emptyList(), video.fetchedAnalysisIds)
    }

    @Test
    fun refetchesAnalysisOnceThenFindsTripPlan() = runImmediateSuspend {
        val tripPlans = ScriptedTripPlanRepository(pages = mapOf(null to page()))
        val video = ScriptedVideoRepository(
            analyzeResults = listOf(completed("analysis-rec1")),
            onGetAnalysis = { tripPlans.pageOverrides = mapOf(null to page(tripPlanSummary("plan-1", "analysis-rec1"))) },
        )

        val result = createUseCase(RecordingAuthRepository(), video, tripPlans)(recommended[0], recommended)

        assertEquals(CreateOnboardingScheduleResult.Created("plan-1", "Title plan-1"), result)
        assertEquals(listOf("analysis-rec1"), video.fetchedAnalysisIds)
        assertEquals(listOf<String?>(null, null), tripPlans.requestedCursors)
        assertEquals(listOf("markUnchecked(plan-1)"), tripPlans.events)
    }

    @Test
    fun reportsNotReadyWhenTripPlanIsStillMissingAfterRefetch() = runImmediateSuspend {
        val tripPlans = ScriptedTripPlanRepository(pages = mapOf(null to page()))
        val video = ScriptedVideoRepository(analyzeResults = listOf(completed("analysis-rec1")))

        val result = createUseCase(RecordingAuthRepository(), video, tripPlans)(recommended[0], recommended)

        assertIs<CreateOnboardingScheduleResult.NotReady>(result)
        assertEquals(listOf("analysis-rec1"), video.fetchedAnalysisIds)
        assertEquals(emptyList(), tripPlans.events)
    }

    @Test
    fun retriesOnceWithRefreshedTokenAfterUnauthorized() = runImmediateSuspend {
        val auth = RecordingAuthRepository()
        val tripPlans = ScriptedTripPlanRepository(
            pages = mapOf(null to page(tripPlanSummary("plan-1", "analysis-rec1"))),
        )
        val video = ScriptedVideoRepository(
            analyzeResults = listOf(unauthorized(), completed("analysis-rec1")),
        )

        val result = createUseCase(auth, video, tripPlans)(recommended[0], recommended)

        assertEquals(CreateOnboardingScheduleResult.Created("plan-1", "Title plan-1"), result)
        assertEquals(2, video.analyzedUrls.size)
        assertEquals(listOf("logout", "login"), auth.events)
    }

    @Test
    fun propagatesOtherApiErrors() = runImmediateSuspend {
        val video = ScriptedVideoRepository(
            analyzeResults = listOf(
                LinkTripApiException(LinkTripErrorCode.DUPLICATE_REQUEST, 429, "too many"),
            ),
        )

        val error = assertFailsWith<LinkTripApiException> {
            createUseCase(RecordingAuthRepository(), video, ScriptedTripPlanRepository())(recommended[0], recommended)
        }

        assertEquals(429, error.httpStatus)
    }

    private fun createUseCase(
        auth: RecordingAuthRepository,
        video: VideoRepository,
        tripPlans: ScriptedTripPlanRepository,
    ) = CreateOnboardingScheduleUseCase(
        ensureAuthenticated = EnsureAuthenticatedUseCase(auth),
        videoRepository = video,
        tripPlanRepository = tripPlans,
    )

    private fun page(vararg items: com.linkit.company.domain.model.tripplan.TripPlanSummary) =
        CursorPage(items = items.toList(), nextCursor = null, hasNext = false)

    private fun completed(id: String) = VideoAnalysis(
        id = id,
        youtubeUrl = "https://youtu.be/rec1",
        isValid = true,
        status = VideoAnalysisStatus.COMPLETED,
        summary = "",
        estimatedMinCost = null,
        estimatedMaxCost = null,
        costBasis = null,
        placeEnrichmentCompleted = true,
        timelines = emptyList(),
        itineraryItems = emptyList(),
    )

    private fun unauthorized() =
        LinkTripApiException(LinkTripErrorCode.UNAUTHORIZED_TOKEN_EXPIRED, 401, "expired")
}

/** [analyzeResults] 큐: [VideoAnalysis]면 반환, [Throwable]이면 던진다. */
private class ScriptedVideoRepository(
    analyzeResults: List<Any> = emptyList(),
    private val onGetAnalysis: () -> Unit = {},
) : VideoRepository {
    val analyzedUrls = mutableListOf<String>()
    val fetchedAnalysisIds = mutableListOf<String>()
    private val analyzeQueue = ArrayDeque(analyzeResults)
    private var lastAnalysis: VideoAnalysis? = null

    override suspend fun analyzeVideo(youtubeUrl: String): VideoAnalysis {
        analyzedUrls += youtubeUrl
        val result = checkNotNull(analyzeQueue.removeFirstOrNull()) { "No analyze fixture left" }
        if (result is Throwable) throw result
        return (result as VideoAnalysis).also { lastAnalysis = it }
    }

    override suspend fun getVideoAnalysis(videoAnalysisTaskId: String): VideoAnalysis {
        fetchedAnalysisIds += videoAnalysisTaskId
        onGetAnalysis()
        return checkNotNull(lastAnalysis)
    }

    override suspend fun getYouTubeVideoMetadata(youtubeUrl: String): YouTubeVideoMetadata =
        error("Not used in this test")

    override suspend fun getDiscoverVideosByTheme(theme: String, cursor: String?): CursorPage<DiscoverVideo> =
        error("Not used in this test")

    override suspend fun getDiscoverChannels(): List<DiscoverChannel> = error("Not used in this test")

    override suspend fun getDiscoverVideosByCountry(country: String): List<DiscoverVideo> =
        error("Not used in this test")

    override suspend fun getDiscoverVideosByRegion(region: String): List<DiscoverVideo> =
        error("Not used in this test")

    override suspend fun getOnboardingVideos(): List<DiscoverVideo> = error("Not used in this test")
}

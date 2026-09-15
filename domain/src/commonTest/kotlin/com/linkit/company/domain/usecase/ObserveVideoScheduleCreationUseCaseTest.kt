package com.linkit.company.domain.usecase

import com.linkit.company.domain.exception.LinkTripApiException
import com.linkit.company.domain.exception.LinkTripErrorCode
import com.linkit.company.domain.model.auth.Auth
import com.linkit.company.domain.model.common.CursorPage
import com.linkit.company.domain.model.tripplan.TripPlanDetail
import com.linkit.company.domain.model.tripplan.TripPlanItemOrder
import com.linkit.company.domain.model.tripplan.TripPlanSummary
import com.linkit.company.domain.model.video.DiscoverChannel
import com.linkit.company.domain.model.video.DiscoverVideo
import com.linkit.company.domain.model.video.VideoAnalysis
import com.linkit.company.domain.model.video.VideoAnalysisStatus
import com.linkit.company.domain.model.video.VideoScheduleCreationState
import com.linkit.company.domain.model.video.YouTubeVideoMetadata
import com.linkit.company.domain.repository.AuthRepository
import com.linkit.company.domain.repository.TripPlanRepository
import com.linkit.company.domain.repository.VideoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ObserveVideoScheduleCreationUseCaseTest {
    @Test
    fun resumesSavedProcessingTaskAndWaitsForMatchingPlanAcrossPages() = runTrackingTest {
        val video = TrackingVideoRepositoryFake().apply {
            states += analysis(VideoAnalysisStatus.PROCESSING)
            states += analysis(VideoAnalysisStatus.COMPLETED)
        }
        val trips = TrackingTripPlanRepositoryFake { cursor, call ->
            when {
                call == 1 -> page(listOf(trip("unrelated", "other-task")))
                cursor == null -> page(listOf(trip("unrelated", "other-task")), "second")
                else -> page(listOf(trip("created", "task")))
            }
        }

        val states = observer(video, trips)(pollIntervalMillis = 0).take(2).toList()

        assertEquals(
            listOf(
                VideoScheduleCreationState.InProgress("task"),
                VideoScheduleCreationState.Completed("task", "created", "Title created"),
            ),
            states,
        )
        assertEquals(3, video.analysisCalls)
        assertEquals(listOf(null, null, "second"), trips.cursors)
        assertEquals("task", video.pending.value)
    }

    @Test
    fun transientFailureStopsAfterThreeAttemptsAndCanBeRetriedWithoutLosingTask() = runTrackingTest {
        val video = TrackingVideoRepositoryFake().apply { failure = IllegalStateException("offline") }
        val observe = observer(video)

        val failure = observe(pollIntervalMillis = 0).first { it is VideoScheduleCreationState.Error }

        assertEquals("task", assertIs<VideoScheduleCreationState.Error>(failure).taskId)
        assertEquals(3, video.analysisCalls)
        assertEquals("task", video.pending.value)

        video.failure = null
        video.states += analysis(VideoAnalysisStatus.COMPLETED)
        val completion = observe(pollIntervalMillis = 0).first { it is VideoScheduleCreationState.Completed }
        assertEquals("created", assertIs<VideoScheduleCreationState.Completed>(completion).tripPlanId)
    }

    @Test
    fun missingCreatedPlanReachesPollingLimitWithoutCompletingOrClearingTask() = runTrackingTest {
        val video = TrackingVideoRepositoryFake().apply { states += analysis(VideoAnalysisStatus.COMPLETED) }
        val trips = TrackingTripPlanRepositoryFake { _, _ -> page(listOf(trip("wrong", "another-task"))) }

        val result = observer(video, trips)(pollIntervalMillis = 0, maxPollAttempts = 2)
            .first { it is VideoScheduleCreationState.Error }

        assertIs<VideoScheduleCreationState.Error>(result)
        assertEquals(2, video.analysisCalls)
        assertEquals("task", video.pending.value)
    }

    @Test
    fun reusedAnalysisTaskWaitsForNewPlanInsteadOfOpeningExcludedSchedule() = runTrackingTest {
        val video = TrackingVideoRepositoryFake().apply {
            states += analysis(VideoAnalysisStatus.COMPLETED)
            excludedTripPlanIds = setOf("old")
        }
        val trips = TrackingTripPlanRepositoryFake { _, call ->
            if (call == 1) page(listOf(trip("old", "task")))
            else page(listOf(trip("old", "task"), trip("new", "task")))
        }

        val result = observer(video, trips)(0).first { it is VideoScheduleCreationState.Completed }

        assertEquals("new", assertIs<VideoScheduleCreationState.Completed>(result).tripPlanId)
        assertEquals(2, video.analysisCalls)
    }

    @Test
    fun terminalFailureStaysUntilAcknowledgedAndThenBecomesIdle() = runTrackingTest {
        val video = TrackingVideoRepositoryFake().apply { states += analysis(VideoAnalysisStatus.FAILED) }
        val observe = observer(video)

        assertIs<VideoScheduleCreationState.Failed>(observe(0).first { it is VideoScheduleCreationState.Failed })
        assertEquals("task", video.pending.value)

        AcknowledgeVideoScheduleCreationUseCase(video)("task")

        assertEquals(VideoScheduleCreationState.Idle, observe().first())
    }

    @Test
    fun expiredAuthenticationIsRefreshedOnceAndPollingContinues() = runTrackingTest {
        for (code in listOf(LinkTripErrorCode.UNAUTHORIZED_AUTHENTICATION_FAILED, LinkTripErrorCode.UNKNOWN)) {
            val auth = TrackingAuthRepositoryFake()
            val video = TrackingVideoRepositoryFake().apply {
                failure = LinkTripApiException(code, 401, "expired")
                failuresRemaining = 1
                states += analysis(VideoAnalysisStatus.COMPLETED)
            }

            val result = observer(video, auth = auth)(0).first { it is VideoScheduleCreationState.Completed }

            assertIs<VideoScheduleCreationState.Completed>(result)
            assertEquals(1, auth.loginCalls)
            assertEquals(1, auth.logoutCalls)
            assertEquals(2, video.analysisCalls)
        }
    }

    @Test
    fun repeatedUnknown401StopsWithoutRefreshingAgainOrLosingPendingTask() = runTrackingTest {
        val auth = TrackingAuthRepositoryFake()
        val video = TrackingVideoRepositoryFake().apply {
            failure = LinkTripApiException(LinkTripErrorCode.UNKNOWN, 401, "expired")
        }

        val result = observer(video, auth = auth)(0).first { it is VideoScheduleCreationState.Error }

        assertIs<VideoScheduleCreationState.Error>(result)
        assertEquals(1, auth.loginCalls)
        assertEquals(1, auth.logoutCalls)
        assertEquals(4, video.analysisCalls)
        assertEquals("task", video.pending.value)
    }

    @Test
    fun cancellingTheObserverStopsRequestsAndKeepsPendingTask() = runBlocking {
        withTimeout(5_000) {
            val requested = CompletableDeferred<Unit>()
            val observed = CompletableDeferred<Unit>()
            val video = TrackingVideoRepositoryFake().apply {
                onAnalysis = {
                    requested.complete(Unit)
                    awaitCancellation()
                }
            }
            val states = mutableListOf<VideoScheduleCreationState>()
            val job = launch {
                observer(video)(0).collect {
                    states += it
                    observed.complete(Unit)
                }
            }
            requested.await()
            observed.await()

            job.cancelAndJoin()

            assertEquals("task", video.pending.value)
            assertEquals(1, video.analysisCalls)
            assertEquals(listOf<VideoScheduleCreationState>(VideoScheduleCreationState.InProgress("task")), states)
        }
    }

    @Test
    fun localReadFailureIsExposedAsRetryableState() = runTrackingTest {
        val video = TrackingVideoRepositoryFake().apply { localFailure = IllegalStateException("storage") }

        val state = observer(video)().first()

        assertEquals(null, assertIs<VideoScheduleCreationState.Error>(state).taskId)
        assertEquals(0, video.analysisCalls)
    }

    private fun observer(
        video: VideoRepository,
        trips: TripPlanRepository = TrackingTripPlanRepositoryFake { _, _ -> page(listOf(trip("created", "task"))) },
        auth: AuthRepository = TrackingAuthRepositoryFake(),
    ) = ObserveVideoScheduleCreationUseCase(EnsureAuthenticatedUseCase(auth), video, trips)
}

private fun runTrackingTest(block: suspend () -> Unit) = runBlocking {
    withTimeout(5_000) { block() }
}

private class TrackingVideoRepositoryFake : VideoRepository {
    val pending = MutableStateFlow<String?>("task")
    val states = mutableListOf<VideoAnalysis>()
    var failure: Exception? = null
    var localFailure: Exception? = null
    var failuresRemaining = Int.MAX_VALUE
    var analysisCalls = 0
    var excludedTripPlanIds = emptySet<String>()
    var onAnalysis: suspend () -> Unit = {}

    override fun observePendingVideoAnalysisTaskId(): Flow<String?> =
        localFailure?.let { error -> flow { throw error } } ?: pending

    override suspend fun savePendingVideoAnalysisTaskId(taskId: String, excludedTripPlanIds: Set<String>) {
        pending.value = taskId
        this.excludedTripPlanIds = excludedTripPlanIds
    }
    override suspend fun getPendingVideoAnalysisExcludedTripPlanIds(): Set<String> = excludedTripPlanIds
    override suspend fun clearPendingVideoAnalysisTaskId(expectedTaskId: String) {
        pending.compareAndSet(expectedTaskId, null)
    }

    override suspend fun getVideoAnalysis(videoAnalysisTaskId: String): VideoAnalysis {
        analysisCalls += 1
        onAnalysis()
        failure?.takeIf { failuresRemaining-- > 0 }?.let { throw it }
        return when (states.size) {
            0 -> analysis(VideoAnalysisStatus.PENDING)
            1 -> states.single()
            else -> states.removeAt(0)
        }
    }

    override suspend fun analyzeVideo(youtubeUrl: String): VideoAnalysis = error("Unused")
    override suspend fun getYouTubeVideoMetadata(youtubeUrl: String): YouTubeVideoMetadata = error("Unused")
    override suspend fun getDiscoverVideosByTheme(theme: String, cursor: String?): CursorPage<DiscoverVideo> = error("Unused")
    override suspend fun getDiscoverChannels(): List<DiscoverChannel> = error("Unused")
    override suspend fun getDiscoverVideosByCountry(country: String): List<DiscoverVideo> = error("Unused")
    override suspend fun getDiscoverVideosByRegion(region: String): List<DiscoverVideo> = error("Unused")
}

private class TrackingTripPlanRepositoryFake(
    private val pages: (String?, Int) -> CursorPage<TripPlanSummary>,
) : TripPlanRepository {
    val cursors = mutableListOf<String?>()
    override suspend fun getTripPlans(cursor: String?): CursorPage<TripPlanSummary> {
        cursors += cursor
        return pages(cursor, cursors.size)
    }
    override suspend fun getTripPlan(tripPlanId: String): TripPlanDetail = error("Unused")
    override suspend fun updateTripPlan(tripPlanId: String, title: String?, items: List<TripPlanItemOrder>?): TripPlanDetail = error("Unused")
    override suspend fun deleteTripPlan(tripPlanId: String) = error("Unused")
}

private class TrackingAuthRepositoryFake : AuthRepository {
    var loginCalls = 0
    var logoutCalls = 0
    override suspend fun isLoggedIn(): Boolean = true
    override suspend fun login(): Auth {
        loginCalls += 1
        return Auth("member", "token")
    }
    override suspend fun logout() { logoutCalls += 1 }
}

private fun analysis(status: VideoAnalysisStatus) = VideoAnalysis(
    id = "task",
    youtubeUrl = "https://youtu.be/video",
    isValid = true,
    status = status,
    summary = "",
    estimatedMinCost = null,
    estimatedMaxCost = null,
    costBasis = null,
    placeEnrichmentCompleted = false,
    timelines = emptyList(),
    itineraryItems = emptyList(),
)

private fun page(items: List<TripPlanSummary>, next: String? = null) = CursorPage(items, next, next != null)

private fun trip(id: String, taskId: String) = TripPlanSummary(
    id = id,
    title = "Title $id",
    videoAnalysisTaskId = taskId,
    youtubeUrl = "https://youtu.be/video",
    itemCount = 1,
    nights = 1,
    days = 2,
    hashtags = emptyList(),
    createdAt = "2026-09-13T00:00:00Z",
    updatedAt = "2026-09-13T00:00:00Z",
)

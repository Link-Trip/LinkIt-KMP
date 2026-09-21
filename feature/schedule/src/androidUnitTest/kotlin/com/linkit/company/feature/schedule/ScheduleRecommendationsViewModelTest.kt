package com.linkit.company.feature.schedule

import android.os.Looper
import com.linkit.company.domain.model.auth.Auth
import com.linkit.company.domain.model.common.CursorPage
import com.linkit.company.domain.model.tripplan.TripPlanDetail
import com.linkit.company.domain.model.tripplan.TripPlanItemOrder
import com.linkit.company.domain.model.tripplan.TripPlanSummary
import com.linkit.company.domain.model.video.DiscoverChannel
import com.linkit.company.domain.model.video.DiscoverCountry
import com.linkit.company.domain.model.video.DiscoverVideo
import com.linkit.company.domain.model.video.VideoAnalysis
import com.linkit.company.domain.model.video.VideoAnalysisStatus
import com.linkit.company.domain.model.video.YouTubeVideoMetadata
import com.linkit.company.domain.repository.AuthRepository
import com.linkit.company.domain.repository.TripPlanRepository
import com.linkit.company.domain.repository.VideoRepository
import com.linkit.company.domain.usecase.DeleteTripPlanUseCase
import com.linkit.company.domain.usecase.EnsureAuthenticatedUseCase
import com.linkit.company.domain.usecase.GetExploreVideosUseCase
import com.linkit.company.domain.usecase.RenameTripPlanUseCase
import com.linkit.company.domain.usecase.StartVideoScheduleCreationUseCase
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ScheduleRecommendationsViewModelTest {
    @Test
    fun loadsServerRecommendationsWithoutReplacingManualInput() {
        val videos = RecommendationRepository()
        val viewModel = createViewModel(videos)
        viewModel.onIntent(ScheduleIntent.UpdateVideoLink("https://youtu.be/manual-first"))
        viewModel.onIntent(ScheduleIntent.LoadRecommendedVideos)
        shadowOf(Looper.getMainLooper()).idle()

        assertTrue(viewModel.uiState.value.isLoadingRecommendedVideos)
        assertTrue(viewModel.uiState.value.recommendedVideos.isEmpty())
        assertEquals(1, videos.requestCount)

        viewModel.onIntent(ScheduleIntent.UpdateVideoLink("https://youtu.be/manual-latest"))
        videos.response.complete(ScheduleRecommendationFixtures)
        shadowOf(Looper.getMainLooper()).idle()

        assertFalse(viewModel.uiState.value.isLoadingRecommendedVideos)
        assertEquals(ScheduleRecommendationFixtures, viewModel.uiState.value.recommendedVideos)
        assertNull(viewModel.uiState.value.recommendedVideosError)
        assertEquals("https://youtu.be/manual-latest", viewModel.uiState.value.videoLink)
        assertFalse(viewModel.uiState.value.isSubmittingVideoLink)
    }

    @Test
    fun retriesFailedRequestAndClearsErrorWithoutLosingManualInput() {
        val videos = RecommendationRepository()
        val viewModel = createViewModel(videos)
        viewModel.onIntent(ScheduleIntent.UpdateVideoLink("https://youtu.be/manual"))
        viewModel.onIntent(ScheduleIntent.LoadRecommendedVideos)
        shadowOf(Looper.getMainLooper()).idle()
        videos.response.completeExceptionally(IllegalStateException("Service unavailable"))
        shadowOf(Looper.getMainLooper()).idle()

        assertFalse(viewModel.uiState.value.isLoadingRecommendedVideos)
        assertNotNull(viewModel.uiState.value.recommendedVideosError)
        assertTrue(viewModel.uiState.value.recommendedVideos.isEmpty())
        assertEquals("https://youtu.be/manual", viewModel.uiState.value.videoLink)

        videos.response = CompletableDeferred()
        viewModel.onIntent(ScheduleIntent.LoadRecommendedVideos)
        shadowOf(Looper.getMainLooper()).idle()

        assertTrue(viewModel.uiState.value.isLoadingRecommendedVideos)
        assertNull(viewModel.uiState.value.recommendedVideosError)
        assertEquals(2, videos.requestCount)

        videos.response.complete(ScheduleRecommendationFixtures)
        shadowOf(Looper.getMainLooper()).idle()

        assertFalse(viewModel.uiState.value.isLoadingRecommendedVideos)
        assertNull(viewModel.uiState.value.recommendedVideosError)
        assertEquals(ScheduleRecommendationFixtures, viewModel.uiState.value.recommendedVideos)
        assertEquals("https://youtu.be/manual", viewModel.uiState.value.videoLink)
    }

    @Test
    fun ignoresDuplicateLoadsWhileRequestIsPendingAndExpandsWithoutRefetching() {
        val videos = RecommendationRepository()
        val viewModel = createViewModel(videos)
        repeat(3) { viewModel.onIntent(ScheduleIntent.LoadRecommendedVideos) }
        shadowOf(Looper.getMainLooper()).idle()
        viewModel.onIntent(ScheduleIntent.LoadRecommendedVideos)
        shadowOf(Looper.getMainLooper()).idle()

        assertEquals(1, videos.requestCount)
        assertTrue(viewModel.uiState.value.isLoadingRecommendedVideos)

        videos.response.complete(ScheduleRecommendationFixtures)
        shadowOf(Looper.getMainLooper()).idle()
        viewModel.onIntent(ScheduleIntent.ToggleRecommendedVideos)

        assertTrue(viewModel.uiState.value.areRecommendedVideosExpanded)
        assertEquals(ScheduleRecommendationFixtures, viewModel.uiState.value.recommendedVideos)
        viewModel.onIntent(ScheduleIntent.ToggleRecommendedVideos)
        assertFalse(viewModel.uiState.value.areRecommendedVideosExpanded)
        assertFalse(viewModel.uiState.value.isLoadingRecommendedVideos)
        assertEquals(1, videos.requestCount)
    }

    @Test
    fun changingLinkDuringSubmissionDoesNotCancelRequestOrLoseReturnedTaskId() {
        val videos = RecommendationRepository()
        val viewModel = createViewModel(videos)
        val submittedUrl = "https://youtu.be/submitted-video"
        viewModel.onIntent(ScheduleIntent.UpdateVideoLink(submittedUrl))
        viewModel.onIntent(ScheduleIntent.SubmitVideoLink)
        shadowOf(Looper.getMainLooper()).idle()

        assertTrue(viewModel.uiState.value.isSubmittingVideoLink)
        assertEquals(listOf(submittedUrl), videos.analysisUrls)
        viewModel.onIntent(ScheduleIntent.UpdateVideoLink(ScheduleRecommendationFixtures[0].videoUrl))
        viewModel.onIntent(ScheduleIntent.UpdateVideoLink("https://youtu.be/pasted-video"))
        viewModel.onIntent(ScheduleIntent.SubmitVideoLink)
        shadowOf(Looper.getMainLooper()).idle()

        assertEquals(submittedUrl, viewModel.uiState.value.videoLink)
        assertTrue(viewModel.uiState.value.isSubmittingVideoLink)
        assertFalse(viewModel.uiState.value.canCreate)
        assertFalse(videos.analysisCancelled)
        assertEquals(listOf(submittedUrl), videos.analysisUrls)
        videos.analysisResponse.complete(
            VideoAnalysis(
                id = "accepted-task",
                youtubeUrl = submittedUrl,
                isValid = true,
                status = VideoAnalysisStatus.PROCESSING,
                summary = "",
                estimatedMinCost = null,
                estimatedMaxCost = null,
                costBasis = null,
                placeEnrichmentCompleted = false,
                timelines = emptyList(),
                itineraryItems = emptyList(),
            ),
        )
        shadowOf(Looper.getMainLooper()).idle()

        assertFalse(videos.analysisCancelled)
        assertEquals("accepted-task", videos.pendingTaskId)
        assertFalse(viewModel.uiState.value.isSubmittingVideoLink)
        assertNull(viewModel.uiState.value.videoLinkError)
    }

    private fun createViewModel(videos: VideoRepository): ScheduleViewModel {
        val authentication = EnsureAuthenticatedUseCase(object : AuthRepository {
            override suspend fun login() = Auth("member", "token")
            override suspend fun isLoggedIn() = true
            override suspend fun logout() = Unit
        })
        val trips = object : TripPlanRepository {
            override suspend fun getTripPlans(cursor: String?) = CursorPage<TripPlanSummary>(emptyList(), null, false)
            override suspend fun getTripPlan(tripPlanId: String): TripPlanDetail = error("Unused")
            override suspend fun updateTripPlan(
                tripPlanId: String,
                title: String?,
                items: List<TripPlanItemOrder>?,
            ): TripPlanDetail = error("Unused")
            override suspend fun deleteTripPlan(tripPlanId: String): Unit = error("Unused")
        }
        return ScheduleViewModel(
            startVideoScheduleCreation = StartVideoScheduleCreationUseCase(authentication, trips, videos),
            renameTripPlan = RenameTripPlanUseCase(authentication, trips),
            deleteTripPlan = DeleteTripPlanUseCase(authentication, trips),
            getExploreVideos = GetExploreVideosUseCase(authentication, videos),
        )
    }

    private class RecommendationRepository : VideoRepository {
        var response = CompletableDeferred<List<DiscoverVideo>>()
        var requestCount = 0
        val analysisResponse = CompletableDeferred<VideoAnalysis>()
        val analysisUrls = mutableListOf<String>()
        var analysisCancelled = false
        var pendingTaskId: String? = null

        override suspend fun getDiscoverVideos(): List<DiscoverVideo> {
            requestCount++
            return response.await()
        }

        override suspend fun analyzeVideo(youtubeUrl: String): VideoAnalysis {
            analysisUrls += youtubeUrl
            return try {
                analysisResponse.await()
            } catch (error: CancellationException) {
                analysisCancelled = true
                throw error
            }
        }
        override suspend fun getVideoAnalysis(videoAnalysisTaskId: String): VideoAnalysis = error("Unused")
        override fun observePendingVideoAnalysisTaskId() = flowOf(pendingTaskId)
        override suspend fun savePendingVideoAnalysisTaskId(taskId: String, excludedTripPlanIds: Set<String>) {
            pendingTaskId = taskId
        }
        override suspend fun getPendingVideoAnalysisExcludedTripPlanIds(): Set<String> = error("Unused")
        override suspend fun clearPendingVideoAnalysisTaskId(expectedTaskId: String): Unit = error("Unused")
        override suspend fun getYouTubeVideoMetadata(youtubeUrl: String): YouTubeVideoMetadata = error("Unused")
        override suspend fun getDiscoverVideosByTheme(theme: String, cursor: String?): CursorPage<DiscoverVideo> = error("Unused")
        override suspend fun getDiscoverChannels(): List<DiscoverChannel> = error("Unused")
        override suspend fun getDiscoverCountries(): List<DiscoverCountry> = error("Unused")
        override suspend fun getDiscoverVideosByCountry(country: String): List<DiscoverVideo> = error("Unused")
        override suspend fun getDiscoverVideosByRegion(region: String): List<DiscoverVideo> = error("Unused")
    }
}

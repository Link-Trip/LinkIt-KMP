package com.linkit.company.domain.usecase

import com.linkit.company.domain.exception.LinkTripApiException
import com.linkit.company.domain.exception.LinkTripErrorCode
import com.linkit.company.domain.model.auth.Auth
import com.linkit.company.domain.model.common.CursorPage
import com.linkit.company.domain.model.place.PlaceCategory
import com.linkit.company.domain.model.tripplan.TripPlanDetail
import com.linkit.company.domain.model.tripplan.TripPlanItem
import com.linkit.company.domain.model.tripplan.TripPlanItemOrder
import com.linkit.company.domain.model.tripplan.TripPlanSummary
import com.linkit.company.domain.model.video.DiscoverChannel
import com.linkit.company.domain.model.video.DiscoverCountry
import com.linkit.company.domain.model.video.DiscoverVideo
import com.linkit.company.domain.model.video.VideoAnalysis
import com.linkit.company.domain.model.video.VideoAnalysisStatus
import com.linkit.company.domain.model.video.VideoTimeline
import com.linkit.company.domain.model.video.YouTubeVideoMetadata
import com.linkit.company.domain.repository.AuthRepository
import com.linkit.company.domain.repository.TripPlanRepository
import com.linkit.company.domain.repository.VideoRepository
import com.linkit.company.domain.runImmediateSuspend
import kotlin.coroutines.cancellation.CancellationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlinx.coroutines.flow.flowOf

class GetTripPlanContentUseCaseTest {
    private val trips = DetailTripsFake()
    private val videos = DetailVideosFake()
    private var refreshes = 0
    private val auth = object : AuthRepository {
        override suspend fun isLoggedIn() = true
        override suspend fun login(): Auth {
            refreshes++
            return Auth("member", "token")
        }
        override suspend fun logout() = Unit
    }
    private val useCase = GetTripPlanContentUseCase(EnsureAuthenticatedUseCase(auth), trips, videos)

    @Test
    fun joinsMatchingSummaryAndVideoAndOrdersItineraryByDayThenOrder() = runImmediateSuspend {
        val result = useCase("trip")

        assertEquals(listOf("first", "second", "third"), result.tripPlan.items.map { it.id })
        assertEquals(listOf(null, "next"), trips.requestedCursors)
        assertEquals("analysis", videos.requestedAnalysis)
        assertEquals("https://youtu.be/source", videos.metadataUrl)
        assertEquals(listOf("여행"), result.summary?.hashtags)
        assertEquals("영상 제목", result.metadata?.title)
        assertEquals(listOf(10, 100), result.analysis?.timelines?.map { it.timestampSeconds })
    }

    @Test
    fun retainsItineraryAndUsesSummaryVideoWhenAnalysisFails() = runImmediateSuspend {
        videos.analysisError = IllegalStateException("offline")
        val result = useCase("trip")

        assertEquals("trip", result.tripPlan.id)
        assertNull(result.analysis)
        assertEquals("https://youtu.be/saved", videos.metadataUrl)
    }

    @Test
    fun repeatedCursorStopsLookupAndMetadataFailureDoesNotHideItinerary() = runImmediateSuspend {
        trips.repeatCursor = true
        videos.metadataError = IllegalStateException("oEmbed unavailable")
        val result = useCase("trip")

        assertEquals(listOf(null, "next"), trips.requestedCursors)
        assertNull(result.summary)
        assertNull(result.metadata)
        assertEquals(3, result.tripPlan.items.size)
    }

    @Test
    fun cancellationIsNotConvertedToMissingOptionalData() {
        videos.analysisError = CancellationException("screen closed")
        assertFailsWith<CancellationException> { runImmediateSuspend { useCase("trip") } }
    }

    @Test
    fun refreshesLinkTripAuthenticationOnceForOptionalAnalysis() = runImmediateSuspend {
        videos.analysisError = LinkTripApiException(LinkTripErrorCode.UNAUTHORIZED_AUTHENTICATION_FAILED, 401, "expired")
        videos.failAnalysisOnce = true
        val result = useCase("trip")

        assertEquals(1, refreshes)
        assertEquals("analysis", result.analysis?.id)
    }

    @Test
    fun youtubeAuthenticationFailureDoesNotRefreshLinkTripAuthentication() = runImmediateSuspend {
        videos.metadataError = LinkTripApiException(LinkTripErrorCode.UNKNOWN, 401, "YouTube unavailable")
        val result = useCase("trip")

        assertEquals(0, refreshes)
        assertNull(result.metadata)
    }
}

private class DetailTripsFake : TripPlanRepository {
    val requestedCursors = mutableListOf<String?>()
    var repeatCursor = false

    override suspend fun getTripPlan(tripPlanId: String) = TripPlanDetail(
        tripPlanId, "실제 일정", "analysis",
        listOf(item("third", 2, 1), item("second", 1, 2), item("first", 1, 1)), "", "",
    )

    override suspend fun getTripPlans(cursor: String?): CursorPage<TripPlanSummary> {
        requestedCursors += cursor
        return if (cursor == null || repeatCursor) {
            CursorPage(emptyList(), "next", true)
        } else {
            CursorPage(
                listOf(TripPlanSummary("trip", "실제 일정", "analysis", "https://youtu.be/saved", 3, 1, 2, listOf("여행"), "", "")),
                null,
                false,
            )
        }
    }

    override suspend fun updateTripPlan(tripPlanId: String, title: String?, items: List<TripPlanItemOrder>?): TripPlanDetail = error("unused")
    override suspend fun deleteTripPlan(tripPlanId: String) = error("unused")

    private fun item(id: String, day: Int, order: Int) = TripPlanItem(
        id, "source-$id", day, order, id, PlaceCategory.ATTRACTION, "", "", null,
    )
}

private class DetailVideosFake : VideoRepository {
    override suspend fun getDiscoverCountries(): List<DiscoverCountry> = error("Not used in this test")
    override suspend fun getDiscoverVideos(): List<DiscoverVideo> = error("Not used in this test")
    var analysisError: Exception? = null
    var metadataError: Exception? = null
    var failAnalysisOnce = false
    var requestedAnalysis: String? = null
    var metadataUrl: String? = null

    override fun observePendingVideoAnalysisTaskId() = flowOf<String?>(null)
    override suspend fun savePendingVideoAnalysisTaskId(taskId: String, excludedTripPlanIds: Set<String>) = Unit
    override suspend fun getPendingVideoAnalysisExcludedTripPlanIds(): Set<String> = emptySet()
    override suspend fun clearPendingVideoAnalysisTaskId(expectedTaskId: String) = Unit

    override suspend fun getVideoAnalysis(videoAnalysisTaskId: String): VideoAnalysis {
        requestedAnalysis = videoAnalysisTaskId
        analysisError?.let {
            if (failAnalysisOnce) analysisError = null
            throw it
        }
        return VideoAnalysis(
            videoAnalysisTaskId, "https://youtu.be/source", true, VideoAnalysisStatus.COMPLETED,
            "실제 요약", 10000, 20000, null, true,
            listOf(VideoTimeline(100, "1:40", "", "두 번째"), VideoTimeline(10, "0:10", "", "첫 번째")),
            emptyList(),
        )
    }

    override suspend fun getYouTubeVideoMetadata(youtubeUrl: String): YouTubeVideoMetadata {
        metadataUrl = youtubeUrl
        metadataError?.let { throw it }
        return YouTubeVideoMetadata("영상 제목", "https://example.com/thumbnail.jpg")
    }

    override suspend fun analyzeVideo(youtubeUrl: String): VideoAnalysis = error("unused")
    override suspend fun getDiscoverVideosByTheme(theme: String, cursor: String?): CursorPage<DiscoverVideo> = error("unused")
    override suspend fun getDiscoverChannels(): List<DiscoverChannel> = error("unused")
    override suspend fun getDiscoverVideosByCountry(country: String): List<DiscoverVideo> = error("unused")
    override suspend fun getDiscoverVideosByRegion(region: String): List<DiscoverVideo> = error("unused")
}

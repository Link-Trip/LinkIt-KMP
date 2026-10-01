package com.linkit.company.domain.usecase

import com.linkit.company.domain.exception.LinkTripApiException
import com.linkit.company.domain.exception.LinkTripErrorCode
import com.linkit.company.domain.model.tripplan.TripPlanDetail
import com.linkit.company.domain.model.tripplan.TripPlanItem
import com.linkit.company.domain.model.tripplan.TripPlanSummary
import com.linkit.company.domain.model.video.VideoAnalysis
import com.linkit.company.domain.model.video.YouTubeVideoMetadata
import com.linkit.company.domain.repository.TripPlanRepository
import com.linkit.company.domain.repository.VideoRepository
import dev.zacsweers.metro.Inject
import kotlin.coroutines.cancellation.CancellationException

data class TripPlanContent(
    val tripPlan: TripPlanDetail,
    val summary: TripPlanSummary?,
    val analysis: VideoAnalysis?,
    val metadata: YouTubeVideoMetadata?,
)

/** 저장 일정과 원본 영상 정보를 조합한다. 부가 정보 조회 실패로 일정을 숨기지 않는다. */
@Inject
class GetTripPlanContentUseCase(
    private val ensureAuthenticated: EnsureAuthenticatedUseCase,
    private val tripPlanRepository: TripPlanRepository,
    private val videoRepository: VideoRepository,
) {
    suspend operator fun invoke(tripPlanId: String): TripPlanContent {
        ensureAuthenticated()
        val content = try {
            loadTripPlan(tripPlanId)
        } catch (error: LinkTripApiException) {
            if (!error.isUnauthorized()) throw error
            ensureAuthenticated(forceRefresh = true)
            loadTripPlan(tripPlanId)
        }
        val youtubeUrl = content.analysis?.youtubeUrl?.takeIf(String::isNotBlank)
            ?: content.summary?.youtubeUrl?.takeIf(String::isNotBlank)
        return content.copy(
            metadata = youtubeUrl?.let { optional { videoRepository.getYouTubeVideoMetadata(it) } },
        )
    }

    private suspend fun loadTripPlan(tripPlanId: String): TripPlanContent {
        val detail = tripPlanRepository.getTripPlan(tripPlanId)
        val analysis = optional(rethrowUnauthorized = true) {
            videoRepository.getVideoAnalysis(detail.videoAnalysisTaskId)
        }
        val summary = optional(rethrowUnauthorized = true) { findSummary(tripPlanId) }
        return TripPlanContent(
            tripPlan = detail.copy(
                items = detail.items.sortedWith(compareBy(TripPlanItem::day, TripPlanItem::itemOrder)),
            ),
            summary = summary,
            analysis = analysis?.copy(timelines = analysis.timelines.sortedBy { it.timestampSeconds }),
            metadata = null,
        )
    }

    private suspend fun findSummary(tripPlanId: String): TripPlanSummary? {
        val requestedCursors = mutableSetOf<String?>()
        var cursor: String? = null
        while (requestedCursors.add(cursor)) {
            val page = tripPlanRepository.getTripPlans(cursor)
            page.items.firstOrNull { it.id == tripPlanId }?.let { return it }
            if (!page.hasNext) return null
            cursor = page.nextCursor ?: return null
        }
        return null
    }

    private suspend fun <T> optional(
        rethrowUnauthorized: Boolean = false,
        block: suspend () -> T,
    ): T? = try {
        block()
    } catch (error: CancellationException) {
        throw error
    } catch (error: LinkTripApiException) {
        if (rethrowUnauthorized && error.isUnauthorized()) throw error
        null
    } catch (_: Exception) {
        null
    }

    private fun LinkTripApiException.isUnauthorized(): Boolean =
        errorCode == LinkTripErrorCode.UNAUTHORIZED_AUTHENTICATION_FAILED || httpStatus == 401
}

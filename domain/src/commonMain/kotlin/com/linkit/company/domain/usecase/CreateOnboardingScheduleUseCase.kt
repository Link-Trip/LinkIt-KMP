package com.linkit.company.domain.usecase

import com.linkit.company.domain.exception.LinkTripApiException
import com.linkit.company.domain.model.tripplan.TripPlanSummary
import com.linkit.company.domain.model.video.VideoAnalysisStatus
import com.linkit.company.domain.repository.TripPlanRepository
import com.linkit.company.domain.repository.VideoRepository
import com.linkit.company.domain.util.YouTubeUrl
import dev.zacsweers.metro.Inject

/**
 * 온보딩 튜토리얼에서 추천 영상 링크로 첫 일정을 만든다.
 *
 * 추천 영상은 서버가 미리 분석을 끝내 둔 것을 전제로 하므로 분석 대기 없이 곧바로 일정이 나와야 한다.
 * 규칙:
 * 1. [YouTubeUrl.videoIdOrNull] 실패 → [CreateOnboardingScheduleResult.InvalidFormat]
 * 2. 추천 영상 ID 집합에 없음 → [CreateOnboardingScheduleResult.NotRecommended]
 * 3. 인증 보장 후 분석 요청. 상태가 `COMPLETED`가 아니면 → [CreateOnboardingScheduleResult.NotReady]
 * 4. 저장 일정 목록에서 `videoAnalysisTaskId`가 같은 일정을 찾는다. 없으면 분석 결과를 한 번 재조회한 뒤 다시 찾고,
 *    그래도 없으면 → [CreateOnboardingScheduleResult.NotReady]
 * 5. 찾은 일정을 `확인전`으로 표시하고 → [CreateOnboardingScheduleResult.Created]
 *
 * 401은 토큰을 재발급한 뒤 한 번만 재시도한다. 그 밖의 [LinkTripApiException]은 전파한다.
 */
@Inject
class CreateOnboardingScheduleUseCase(
    private val ensureAuthenticated: EnsureAuthenticatedUseCase,
    private val videoRepository: VideoRepository,
    private val tripPlanRepository: TripPlanRepository,
) {
    suspend operator fun invoke(
        youtubeUrl: String,
        recommendedVideoUrls: List<String>,
    ): CreateOnboardingScheduleResult {
        val videoId = YouTubeUrl.videoIdOrNull(youtubeUrl)
            ?: return CreateOnboardingScheduleResult.InvalidFormat
        val recommendedIds = recommendedVideoUrls.mapNotNull(YouTubeUrl::videoIdOrNull).toSet()
        if (videoId !in recommendedIds) return CreateOnboardingScheduleResult.NotRecommended

        ensureAuthenticated()

        val analysis = runWithAuthRetry { videoRepository.analyzeVideo(YouTubeUrl.normalize(youtubeUrl)) }
        if (analysis.status != VideoAnalysisStatus.COMPLETED) return CreateOnboardingScheduleResult.NotReady

        val tripPlan = findTripPlan(analysis.id)
            ?: run {
                runWithAuthRetry { videoRepository.getVideoAnalysis(analysis.id) }
                findTripPlan(analysis.id)
            }
            ?: return CreateOnboardingScheduleResult.NotReady

        tripPlanRepository.markTripPlanUnchecked(tripPlan.id)
        return CreateOnboardingScheduleResult.Created(tripPlanId = tripPlan.id, title = tripPlan.title)
    }

    private suspend fun findTripPlan(videoAnalysisTaskId: String): TripPlanSummary? {
        val requestedCursors = mutableSetOf<String?>(null)
        var cursor: String? = null

        while (true) {
            val page = runWithAuthRetry { tripPlanRepository.getTripPlans(cursor) }
            page.items.firstOrNull { it.videoAnalysisTaskId == videoAnalysisTaskId }?.let { return it }

            if (!page.hasNext) return null
            val nextCursor = page.nextCursor ?: return null
            if (!requestedCursors.add(nextCursor)) return null
            cursor = nextCursor
        }
    }

    private suspend fun <T> runWithAuthRetry(action: suspend () -> T): T = try {
        action()
    } catch (error: LinkTripApiException) {
        if (error.httpStatus != HttpUnauthorized) throw error
        ensureAuthenticated(forceRefresh = true)
        action()
    }

    private companion object {
        const val HttpUnauthorized = 401
    }
}

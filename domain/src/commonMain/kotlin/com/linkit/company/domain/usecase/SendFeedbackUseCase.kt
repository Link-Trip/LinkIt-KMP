package com.linkit.company.domain.usecase

import com.linkit.company.domain.exception.LinkTripApiException
import com.linkit.company.domain.model.feedback.FeedbackType
import com.linkit.company.domain.repository.AppInfoRepository
import com.linkit.company.domain.repository.FeedbackRepository
import dev.zacsweers.metro.Inject

/**
 * 의견을 전송한다.
 *
 * - 유형을 고르지 않았으면 [FeedbackType.ETC]로 기록한다.
 * - 내용은 앞뒤 공백을 제거해 1~[MaxContentLength]자여야 한다.
 * - 앱 버전·플랫폼·OS 버전·기기 모델을 자동 첨부한다.
 * - 401이면 토큰을 재발급한 뒤 한 번만 재시도한다. 그 외 API 예외는 그대로 전파해 ViewModel이 분기한다.
 */
@Inject
class SendFeedbackUseCase(
    private val ensureAuthenticated: EnsureAuthenticatedUseCase,
    private val feedbackRepository: FeedbackRepository,
    private val appInfoRepository: AppInfoRepository,
) {
    suspend operator fun invoke(type: FeedbackType?, content: String) {
        val trimmed = content.trim()
        require(trimmed.isNotEmpty()) { "의견 내용은 1자 이상이어야 합니다" }
        require(trimmed.length <= MaxContentLength) { "의견 내용은 ${MaxContentLength}자 이하여야 합니다" }

        val feedbackType = type ?: FeedbackType.ETC
        val appInfo = appInfoRepository.getAppInfo()

        ensureAuthenticated()
        try {
            feedbackRepository.sendFeedback(feedbackType, trimmed, appInfo)
        } catch (error: LinkTripApiException) {
            if (error.httpStatus != HttpUnauthorized) throw error
            ensureAuthenticated(forceRefresh = true)
            feedbackRepository.sendFeedback(feedbackType, trimmed, appInfo)
        }
    }

    companion object {
        const val MaxContentLength = 200
        private const val HttpUnauthorized = 401
    }
}

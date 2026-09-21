package com.linkit.company.domain.usecase

import com.linkit.company.domain.exception.LinkTripApiException
import com.linkit.company.domain.repository.MemberRepository
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.CancellationException

/**
 * 기기 알림 허용 여부를 서버에 best-effort로 반영한다.
 *
 * UI는 서버 응답에 의존하지 않으므로 [CancellationException]을 제외한 모든 예외를 삼킨다.
 * 401이면 토큰을 재발급한 뒤 한 번만 재시도하고, 재실패도 무시한다.
 */
@Inject
class SyncNotificationSettingUseCase(
    private val ensureAuthenticated: EnsureAuthenticatedUseCase,
    private val memberRepository: MemberRepository,
) {
    suspend operator fun invoke(enabled: Boolean) {
        try {
            ensureAuthenticated()
            try {
                memberRepository.updateNotificationSetting(enabled)
            } catch (error: LinkTripApiException) {
                if (error.httpStatus != HttpUnauthorized) throw error
                ensureAuthenticated(forceRefresh = true)
                memberRepository.updateNotificationSetting(enabled)
            }
        } catch (error: CancellationException) {
            throw error
        } catch (_: Throwable) {
            // best-effort 동기화: 실패해도 화면 동작에 영향을 주지 않는다
        }
    }

    private companion object {
        const val HttpUnauthorized = 401
    }
}

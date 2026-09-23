package com.linkit.company.domain.usecase

import com.linkit.company.domain.exception.LinkTripApiException
import com.linkit.company.domain.repository.AppSettingsRepository
import com.linkit.company.domain.repository.MemberRepository
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.CancellationException

/**
 * 서버에 저장된 앱 알림 수신 설정을 읽어 로컬 캐시를 서버 값에 맞춘다. 마이페이지 진입 시 한 번 호출한다.
 *
 * 순서: 인증 보장 → `GET /members/me/notification` (401이면 토큰 재발급 후 한 번만 재시도)
 * → 응답값을 [AppSettingsRepository.setNotificationEnabled]에 저장.
 * 화면은 로컬 캐시로 이미 동작하므로 [CancellationException]을 제외한 모든 예외를 삼키고 로컬을 그대로 둔다.
 * 호출자가 사용자 조작을 우선해야 하면 이 호출을 취소한다.
 */
@Inject
class FetchNotificationSettingUseCase(
    private val ensureAuthenticated: EnsureAuthenticatedUseCase,
    private val memberRepository: MemberRepository,
    private val appSettingsRepository: AppSettingsRepository,
) {
    suspend operator fun invoke() {
        try {
            ensureAuthenticated()
            val setting = try {
                memberRepository.getNotificationSetting()
            } catch (error: LinkTripApiException) {
                if (error.httpStatus != HttpUnauthorized) throw error
                ensureAuthenticated(forceRefresh = true)
                memberRepository.getNotificationSetting()
            }
            appSettingsRepository.setNotificationEnabled(setting.enabled)
        } catch (error: CancellationException) {
            throw error
        } catch (_: Throwable) {
            // 조회 실패는 알리지 않는다. 로컬 캐시를 유지하고 다음 진입 때 다시 읽는다
        }
    }

    private companion object {
        const val HttpUnauthorized = 401
    }
}

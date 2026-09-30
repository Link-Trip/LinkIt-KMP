package com.linkit.company.domain.usecase

import com.linkit.company.domain.exception.LinkTripApiException
import com.linkit.company.domain.repository.AppSettingsRepository
import com.linkit.company.domain.repository.MemberRepository
import dev.zacsweers.metro.Inject

/**
 * 사용자가 토글로 바꾼 앱 알림 수신 설정을 서버에 반영하고, 성공했을 때만 로컬 캐시에 저장한다.
 *
 * 순서: 인증 보장 → `PUT /members/me/notification` (401이면 토큰 재발급 후 한 번만 재시도)
 * → 응답값을 [AppSettingsRepository.setNotificationEnabled]에 저장.
 * 서버 반영에 실패하면 로컬은 손대지 않고 예외를 전파한다 — ViewModel이 토글을 되돌리고 실패를 알린다.
 */
@Inject
class UpdateNotificationSettingUseCase(
    private val ensureAuthenticated: EnsureAuthenticatedUseCase,
    private val memberRepository: MemberRepository,
    private val appSettingsRepository: AppSettingsRepository,
) {
    suspend operator fun invoke(enabled: Boolean) {
        ensureAuthenticated()
        val setting = try {
            memberRepository.updateNotificationSetting(enabled)
        } catch (error: LinkTripApiException) {
            if (error.httpStatus != HttpUnauthorized) throw error
            ensureAuthenticated(forceRefresh = true)
            memberRepository.updateNotificationSetting(enabled)
        }
        appSettingsRepository.setNotificationEnabled(setting.enabled)
    }

    private companion object {
        const val HttpUnauthorized = 401
    }
}

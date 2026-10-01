package com.linkit.company.domain.usecase

import com.linkit.company.domain.exception.LinkTripApiException
import com.linkit.company.domain.repository.AppInfoRepository
import com.linkit.company.domain.repository.MemberRepository
import dev.zacsweers.metro.Inject

/** 플랫폼 SDK가 발급한 실제 토큰을 등록한다. 앱 시작·토큰 갱신 콜백에서 호출한다. */
@Inject
class RegisterFcmTokenUseCase(
    private val ensureAuthenticated: EnsureAuthenticatedUseCase,
    private val memberRepository: MemberRepository,
    private val appInfoRepository: AppInfoRepository,
) {
    suspend operator fun invoke(fcmToken: String) {
        val token = fcmToken.trim()
        require(token.isNotEmpty()) { "FCM 토큰은 비어 있을 수 없습니다." }
        val platform = appInfoRepository.getAppInfo().platform
        require(platform == "ANDROID" || platform == "IOS") { "지원하지 않는 플랫폼입니다." }

        ensureAuthenticated()
        try {
            memberRepository.registerFcmToken(token, platform)
        } catch (error: LinkTripApiException) {
            if (error.httpStatus != 401) throw error
            ensureAuthenticated(forceRefresh = true)
            memberRepository.registerFcmToken(token, platform)
        }
    }
}

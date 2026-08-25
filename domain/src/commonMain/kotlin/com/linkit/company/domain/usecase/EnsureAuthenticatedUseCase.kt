package com.linkit.company.domain.usecase

import com.linkit.company.domain.repository.AuthRepository
import dev.zacsweers.metro.Inject

/** 저장된 인증 정보가 없으면 기기 계정으로 로그인한다. */
@Inject
class EnsureAuthenticatedUseCase(
    private val authRepository: AuthRepository,
) {
    /**
     * [forceRefresh]가 true면 무효한 저장 토큰을 제거하고 새 토큰을 발급한다.
     */
    suspend operator fun invoke(forceRefresh: Boolean = false) {
        if (forceRefresh) {
            authRepository.logout()
            authRepository.login()
            return
        }

        if (!authRepository.isLoggedIn()) {
            authRepository.login()
        }
    }
}

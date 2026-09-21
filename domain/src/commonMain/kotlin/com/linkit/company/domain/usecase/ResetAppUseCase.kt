package com.linkit.company.domain.usecase

import com.linkit.company.domain.exception.LinkTripApiException
import com.linkit.company.domain.exception.LinkTripErrorCode
import com.linkit.company.domain.repository.AppSettingsRepository
import com.linkit.company.domain.repository.AuthRepository
import com.linkit.company.domain.repository.MemberRepository
import dev.zacsweers.metro.Inject

/**
 * 앱을 첫 설치 상태로 되돌린다.
 *
 * 순서: 인증 보장 → 회원 탈퇴(서버가 일정 전체 삭제를 단일 트랜잭션으로 처리) → 로컬 설정 초기화 → 로그아웃(토큰 폐기).
 * 원격 단계가 실패하면 로컬은 손대지 않고 예외를 전파한다. 이미 없는 회원(`NOT_FOUND_MEMBER`)은 성공으로 본다.
 * 401이면 토큰을 재발급한 뒤 한 번만 재시도한다. `device_id`는 유지한다.
 */
@Inject
class ResetAppUseCase(
    private val ensureAuthenticated: EnsureAuthenticatedUseCase,
    private val memberRepository: MemberRepository,
    private val appSettingsRepository: AppSettingsRepository,
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke() {
        ensureAuthenticated()
        withdrawIgnoringMissingMember()
        appSettingsRepository.clearAll()
        authRepository.logout()
    }

    private suspend fun withdrawIgnoringMissingMember() {
        try {
            withdrawWithAuthRetry()
        } catch (error: LinkTripApiException) {
            if (error.errorCode != LinkTripErrorCode.NOT_FOUND_MEMBER) throw error
        }
    }

    private suspend fun withdrawWithAuthRetry() {
        try {
            memberRepository.withdraw()
        } catch (error: LinkTripApiException) {
            if (error.httpStatus != HttpUnauthorized) throw error
            ensureAuthenticated(forceRefresh = true)
            memberRepository.withdraw()
        }
    }

    private companion object {
        const val HttpUnauthorized = 401
    }
}

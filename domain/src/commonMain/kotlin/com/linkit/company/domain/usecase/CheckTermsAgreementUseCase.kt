package com.linkit.company.domain.usecase

import com.linkit.company.domain.exception.LinkTripApiException
import com.linkit.company.domain.repository.OnboardingRepository
import com.linkit.company.domain.repository.TermsRepository
import dev.zacsweers.metro.Inject
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * 앱 시작 시 약관 동의 시트를 띄워야 하는지 서버 기준으로 판단한다.
 *
 * 기기 계정으로 로그인한 뒤 `GET /terms`를 조회해 **필수 약관 중 미동의가 하나라도 있으면 true**를 돌려준다.
 * 약관이 개정되면 서버가 기존 동의자도 미동의로 내려주므로 재동의 시트가 자연히 뜬다.
 *
 * 서버가 모두 동의됨으로 응답했는데 로컬 동의 기록이 없으면(재설치 등) 로컬 기록을 맞춰 둔다.
 * 401이면 토큰을 재발급한 뒤 한 번만 재시도한다. 네트워크 실패 등 그 외 예외는 그대로 전파하며,
 * 호출 측이 로컬 동의 기록으로 폴백할지 정한다.
 */
@OptIn(ExperimentalTime::class)
@Inject
class CheckTermsAgreementUseCase(
    private val ensureAuthenticated: EnsureAuthenticatedUseCase,
    private val termsRepository: TermsRepository,
    private val onboardingRepository: OnboardingRepository,
) {
    suspend operator fun invoke(): Boolean {
        ensureAuthenticated()
        val terms = try {
            termsRepository.getTermsAgreements()
        } catch (error: LinkTripApiException) {
            if (error.httpStatus != HttpUnauthorized) throw error
            ensureAuthenticated(forceRefresh = true)
            termsRepository.getTermsAgreements()
        }

        val needsAgreement = terms.any { it.required && !it.agreed }
        if (!needsAgreement && !onboardingRepository.isTermsAgreed()) {
            onboardingRepository.setTermsAgreed(Clock.System.now().toEpochMilliseconds())
        }
        return needsAgreement
    }

    private companion object {
        const val HttpUnauthorized = 401
    }
}

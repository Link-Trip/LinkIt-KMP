package com.linkit.company.domain.usecase

import com.linkit.company.domain.exception.LinkTripApiException
import com.linkit.company.domain.model.terms.TermsDocumentType
import com.linkit.company.domain.repository.OnboardingRepository
import com.linkit.company.domain.repository.TermsRepository
import dev.zacsweers.metro.Inject
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * 약관 동의를 서버에 기록한 뒤 로컬 동의 시각을 남긴다.
 *
 * 서버 기록이 먼저다. 서버가 실패하면 로컬도 남기지 않고 예외를 전파해 시트에서 다시 시도하게 한다.
 * (로컬만 남기면 다음 실행에서 서버가 미동의로 응답해 재동의 시트가 다시 뜬다.)
 * 401이면 토큰을 재발급한 뒤 한 번만 재시도한다. `BAD_REQUEST_TERMS_REQUIRED` 등 그 외 API 예외는 그대로 전파한다.
 */
@OptIn(ExperimentalTime::class)
@Inject
class AgreeTermsUseCase(
    private val ensureAuthenticated: EnsureAuthenticatedUseCase,
    private val termsRepository: TermsRepository,
    private val onboardingRepository: OnboardingRepository,
) {
    suspend operator fun invoke(types: List<TermsDocumentType>) {
        require(types.isNotEmpty()) { "동의할 약관 목록은 비어 있을 수 없습니다" }

        ensureAuthenticated()
        try {
            termsRepository.agreeTerms(types)
        } catch (error: LinkTripApiException) {
            if (error.httpStatus != HttpUnauthorized) throw error
            ensureAuthenticated(forceRefresh = true)
            termsRepository.agreeTerms(types)
        }
        onboardingRepository.setTermsAgreed(Clock.System.now().toEpochMilliseconds())
    }

    private companion object {
        const val HttpUnauthorized = 401
    }
}

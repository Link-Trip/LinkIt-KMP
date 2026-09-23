package com.linkit.company.domain.usecase

import com.linkit.company.domain.exception.LinkTripApiException
import com.linkit.company.domain.exception.LinkTripErrorCode
import com.linkit.company.domain.fake.InMemoryOnboardingRepository
import com.linkit.company.domain.fake.RecordingAuthRepository
import com.linkit.company.domain.fake.ScriptedTermsRepository
import com.linkit.company.domain.model.terms.TermsDocumentType
import com.linkit.company.domain.runImmediateSuspend
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class AgreeTermsUseCaseTest {

    private val requiredTypes = listOf(TermsDocumentType.SERVICE, TermsDocumentType.PRIVACY)

    @Test
    fun recordsOnServerThenLocally() = runImmediateSuspend<Unit> {
        val auth = RecordingAuthRepository(loggedIn = false)
        val terms = ScriptedTermsRepository()
        val onboarding = InMemoryOnboardingRepository()

        createUseCase(auth, terms, onboarding)(requiredTypes)

        assertEquals(listOf("login"), auth.events)
        assertEquals(listOf(requiredTypes), terms.agreedTypes)
        assertNotNull(onboarding.termsAgreedAt)
    }

    @Test
    fun doesNotRecordLocallyWhenServerRejects() = runImmediateSuspend<Unit> {
        val terms = ScriptedTermsRepository(
            agreeResults = listOf(apiException(LinkTripErrorCode.BAD_REQUEST_TERMS_REQUIRED, 400)),
        )
        val onboarding = InMemoryOnboardingRepository()

        val error = assertFailsWith<LinkTripApiException> {
            createUseCase(terms = terms, onboarding = onboarding)(requiredTypes)
        }

        assertEquals(LinkTripErrorCode.BAD_REQUEST_TERMS_REQUIRED, error.errorCode)
        assertNull(onboarding.termsAgreedAt)
        assertEquals(1, terms.agreedTypes.size)
    }

    @Test
    fun retriesOnceAfterUnauthorized() = runImmediateSuspend<Unit> {
        val auth = RecordingAuthRepository()
        val terms = ScriptedTermsRepository(
            agreeResults = listOf(apiException(LinkTripErrorCode.UNAUTHORIZED_TOKEN_INVALID, 401), null),
        )
        val onboarding = InMemoryOnboardingRepository()

        createUseCase(auth, terms, onboarding)(requiredTypes)

        assertEquals(2, terms.agreedTypes.size)
        assertEquals(listOf("logout", "login"), auth.events)
        assertNotNull(onboarding.termsAgreedAt)
    }

    @Test
    fun rejectsEmptyTypesWithoutCallingServer() = runImmediateSuspend<Unit> {
        val terms = ScriptedTermsRepository()

        assertFailsWith<IllegalArgumentException> { createUseCase(terms = terms)(emptyList()) }
        assertEquals(0, terms.agreedTypes.size)
    }

    private fun createUseCase(
        auth: RecordingAuthRepository = RecordingAuthRepository(),
        terms: ScriptedTermsRepository = ScriptedTermsRepository(),
        onboarding: InMemoryOnboardingRepository = InMemoryOnboardingRepository(),
    ) = AgreeTermsUseCase(
        ensureAuthenticated = EnsureAuthenticatedUseCase(auth),
        termsRepository = terms,
        onboardingRepository = onboarding,
    )

    private fun apiException(code: LinkTripErrorCode, status: Int) =
        LinkTripApiException(errorCode = code, httpStatus = status, message = code.name)
}

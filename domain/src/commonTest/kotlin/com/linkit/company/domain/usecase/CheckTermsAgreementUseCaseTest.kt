package com.linkit.company.domain.usecase

import com.linkit.company.domain.exception.LinkTripApiException
import com.linkit.company.domain.exception.LinkTripErrorCode
import com.linkit.company.domain.fake.InMemoryOnboardingRepository
import com.linkit.company.domain.fake.RecordingAuthRepository
import com.linkit.company.domain.fake.ScriptedTermsRepository
import com.linkit.company.domain.fake.allAgreedTerms
import com.linkit.company.domain.fake.pendingTerms
import com.linkit.company.domain.fake.termsAgreement
import com.linkit.company.domain.model.terms.TermsDocumentType
import com.linkit.company.domain.runImmediateSuspend
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CheckTermsAgreementUseCaseTest {

    @Test
    fun logsInBeforeQueryingAndReturnsTrueWhenRequiredTermIsPending() = runImmediateSuspend<Unit> {
        val auth = RecordingAuthRepository(loggedIn = false)
        val terms = ScriptedTermsRepository(agreements = pendingTerms())
        val onboarding = InMemoryOnboardingRepository()

        val needsAgreement = createUseCase(auth, terms, onboarding)()

        assertTrue(needsAgreement)
        assertEquals(listOf("login"), auth.events)
        assertEquals(listOf("getTermsAgreements"), terms.events)
        assertNull(onboarding.termsAgreedAt)
    }

    @Test
    fun returnsFalseAndRecordsLocalAgreementWhenServerSaysAllAgreed() = runImmediateSuspend<Unit> {
        val onboarding = InMemoryOnboardingRepository()

        val needsAgreement = createUseCase(terms = ScriptedTermsRepository(allAgreedTerms()), onboarding = onboarding)()

        assertFalse(needsAgreement)
        assertNotNull(onboarding.termsAgreedAt)
        assertEquals(listOf("setTermsAgreed"), onboarding.events)
    }

    @Test
    fun keepsExistingLocalAgreementUntouched() = runImmediateSuspend<Unit> {
        val onboarding = InMemoryOnboardingRepository().apply { termsAgreedAt = 1_000L }

        createUseCase(terms = ScriptedTermsRepository(allAgreedTerms()), onboarding = onboarding)()

        assertEquals(1_000L, onboarding.termsAgreedAt)
        assertEquals(emptyList<String>(), onboarding.events)
    }

    @Test
    fun ignoresOptionalPendingTerms() = runImmediateSuspend<Unit> {
        val terms = ScriptedTermsRepository(
            agreements = listOf(
                termsAgreement(TermsDocumentType.SERVICE, agreed = true),
                termsAgreement(TermsDocumentType.PRIVACY, agreed = false, required = false),
            ),
        )

        assertFalse(createUseCase(terms = terms)())
    }

    @Test
    fun retriesOnceAfterUnauthorized() = runImmediateSuspend<Unit> {
        val auth = RecordingAuthRepository()
        val terms = ScriptedTermsRepository(
            agreements = pendingTerms(),
            getResults = listOf(apiException(LinkTripErrorCode.UNAUTHORIZED_TOKEN_EXPIRED, 401), null),
        )

        assertTrue(createUseCase(auth, terms)())

        assertEquals(2, terms.events.size)
        assertEquals(listOf("logout", "login"), auth.events)
    }

    @Test
    fun propagatesNonAuthFailures() = runImmediateSuspend<Unit> {
        val terms = ScriptedTermsRepository(getResults = listOf(IllegalStateException("offline")))

        assertFailsWith<IllegalStateException> { createUseCase(terms = terms)() }
    }

    private fun createUseCase(
        auth: RecordingAuthRepository = RecordingAuthRepository(),
        terms: ScriptedTermsRepository = ScriptedTermsRepository(),
        onboarding: InMemoryOnboardingRepository = InMemoryOnboardingRepository(),
    ) = CheckTermsAgreementUseCase(
        ensureAuthenticated = EnsureAuthenticatedUseCase(auth),
        termsRepository = terms,
        onboardingRepository = onboarding,
    )

    private fun apiException(code: LinkTripErrorCode, status: Int) =
        LinkTripApiException(errorCode = code, httpStatus = status, message = code.name)
}

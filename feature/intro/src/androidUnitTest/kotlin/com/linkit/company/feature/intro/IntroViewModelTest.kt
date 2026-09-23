package com.linkit.company.feature.intro

import android.os.Looper
import com.linkit.company.domain.exception.LinkTripApiException
import com.linkit.company.domain.exception.LinkTripErrorCode
import com.linkit.company.domain.model.auth.Auth
import com.linkit.company.domain.model.onboarding.TutorialStep
import com.linkit.company.domain.model.terms.TermsAgreement
import com.linkit.company.domain.model.terms.TermsDocument
import com.linkit.company.domain.model.terms.TermsDocumentType
import com.linkit.company.domain.repository.AuthRepository
import com.linkit.company.domain.repository.OnboardingRepository
import com.linkit.company.domain.repository.TermsRepository
import com.linkit.company.domain.usecase.AgreeTermsUseCase
import com.linkit.company.domain.usecase.CheckTermsAgreementUseCase
import com.linkit.company.domain.usecase.CompleteOnboardingUseCase
import com.linkit.company.domain.usecase.EnsureAuthenticatedUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import kotlin.test.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class IntroViewModelTest {

    @Test
    fun splashGoesToOnboardingStartWhenNotCompleted() {
        val repository = FakeOnboardingRepository(completed = false)
        val viewModel = createViewModel(repository)
        val effects = viewModel.collectSideEffects()

        viewModel.onIntent(IntroIntent.SplashFinished)
        idle()

        assertEquals(IntroPhase.START, viewModel.uiState.value.phase)
        assertEquals(listOf(IntroSideEffect.NavigateToOnboardingStart), effects)
    }

    @Test
    fun splashLogsInAndChecksTermsBeforeNavigating() {
        val auth = FakeAuthRepository(loggedIn = false)
        val terms = FakeTermsRepository(pending = true)
        val viewModel = createViewModel(FakeOnboardingRepository(completed = false), terms, auth)
        val effects = viewModel.collectSideEffects()

        viewModel.onIntent(IntroIntent.SplashFinished)
        idle()

        assertEquals(listOf("login"), auth.events)
        assertEquals(1, terms.getCount)
        assertEquals(listOf(IntroSideEffect.NavigateToOnboardingStart), effects)
    }

    @Test
    fun splashGoesHomeWhenCompletedAndServerHasAgreement() {
        val viewModel = createViewModel(FakeOnboardingRepository(completed = true), FakeTermsRepository(pending = false))
        val effects = viewModel.collectSideEffects()

        viewModel.onIntent(IntroIntent.SplashFinished)
        idle()

        assertEquals(listOf(IntroSideEffect.NavigateToHome), effects)
        assertNull(viewModel.uiState.value.termsSheet)
    }

    @Test
    fun splashShowsUndismissableReagreementSheetWhenCompletedButServerRequiresAgreement() {
        val viewModel = createViewModel(FakeOnboardingRepository(completed = true), FakeTermsRepository(pending = true))
        val effects = viewModel.collectSideEffects()

        viewModel.onIntent(IntroIntent.SplashFinished)
        idle()

        assertEquals(emptyList<IntroSideEffect>(), effects)
        val sheet = assertNotNull(viewModel.uiState.value.termsSheet)
        assertFalse(sheet.dismissible)
        assertEquals(OnboardingPendingAction.RESUME, viewModel.uiState.value.pendingAction)

        viewModel.onIntent(IntroIntent.DismissTermsSheet)
        assertNotNull(viewModel.uiState.value.termsSheet)
    }

    @Test
    fun reagreementRecordsOnServerThenGoesHomeWithoutCompletingAgain() {
        val repository = FakeOnboardingRepository(completed = true)
        val terms = FakeTermsRepository(pending = true)
        val viewModel = createViewModel(repository, terms)
        val effects = viewModel.collectSideEffects()
        viewModel.onIntent(IntroIntent.SplashFinished)
        idle()
        viewModel.onIntent(IntroIntent.ToggleAllTerms)

        viewModel.onIntent(IntroIntent.AgreeAndStart)
        idle()

        assertEquals(listOf(listOf(TermsDocumentType.SERVICE, TermsDocumentType.PRIVACY)), terms.agreedTypes)
        assertEquals(listOf("setTermsAgreed"), repository.events)
        assertEquals(listOf(IntroSideEffect.NavigateToHome), effects)
    }

    @Test
    fun splashFallsBackToLocalAgreementWhenServerCheckFails() {
        val agreedLocally = FakeOnboardingRepository(completed = true).apply { termsAgreedAt = 1L }
        val viewModel = createViewModel(agreedLocally, FakeTermsRepository(getError = IllegalStateException("offline")))
        val effects = viewModel.collectSideEffects()

        viewModel.onIntent(IntroIntent.SplashFinished)
        idle()

        assertEquals(listOf(IntroSideEffect.NavigateToHome), effects)
    }

    @Test
    fun splashShowsSheetWhenServerCheckFailsAndNoLocalAgreement() {
        val viewModel = createViewModel(
            FakeOnboardingRepository(completed = true),
            FakeTermsRepository(getError = IllegalStateException("offline")),
        )
        val effects = viewModel.collectSideEffects()

        viewModel.onIntent(IntroIntent.SplashFinished)
        idle()

        assertEquals(emptyList<IntroSideEffect>(), effects)
        assertNotNull(viewModel.uiState.value.termsSheet)
    }

    @Test
    fun tapStartNowSkipsSheetWhenServerAlreadyHasAgreement() {
        val repository = FakeOnboardingRepository(completed = false)
        val terms = FakeTermsRepository(pending = false)
        val viewModel = createViewModel(repository, terms)
        val effects = viewModel.collectSideEffects()
        viewModel.onIntent(IntroIntent.SplashFinished)
        idle()

        viewModel.onIntent(IntroIntent.TapStartNow)
        idle()

        assertNull(viewModel.uiState.value.termsSheet)
        assertEquals(0, terms.agreedTypes.size)
        assertTrue(repository.onboardingCompleted)
        assertEquals(listOf(IntroSideEffect.NavigateToOnboardingStart, IntroSideEffect.NavigateToHome), effects)
    }

    @Test
    fun agreeAndStartReenablesSheetWhenServerRejects() {
        val repository = FakeOnboardingRepository()
        val terms = FakeTermsRepository(
            agreeError = LinkTripApiException(LinkTripErrorCode.BAD_REQUEST_TERMS_REQUIRED, 400, "필수 약관"),
        )
        val viewModel = createViewModel(repository, terms)
        val effects = viewModel.collectSideEffects()
        viewModel.onIntent(IntroIntent.TapStartNow)
        viewModel.onIntent(IntroIntent.ToggleAllTerms)

        viewModel.onIntent(IntroIntent.AgreeAndStart)
        idle()

        val sheet = assertNotNull(viewModel.uiState.value.termsSheet)
        assertFalse(sheet.isSubmitting)
        assertTrue(sheet.canStart)
        assertEquals(emptyList<String>(), repository.events)
        assertEquals(emptyList<IntroSideEffect>(), effects)
    }

    @Test
    fun splashFinishedIsHandledOnlyOnce() {
        val viewModel = createViewModel(FakeOnboardingRepository(completed = false))
        val effects = viewModel.collectSideEffects()

        viewModel.onIntent(IntroIntent.SplashFinished)
        viewModel.onIntent(IntroIntent.SplashFinished)
        idle()

        assertEquals(1, effects.size)
    }

    @Test
    fun tapStartNowOpensUncheckedSheetWithSkipPending() {
        val viewModel = createViewModel()

        viewModel.onIntent(IntroIntent.TapStartNow)

        val sheet = assertNotNull(viewModel.uiState.value.termsSheet)
        assertEquals(OnboardingPendingAction.SKIP, viewModel.uiState.value.pendingAction)
        assertFalse(sheet.serviceAgreed)
        assertFalse(sheet.privacyAgreed)
        assertFalse(sheet.allAgreed)
        assertFalse(sheet.canStart)
    }

    @Test
    fun toggleAllTermsChecksAndUnchecksBothItems() {
        val viewModel = createViewModel()
        viewModel.onIntent(IntroIntent.TapSeeHowTo)

        viewModel.onIntent(IntroIntent.ToggleAllTerms)
        val checked = assertNotNull(viewModel.uiState.value.termsSheet)
        assertTrue(checked.serviceAgreed && checked.privacyAgreed && checked.allAgreed && checked.canStart)

        viewModel.onIntent(IntroIntent.ToggleAllTerms)
        val unchecked = assertNotNull(viewModel.uiState.value.termsSheet)
        assertFalse(unchecked.serviceAgreed || unchecked.privacyAgreed || unchecked.canStart)
    }

    @Test
    fun individualTogglesDeriveAllAgreed() {
        val viewModel = createViewModel()
        viewModel.onIntent(IntroIntent.TapSeeHowTo)

        viewModel.onIntent(IntroIntent.ToggleServiceTerms)
        assertFalse(assertNotNull(viewModel.uiState.value.termsSheet).allAgreed)

        viewModel.onIntent(IntroIntent.TogglePrivacyTerms)
        assertTrue(assertNotNull(viewModel.uiState.value.termsSheet).allAgreed)

        viewModel.onIntent(IntroIntent.ToggleServiceTerms)
        val sheet = assertNotNull(viewModel.uiState.value.termsSheet)
        assertFalse(sheet.allAgreed)
        assertFalse(sheet.canStart)
    }

    @Test
    fun dismissClearsSheetAndPendingActionWithoutSaving() {
        val repository = FakeOnboardingRepository()
        val viewModel = createViewModel(repository)
        viewModel.onIntent(IntroIntent.TapStartNow)
        viewModel.onIntent(IntroIntent.ToggleAllTerms)

        viewModel.onIntent(IntroIntent.DismissTermsSheet)
        idle()

        assertNull(viewModel.uiState.value.termsSheet)
        assertNull(viewModel.uiState.value.pendingAction)
        assertEquals(emptyList<String>(), repository.events)
    }

    @Test
    fun openTermsDetailKeepsCheckStateAndNavigates() {
        val viewModel = createViewModel()
        val effects = viewModel.collectSideEffects()
        viewModel.onIntent(IntroIntent.TapSeeHowTo)
        viewModel.onIntent(IntroIntent.ToggleServiceTerms)

        viewModel.onIntent(IntroIntent.OpenTermsDetail(TermsDocumentType.SERVICE))
        idle()

        assertEquals(listOf(IntroSideEffect.NavigateToTermsDetail(TermsDocumentType.SERVICE)), effects)
        assertTrue(assertNotNull(viewModel.uiState.value.termsSheet).serviceAgreed)
    }

    @Test
    fun agreeAndStartForSkipRecordsTermsThenCompletionThenGoesHome() {
        val repository = FakeOnboardingRepository()
        val terms = FakeTermsRepository()
        val viewModel = createViewModel(repository, terms)
        val effects = viewModel.collectSideEffects()
        viewModel.onIntent(IntroIntent.TapStartNow)
        viewModel.onIntent(IntroIntent.ToggleAllTerms)

        viewModel.onIntent(IntroIntent.AgreeAndStart)
        idle()

        assertEquals(
            listOf("setTermsAgreed", "setOnboardingCompleted(true)", "setTutorialStep(null)"),
            repository.events,
        )
        assertEquals(listOf(listOf(TermsDocumentType.SERVICE, TermsDocumentType.PRIVACY)), terms.agreedTypes)
        assertTrue(repository.onboardingCompleted)
        assertNull(repository.tutorialStep.value)
        assertEquals(listOf(IntroSideEffect.NavigateToHome), effects)
    }

    @Test
    fun agreeAndStartForTutorialSetsFirstStepWithoutCompleting() {
        val repository = FakeOnboardingRepository()
        val viewModel = createViewModel(repository)
        val effects = viewModel.collectSideEffects()
        viewModel.onIntent(IntroIntent.TapSeeHowTo)
        viewModel.onIntent(IntroIntent.ToggleAllTerms)

        viewModel.onIntent(IntroIntent.AgreeAndStart)
        idle()

        assertEquals(listOf("setTermsAgreed", "setTutorialStep(CREATE_BUTTON)"), repository.events)
        assertFalse(repository.onboardingCompleted)
        assertEquals(TutorialStep.CREATE_BUTTON, repository.tutorialStep.value)
        assertEquals(listOf(IntroSideEffect.NavigateToHome), effects)
    }

    @Test
    fun agreeAndStartIsIgnoredUntilBothTermsAreChecked() {
        val repository = FakeOnboardingRepository()
        val viewModel = createViewModel(repository)
        viewModel.onIntent(IntroIntent.TapStartNow)
        viewModel.onIntent(IntroIntent.ToggleServiceTerms)

        viewModel.onIntent(IntroIntent.AgreeAndStart)
        idle()

        assertEquals(emptyList<String>(), repository.events)
        assertFalse(assertNotNull(viewModel.uiState.value.termsSheet).isSubmitting)
    }

    @Test
    fun duplicateAgreeAndStartIsProcessedOnce() {
        val repository = FakeOnboardingRepository()
        val viewModel = createViewModel(repository)
        val effects = viewModel.collectSideEffects()
        viewModel.onIntent(IntroIntent.TapStartNow)
        viewModel.onIntent(IntroIntent.ToggleAllTerms)

        viewModel.onIntent(IntroIntent.AgreeAndStart)
        viewModel.onIntent(IntroIntent.AgreeAndStart)
        idle()

        assertEquals(1, repository.events.count { it == "setTermsAgreed" })
        assertEquals(1, effects.size)
    }

    private fun createViewModel(
        repository: FakeOnboardingRepository = FakeOnboardingRepository(),
        terms: FakeTermsRepository = FakeTermsRepository(),
        auth: FakeAuthRepository = FakeAuthRepository(),
    ): IntroViewModel {
        val ensureAuthenticated = EnsureAuthenticatedUseCase(auth)
        return IntroViewModel(
            onboardingRepository = repository,
            completeOnboarding = CompleteOnboardingUseCase(repository),
            checkTermsAgreement = CheckTermsAgreementUseCase(ensureAuthenticated, terms, repository),
            agreeTerms = AgreeTermsUseCase(ensureAuthenticated, terms, repository),
        )
    }

    private fun IntroViewModel.collectSideEffects(): List<IntroSideEffect> {
        val effects = mutableListOf<IntroSideEffect>()
        CoroutineScope(Dispatchers.Main).launch { sideEffect.collect(effects::add) }
        idle()
        return effects
    }

    private fun idle() = shadowOf(Looper.getMainLooper()).idle()
}

private class FakeOnboardingRepository(
    completed: Boolean = false,
) : OnboardingRepository {
    val events = mutableListOf<String>()
    var onboardingCompleted = completed
    var termsAgreedAt: Long? = null
    val tutorialStep = MutableStateFlow<TutorialStep?>(null)

    override suspend fun isOnboardingCompleted(): Boolean = onboardingCompleted

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        events += "setOnboardingCompleted($completed)"
        onboardingCompleted = completed
    }

    override suspend fun isTermsAgreed(): Boolean = termsAgreedAt != null

    override suspend fun setTermsAgreed(agreedAtEpochMillis: Long) {
        events += "setTermsAgreed"
        termsAgreedAt = agreedAtEpochMillis
    }

    override fun observeTutorialStep(): Flow<TutorialStep?> = tutorialStep

    override suspend fun setTutorialStep(step: TutorialStep?) {
        events += "setTutorialStep($step)"
        tutorialStep.value = step
    }

    override suspend fun clearAll() {
        events += "clearAll"
        onboardingCompleted = false
        termsAgreedAt = null
        tutorialStep.value = null
    }
}

private class FakeAuthRepository(
    private var loggedIn: Boolean = true,
) : AuthRepository {
    val events = mutableListOf<String>()

    override suspend fun login(): Auth {
        events += "login"
        loggedIn = true
        return Auth(memberId = "member-id", accessToken = "access-token")
    }

    override suspend fun isLoggedIn(): Boolean = loggedIn

    override suspend fun logout() {
        events += "logout"
        loggedIn = false
    }
}

/**
 * @param pending 서버가 필수 약관 미동의를 돌려주는지(기본 true = 최초 설치처럼 동의 필요)
 * @param getError 목록 조회 실패를 흉내낼 예외
 * @param agreeError 동의 기록 실패를 흉내낼 예외
 */
private class FakeTermsRepository(
    private val pending: Boolean = true,
    private val getError: Throwable? = null,
    private val agreeError: Throwable? = null,
) : TermsRepository {
    var getCount = 0
        private set
    val agreedTypes = mutableListOf<List<TermsDocumentType>>()

    override suspend fun getTermsDocuments(): List<TermsDocument> = emptyList()

    override suspend fun getTermsAgreements(): List<TermsAgreement> {
        getCount += 1
        getError?.let { throw it }
        return listOf(
            agreement(TermsDocumentType.SERVICE, agreed = true),
            agreement(TermsDocumentType.PRIVACY, agreed = !pending),
        )
    }

    override suspend fun agreeTerms(types: List<TermsDocumentType>) {
        agreedTypes += types
        agreeError?.let { throw it }
    }

    private fun agreement(type: TermsDocumentType, agreed: Boolean) = TermsAgreement(
        type = type,
        title = type.name,
        required = true,
        version = 1,
        detailUrl = "https://example.com/${type.name}",
        agreed = agreed,
    )
}

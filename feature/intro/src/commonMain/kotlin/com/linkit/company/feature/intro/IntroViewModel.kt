package com.linkit.company.feature.intro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.linkit.company.core.common.architecture.MviContainer
import com.linkit.company.core.common.architecture.MviContext
import com.linkit.company.domain.model.onboarding.OnboardingCompletion
import com.linkit.company.domain.model.onboarding.TutorialStep
import com.linkit.company.domain.model.terms.TermsDocumentType
import com.linkit.company.domain.repository.OnboardingRepository
import com.linkit.company.domain.usecase.AgreeTermsUseCase
import com.linkit.company.domain.usecase.CheckTermsAgreementUseCase
import com.linkit.company.domain.usecase.CompleteOnboardingUseCase
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

/**
 * 인트로 → 온보딩 시작 → 약관 동의 → 목적지 결정 흐름(data-model.md §4.1·§5).
 *
 * - `SplashFinished`: 기기 계정으로 로그인한 뒤 서버 약관 동의 상태를 확인한다(`GET /terms`).
 *   온보딩 미완료면 시작 화면으로, 완료면 필수 약관 미동의(개정 포함)가 있을 때만 인트로 위에 재동의 시트를 띄우고
 *   아니면 메인으로 간다. 서버 확인에 실패하면 로컬 동의 기록으로 판단한다.
 * - `TapStartNow`/`TapSeeHowTo`: 서버가 이미 모두 동의됨으로 응답했으면 시트 없이 바로 목적지 행위를 수행한다.
 * - `AgreeAndStart`: 서버에 동의를 기록하고(`POST /terms/agreement`) 로컬 동의 시각을 남긴 뒤,
 *   `30초 만에 사용법 보기`면 튜토리얼 1단계 세팅, `바로 시작하기`면 완료 기록, 재동의면 그대로 → 메인.
 *
 * 온보딩 완료 조회·튜토리얼 단계 세팅은 단순 저장소 호출이라 UseCase 없이 직접 부른다(contracts §2).
 */
@ContributesIntoMap(AppScope::class)
@ViewModelKey(IntroViewModel::class)
@Inject
class IntroViewModel(
    private val onboardingRepository: OnboardingRepository,
    private val completeOnboarding: CompleteOnboardingUseCase,
    private val checkTermsAgreement: CheckTermsAgreementUseCase,
    private val agreeTerms: AgreeTermsUseCase,
) : ViewModel() {
    private val container = MviContainer<IntroIntent, IntroSideEffect, IntroUiState>(
        initialState = IntroUiState(),
        onIntent = { handleIntent(it) },
    )

    val uiState = container.uiState
    val sideEffect = container.sideEffect

    /**
     * 필수 약관 동의 시트가 필요한지. 스플래시에서 서버로 확정되기 전에는 true로 두어
     * 확인이 끝나지 않은 채 버튼을 누르면 시트를 띄운다(동의 없이 진행되는 일이 없게).
     */
    private var needsTermsAgreement = true

    fun onIntent(intent: IntroIntent) = container.intent(intent)

    private fun MviContext<IntroUiState, IntroSideEffect>.handleIntent(intent: IntroIntent) {
        when (intent) {
            IntroIntent.SplashFinished -> handleSplashFinished()
            IntroIntent.TapSeeHowTo -> startAction(OnboardingPendingAction.TUTORIAL)
            IntroIntent.TapStartNow -> startAction(OnboardingPendingAction.SKIP)
            IntroIntent.ToggleAllTerms -> updateSheet {
                val next = !allAgreed
                copy(serviceAgreed = next, privacyAgreed = next)
            }
            IntroIntent.ToggleServiceTerms -> updateSheet { copy(serviceAgreed = !serviceAgreed) }
            IntroIntent.TogglePrivacyTerms -> updateSheet { copy(privacyAgreed = !privacyAgreed) }
            is IntroIntent.OpenTermsDetail -> {
                if (currentState.termsSheet != null) {
                    postSideEffect(IntroSideEffect.NavigateToTermsDetail(intent.type))
                }
            }
            IntroIntent.DismissTermsSheet -> {
                val sheet = currentState.termsSheet ?: return
                if (sheet.isSubmitting || !sheet.dismissible) return
                reduce { copy(termsSheet = null, pendingAction = null) }
            }
            IntroIntent.AgreeAndStart -> agreeAndStart()
        }
    }

    /** 애니메이션 종료는 SPLASH 단계에서 한 번만 처리한다(백그라운드 복귀·재구성 시 중복 이동 방지). */
    private fun MviContext<IntroUiState, IntroSideEffect>.handleSplashFinished() {
        if (currentState.phase != IntroPhase.SPLASH) return
        reduce { copy(phase = IntroPhase.START) }
        viewModelScope.launch {
            val completed = try {
                onboardingRepository.isOnboardingCompleted()
            } catch (error: CancellationException) {
                throw error
            } catch (_: Throwable) {
                false
            }
            needsTermsAgreement = resolveTermsAgreement()

            with(container.mviContext) {
                when {
                    !completed -> postSideEffect(IntroSideEffect.NavigateToOnboardingStart)
                    needsTermsAgreement -> reduce {
                        copy(
                            pendingAction = OnboardingPendingAction.RESUME,
                            termsSheet = TermsSheetState(dismissible = false),
                        )
                    }
                    else -> postSideEffect(IntroSideEffect.NavigateToHome)
                }
            }
        }
    }

    /** 서버 기준 필수 약관 미동의 여부. 서버 확인(로그인 포함)에 실패하면 로컬 동의 기록으로 폴백한다. */
    private suspend fun resolveTermsAgreement(): Boolean {
        return try {
            checkTermsAgreement()
        } catch (error: CancellationException) {
            throw error
        } catch (_: Throwable) {
            try {
                !onboardingRepository.isTermsAgreed()
            } catch (error: CancellationException) {
                throw error
            } catch (_: Throwable) {
                true
            }
        }
    }

    /** 시작 화면 버튼: 동의가 필요하면 시트를 열고, 서버에 이미 동의돼 있으면 바로 목적지 행위를 수행한다. */
    private fun MviContext<IntroUiState, IntroSideEffect>.startAction(action: OnboardingPendingAction) {
        if (currentState.pendingAction != null) return
        if (needsTermsAgreement) {
            reduce { copy(pendingAction = action, termsSheet = TermsSheetState()) }
            return
        }
        reduce { copy(pendingAction = action) }
        viewModelScope.launch {
            try {
                completeAction(action)
                container.mviContext.postSideEffect(IntroSideEffect.NavigateToHome)
            } catch (error: CancellationException) {
                throw error
            } catch (_: Throwable) {
                container.mviContext.reduce { copy(pendingAction = null) }
            }
        }
    }

    private fun MviContext<IntroUiState, IntroSideEffect>.updateSheet(
        transform: TermsSheetState.() -> TermsSheetState,
    ) {
        val sheet = currentState.termsSheet ?: return
        if (sheet.isSubmitting) return
        reduce { copy(termsSheet = sheet.transform()) }
    }

    private fun MviContext<IntroUiState, IntroSideEffect>.agreeAndStart() {
        val sheet = currentState.termsSheet ?: return
        val action = currentState.pendingAction ?: return
        if (!sheet.canStart) return
        reduce { copy(termsSheet = sheet.copy(isSubmitting = true)) }

        val agreedTypes = buildList {
            if (sheet.serviceAgreed) add(TermsDocumentType.SERVICE)
            if (sheet.privacyAgreed) add(TermsDocumentType.PRIVACY)
        }
        viewModelScope.launch {
            try {
                agreeTerms(agreedTypes)
                needsTermsAgreement = false
                completeAction(action)
                container.mviContext.postSideEffect(IntroSideEffect.NavigateToHome)
            } catch (error: CancellationException) {
                throw error
            } catch (_: Throwable) {
                // 서버 기록 실패 시 시트를 다시 활성화해 재시도할 수 있게 한다
                container.mviContext.reduce {
                    copy(termsSheet = termsSheet?.copy(isSubmitting = false))
                }
            }
        }
    }

    /** 약관 동의가 끝난 뒤 목적지에 따른 기록. 메인 이동은 호출 측이 한다. */
    private suspend fun completeAction(action: OnboardingPendingAction) {
        when (action) {
            OnboardingPendingAction.TUTORIAL ->
                onboardingRepository.setTutorialStep(TutorialStep.CREATE_BUTTON)
            OnboardingPendingAction.SKIP ->
                completeOnboarding(OnboardingCompletion.SKIPPED_AT_START)
            OnboardingPendingAction.RESUME -> Unit
        }
    }

    override fun onCleared() {
        container.close()
        super.onCleared()
    }
}

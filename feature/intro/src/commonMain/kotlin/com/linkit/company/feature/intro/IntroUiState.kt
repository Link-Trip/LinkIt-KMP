package com.linkit.company.feature.intro

import com.linkit.company.core.common.architecture.contract.UiState

/** 인트로 Activity의 현재 화면 단계. */
enum class IntroPhase {
    /** 인트로 애니메이션(`Intro` 라우트) */
    SPLASH,

    /** 온보딩 시작 화면(`OnboardingStart` 라우트) */
    START,
}

/** 약관 동의 뒤에 이어질 행위. 시작 화면 버튼(FR-010) 또는 개정 약관 재동의. */
enum class OnboardingPendingAction {
    /** `30초 만에 사용법 보기` → 튜토리얼 */
    TUTORIAL,

    /** `바로 시작하기` → 메인 화면 */
    SKIP,

    /** 온보딩을 이미 마친 회원의 개정 약관 재동의(인트로 화면 위 시트) → 메인 화면 */
    RESUME,
}

/**
 * 약관 동의 바텀시트 상태(FR-005~FR-011).
 *
 * @property serviceAgreed `[필수] 서비스 이용약관 동의`
 * @property privacyAgreed `[필수] 개인정보 수집·이용 동의`
 * @property isSubmitting `동의하고 시작하기` 저장 중(중복 탭 방지)
 * @property dismissible 끌어 내리기·바깥 탭으로 닫을 수 있는지. 개정 약관 재동의는 필수라 닫을 수 없다
 */
data class TermsSheetState(
    val serviceAgreed: Boolean = false,
    val privacyAgreed: Boolean = false,
    val isSubmitting: Boolean = false,
    val dismissible: Boolean = true,
) {
    /** `전체 동의` 표시 상태. 필수 2종이 모두 체크되면 true(FR-007). */
    val allAgreed: Boolean
        get() = serviceAgreed && privacyAgreed

    /** `동의하고 시작하기` 활성 조건(FR-008). */
    val canStart: Boolean
        get() = allAgreed && !isSubmitting
}

data class IntroUiState(
    val phase: IntroPhase = IntroPhase.SPLASH,
    val pendingAction: OnboardingPendingAction? = null,
    /** `null` = 시트 닫힘 */
    val termsSheet: TermsSheetState? = null,
) : UiState

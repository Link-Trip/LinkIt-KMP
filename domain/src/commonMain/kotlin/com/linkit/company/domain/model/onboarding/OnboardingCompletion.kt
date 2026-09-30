package com.linkit.company.domain.model.onboarding

/**
 * 온보딩을 완료로 기록하는 사유. `CompleteOnboardingUseCase(reason)` 의 파라미터.
 *
 * 저장소에는 완료 여부(Boolean)만 남기고 사유는 로그·분석용으로만 쓴다.
 */
enum class OnboardingCompletion {
    /** 온보딩 시작 화면 `바로 시작하기` + 약관 동의 */
    SKIPPED_AT_START,

    /** 튜토리얼 화면 `건너뛰기` */
    SKIPPED_IN_TUTORIAL,

    /** 분석 완료 화면 `생성된 일정 확인하기` */
    TUTORIAL_FINISHED,
}

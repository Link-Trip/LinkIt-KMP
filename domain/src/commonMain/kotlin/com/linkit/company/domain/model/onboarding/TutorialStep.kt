package com.linkit.company.domain.model.onboarding

/**
 * 체험형 튜토리얼의 현재 단계. `null`이면 튜토리얼이 아니다.
 *
 * 값은 프로세스 메모리에만 존재하며(`OnboardingRepository.observeTutorialStep()`),
 * `null`이 아닌 동안 지도·영상 링크 화면은 온보딩 모드(건너뛰기 노출, 코치마크)로 동작한다.
 */
enum class TutorialStep {
    /** 지도 메인: `일정 생성` FAB 안내 */
    CREATE_BUTTON,

    /** 지도 메인(생성 메뉴 열림): `영상 링크로 만들기` 안내 */
    VIDEO_LINK_OPTION,

    /** 영상 링크로 만들기: 첫 추천 영상 `링크복사` 안내 */
    COPY_LINK,

    /** 영상 링크로 만들기: `복사한 링크 붙여넣기` 안내 */
    PASTE_LINK,

    /** 영상 링크로 만들기: 자유 조작(온보딩 규칙은 유지) */
    FREE,
}

package com.linkit.company.feature.schedule

import com.linkit.company.core.common.architecture.contract.Intent

sealed interface ScheduleIntent : Intent {
    data class UpdateVideoLink(val link: String) : ScheduleIntent
    data object SubmitVideoLink : ScheduleIntent
    data object CreateDuplicateVideoSchedule : ScheduleIntent
    data object OpenExistingSchedule : ScheduleIntent
    data object DismissExistingSchedule : ScheduleIntent
    data class SelectTripDetailTab(val tab: TripDetailTab) : ScheduleIntent

    // 추천 영상 · 클립보드 · 튜토리얼 (data-model.md §4.3)

    /** 화면 진입·`다시 시도` 시 추천 영상 조회 */
    data object LoadRecommendedVideos : ScheduleIntent

    /** 추천 영상 카드 `링크복사` */
    data class CopyRecommendedLink(val url: String) : ScheduleIntent

    /** 화면 진입·포커스 복귀 시 클립보드 텍스트 존재 여부 */
    data class ClipboardAvailabilityChanged(val hasText: Boolean) : ScheduleIntent

    /** `복사한 링크 붙여넣기` 칩 탭 */
    data class PasteFromClipboard(val text: String) : ScheduleIntent

    /** 클립보드 토스트 본문 탭 → 링크 입력 */
    data object ApplyClipboardToast : ScheduleIntent

    /** 클립보드 토스트 닫기 */
    data object DismissClipboardToast : ScheduleIntent

    data object DismissErrorToast : ScheduleIntent

    /** 진입 후 안내 지연(`TutorialGuideDelayMillis`) 경과 */
    data object GuideDelayElapsed : ScheduleIntent

    /** TopNav 우측 `건너뛰기` */
    data object SkipOnboarding : ScheduleIntent

    /** 분석 완료 화면 `생성된 일정 확인하기` */
    data object ConfirmOnboardingSchedule : ScheduleIntent
}

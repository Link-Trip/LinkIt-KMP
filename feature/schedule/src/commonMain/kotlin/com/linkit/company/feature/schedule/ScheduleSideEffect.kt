package com.linkit.company.feature.schedule

import com.linkit.company.core.common.architecture.contract.SideEffect

sealed interface ScheduleSideEffect : SideEffect {
    data class NavigateToAnalysis(
        val videoTitle: String?,
        val thumbnailUrl: String?,
    ) : ScheduleSideEffect

    data class OpenExistingSchedule(
        val tripPlanId: String,
        val title: String,
    ) : ScheduleSideEffect

    /** 시스템 클립보드에 [text] 를 쓴다(화면의 `LocalClipboardManager`) */
    data class WriteClipboard(val text: String) : ScheduleSideEffect

    /** 온보딩 일정 생성 성공 → 분석 중 화면을 건너뛰고 분석 완료 화면으로 */
    data object NavigateToAnalysisComplete : ScheduleSideEffect

    /** 건너뛰기·`생성된 일정 확인하기` → ScheduleActivity 종료(메인 화면 복귀) */
    data object FinishOnboarding : ScheduleSideEffect
}

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
}

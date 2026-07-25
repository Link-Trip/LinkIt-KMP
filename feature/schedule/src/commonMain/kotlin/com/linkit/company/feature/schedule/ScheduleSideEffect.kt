package com.linkit.company.feature.schedule

import com.linkit.company.core.common.architecture.contract.SideEffect

sealed interface ScheduleSideEffect : SideEffect {
    data object NavigateToAnalysis : ScheduleSideEffect

    data class OpenExistingSchedule(
        val tripPlanId: String,
        val title: String,
    ) : ScheduleSideEffect
}

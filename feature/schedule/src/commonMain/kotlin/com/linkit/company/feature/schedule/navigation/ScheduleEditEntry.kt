package com.linkit.company.feature.schedule.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.linkit.company.core.navigation.LinkItNavKey
import com.linkit.company.feature.schedule.ScheduleAnalysisLoadingScreen
import com.linkit.company.feature.schedule.ScheduleAnalysisCompleteScreen
import com.linkit.company.feature.schedule.ScheduleEditScreen

fun EntryProviderScope<NavKey>.scheduleEditEntry(
    onCreateSchedule: () -> Unit,
    onAnalysisComplete: () -> Unit,
    onConfirmAnalysis: () -> Unit,
    onBack: () -> Unit,
) {
    entry<LinkItNavKey.ScheduleEdit> {
        ScheduleEditScreen(
            onCreateSchedule = onCreateSchedule,
        )
    }

    entry<LinkItNavKey.ScheduleAnalysisLoading> {
        ScheduleAnalysisLoadingScreen(
            onBack = onBack,
            onAnalysisComplete = onAnalysisComplete,
        )
    }

    entry<LinkItNavKey.ScheduleAnalysisComplete> {
        ScheduleAnalysisCompleteScreen(onConfirm = onConfirmAnalysis)
    }
}

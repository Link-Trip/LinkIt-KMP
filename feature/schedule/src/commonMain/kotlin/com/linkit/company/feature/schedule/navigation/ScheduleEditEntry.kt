package com.linkit.company.feature.schedule.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.linkit.company.core.navigation.LinkItNavKey
import com.linkit.company.feature.schedule.ScheduleEditScreen

fun EntryProviderScope<NavKey>.scheduleEditEntry(
    onCreateSchedule: () -> Unit,
) {
    entry<LinkItNavKey.ScheduleEdit> {
        ScheduleEditScreen(
            onCreateSchedule = onCreateSchedule,
        )
    }
}

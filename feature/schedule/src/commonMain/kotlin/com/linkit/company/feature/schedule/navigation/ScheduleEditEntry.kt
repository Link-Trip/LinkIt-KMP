package com.linkit.company.feature.schedule.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.linkit.company.core.navigation.LinkItNavKey
import com.linkit.company.feature.schedule.ScheduleAnalysisLoadingScreen
import com.linkit.company.feature.schedule.ScheduleAnalysisCompleteScreen
import com.linkit.company.feature.schedule.ScheduleEditScreen
import com.linkit.company.feature.schedule.ScheduleTripDetailScreen

fun EntryProviderScope<NavKey>.scheduleEditEntry(
    onCreateSchedule: (videoTitle: String?, thumbnailUrl: String?) -> Unit,
    onOpenExistingSchedule: (tripPlanId: String, title: String) -> Unit,
    onReturnHome: () -> Unit,
    showNotificationPermissionSheet: Boolean,
    onAllowNotifications: () -> Unit,
    onDismissNotificationPrompt: () -> Unit,
    onConfirmAnalysis: () -> Unit,
    onBack: () -> Unit,
) {
    entry<LinkItNavKey.ScheduleEdit> {
        ScheduleEditScreen(
            onCreateSchedule = onCreateSchedule,
            onOpenExistingSchedule = onOpenExistingSchedule,
            onBack = onBack,
        )
    }

    entry<LinkItNavKey.ScheduleAnalysisLoading> { route ->
        ScheduleAnalysisLoadingScreen(
            videoTitle = route.videoTitle,
            thumbnailUrl = route.thumbnailUrl,
            onBack = onBack,
            onReturnHome = onReturnHome,
            showNotificationPermissionSheet = showNotificationPermissionSheet,
            onAllowNotifications = onAllowNotifications,
            onDismissNotificationPrompt = onDismissNotificationPrompt,
        )
    }

    entry<LinkItNavKey.ScheduleAnalysisComplete> {
        ScheduleAnalysisCompleteScreen(onConfirm = onConfirmAnalysis)
    }

    entry<LinkItNavKey.ScheduleTripDetail> { route ->
        ScheduleTripDetailScreen(
            tripPlanId = route.tripPlanId,
            title = route.title,
            focusedPlaceId = route.focusedPlaceId,
            onBack = onBack,
        )
    }
}

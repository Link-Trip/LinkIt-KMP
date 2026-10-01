package com.linkit.company.feature.schedule.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import com.linkit.company.core.navigation.LinkItNavDisplay
import com.linkit.company.core.navigation.LinkItNavKey
import com.linkit.company.core.navigation.LinkItNavigator
import com.linkit.company.core.navigation.LinkItSavedStateConfiguration
import com.linkit.company.core.navigation.rememberNavigationState

/** 일정 화면의 백스택은 공유하고, 종료와 알림 권한 요청만 플랫폼에 위임한다. */
@Composable
internal fun ScheduleNavigationHost(
    onClose: () -> Unit,
    startRoute: LinkItNavKey,
    showNotificationPermissionSheet: () -> Boolean,
    onAllowNotifications: () -> Unit,
    onDismissNotificationPrompt: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val navigationState = rememberNavigationState(
        savedStateConfiguration = LinkItSavedStateConfiguration,
        startRoute = startRoute,
        topLevelRoutes = setOf(startRoute),
    )
    val navigator = remember(navigationState) { LinkItNavigator(navigationState) }
    val navigateBack = {
        if (navigationState.currentRoute == startRoute) onClose() else navigator.navigateBack()
    }
    val provider = entryProvider {
        scheduleEditEntry(
            onCreateSchedule = { videoTitle, thumbnailUrl ->
                navigator.navigate(LinkItNavKey.ScheduleAnalysisLoading(videoTitle, thumbnailUrl))
            },
            onOpenExistingSchedule = { tripPlanId, title ->
                navigator.navigate(LinkItNavKey.ScheduleTripDetail(tripPlanId = tripPlanId, title = title))
            },
            onReturnHome = onClose,
            showNotificationPermissionSheet = showNotificationPermissionSheet,
            onAllowNotifications = onAllowNotifications,
            onDismissNotificationPrompt = onDismissNotificationPrompt,
            onNavigateToAnalysisComplete = { navigator.navigate(LinkItNavKey.ScheduleAnalysisComplete) },
            onFinishOnboarding = onClose,
            onBack = navigateBack,
        )
    }
    LinkItNavDisplay(
        modifier = modifier.fillMaxSize(),
        backStack = navigationState.currentTopLevelBackStack,
        onBack = navigateBack,
        entryProvider = provider,
    )
}

package com.linkit.company.feature.schedule.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import com.linkit.company.core.navigation.LinkItNavDisplay
import com.linkit.company.core.navigation.LinkItNavKey
import com.linkit.company.core.navigation.LinkItNavigator
import com.linkit.company.core.navigation.LinkItSavedStateConfiguration
import com.linkit.company.core.navigation.rememberNavigationState

@Composable
fun ScheduleNavDisplay(
    onFinishActivity: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val navigationState = rememberNavigationState(
        savedStateConfiguration = LinkItSavedStateConfiguration,
        startRoute = LinkItNavKey.ScheduleEdit,
        topLevelRoutes = setOf(LinkItNavKey.ScheduleEdit),
    )
    val navigator = remember(navigationState) { LinkItNavigator(navigationState) }

    val entryProvider = entryProvider {
        scheduleEditEntry(
            onCreateSchedule = { navigator.navigate(LinkItNavKey.ScheduleAnalysisLoading) },
            onAnalysisComplete = { navigator.navigate(LinkItNavKey.ScheduleAnalysisComplete) },
            onConfirmAnalysis = {},
            onBack = navigator::navigateBack,
        )
    }

    LinkItNavDisplay(
        modifier = modifier
            .fillMaxSize()
            .systemBarsPadding(),
        backStack = navigationState.currentTopLevelBackStack,
        onBack = {
            if (navigationState.currentRoute == LinkItNavKey.ScheduleEdit) {
                onFinishActivity()
            } else {
                navigator.navigateBack()
            }
        },
        entryProvider = entryProvider,
    )
}

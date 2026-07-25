package com.linkit.company.feature.schedule.navigation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.navigation3.runtime.entryProvider
import com.linkit.company.core.navigation.LinkItNavDisplay
import com.linkit.company.core.navigation.LinkItNavKey
import com.linkit.company.core.navigation.LinkItNavigator
import com.linkit.company.core.navigation.LinkItSavedStateConfiguration
import com.linkit.company.core.navigation.rememberNavigationState

@Composable
fun ScheduleNavDisplay(
    onFinishActivity: () -> Unit,
    startRoute: LinkItNavKey = LinkItNavKey.ScheduleEdit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val notificationPreferences = remember(context) {
        context.getSharedPreferences(NotificationPreferencesName, 0)
    }
    var showNotificationPermissionSheet by rememberSaveable {
        mutableStateOf(
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS,
                ) != PackageManager.PERMISSION_GRANTED &&
                !notificationPreferences.getBoolean(NotificationPromptedKey, false),
        )
    }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) {
        showNotificationPermissionSheet = false
    }
    val allowNotifications = {
        notificationPreferences.edit().putBoolean(NotificationPromptedKey, true).apply()
        showNotificationPermissionSheet = false
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
    val dismissNotificationPrompt = {
        notificationPreferences.edit().putBoolean(NotificationPromptedKey, true).apply()
        showNotificationPermissionSheet = false
    }

    val navigationState = rememberNavigationState(
        savedStateConfiguration = LinkItSavedStateConfiguration,
        startRoute = startRoute,
        topLevelRoutes = setOf(startRoute),
    )
    val navigator = remember(navigationState) { LinkItNavigator(navigationState) }

    val entryProvider = entryProvider {
        scheduleEditEntry(
            onCreateSchedule = { navigator.navigate(LinkItNavKey.ScheduleAnalysisLoading) },
            onOpenExistingSchedule = { tripPlanId, title ->
                navigator.navigate(
                    LinkItNavKey.ScheduleTripDetail(
                        tripPlanId = tripPlanId,
                        title = title,
                    ),
                )
            },
            onReturnHome = onFinishActivity,
            showNotificationPermissionSheet = showNotificationPermissionSheet,
            onAllowNotifications = allowNotifications,
            onDismissNotificationPrompt = dismissNotificationPrompt,
            onConfirmAnalysis = { navigator.navigate(LinkItNavKey.ScheduleTripDetail()) },
            onBack = navigator::navigateBack,
        )
    }

    LinkItNavDisplay(
        modifier = modifier
            .fillMaxSize()
            .systemBarsPadding(),
        backStack = navigationState.currentTopLevelBackStack,
        onBack = {
            if (navigationState.currentRoute == startRoute) {
                onFinishActivity()
            } else {
                navigator.navigateBack()
            }
        },
        entryProvider = entryProvider,
    )
}

private const val NotificationPreferencesName = "schedule_notification_preferences"
private const val NotificationPromptedKey = "notification_prompted"

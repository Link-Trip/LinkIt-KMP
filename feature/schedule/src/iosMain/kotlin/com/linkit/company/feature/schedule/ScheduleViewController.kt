package com.linkit.company.feature.schedule

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.ComposeUIViewController
import com.linkit.company.core.common.AppGraph
import com.linkit.company.core.designsystem.theme.LinkItTheme
import com.linkit.company.core.navigation.LinkItNavKey
import com.linkit.company.feature.schedule.navigation.ScheduleNavigationHost
import dev.zacsweers.metrox.viewmodel.LocalMetroViewModelFactory
import dev.zacsweers.metrox.viewmodel.metroViewModel
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNAuthorizationStatusAuthorized
import platform.UserNotifications.UNAuthorizationStatusEphemeral
import platform.UserNotifications.UNAuthorizationStatusProvisional
import platform.UserNotifications.UNUserNotificationCenter
import kotlin.coroutines.resume

fun ScheduleViewController(
    appGraph: AppGraph,
    onClose: () -> Unit,
    startRoute: LinkItNavKey = LinkItNavKey.ScheduleEdit,
) = ComposeUIViewController {
    CompositionLocalProvider(LocalMetroViewModelFactory provides appGraph.metroViewModelFactory) {
        LinkItTheme {
            IosScheduleNavigation(onClose, startRoute)
        }
    }
}

@Composable
private fun IosScheduleNavigation(onClose: () -> Unit, startRoute: LinkItNavKey) {
    val promptViewModel: NotificationPromptViewModel = metroViewModel()
    val isPrompted by promptViewModel.isPrompted.collectAsState()
    val isOnboardingMode by promptViewModel.isOnboardingMode.collectAsState()
    val notificationCenter = remember { UNUserNotificationCenter.currentNotificationCenter() }
    var isPermissionMissing by remember { mutableStateOf<Boolean?>(null) }
    var isPromptDismissed by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(notificationCenter) {
        isPermissionMissing = suspendCancellableCoroutine { continuation ->
            notificationCenter.getNotificationSettingsWithCompletionHandler { settings ->
                val missing = settings?.authorizationStatus?.let { status ->
                    status != UNAuthorizationStatusAuthorized &&
                        status != UNAuthorizationStatusProvisional &&
                        status != UNAuthorizationStatusEphemeral
                }
                if (continuation.isActive) continuation.resume(missing)
            }
        }
    }

    ScheduleNavigationHost(
        onClose = onClose,
        startRoute = startRoute,
        showNotificationPermissionSheet = {
            isPermissionMissing == true && isPrompted == false &&
                isOnboardingMode == false && !isPromptDismissed
        },
        onAllowNotifications = {
            promptViewModel.markPrompted()
            isPromptDismissed = true
            notificationCenter.requestAuthorizationWithOptions(
                options = UNAuthorizationOptionAlert or UNAuthorizationOptionBadge or UNAuthorizationOptionSound,
                completionHandler = { _, _ -> },
            )
        },
        onDismissNotificationPrompt = {
            promptViewModel.markPrompted()
            isPromptDismissed = true
        },
    )
}

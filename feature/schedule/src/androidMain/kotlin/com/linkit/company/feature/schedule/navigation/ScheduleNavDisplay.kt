package com.linkit.company.feature.schedule.navigation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import com.linkit.company.feature.schedule.NotificationPromptViewModel
import dev.zacsweers.metrox.viewmodel.metroViewModel

@Composable
fun ScheduleNavDisplay(
    onFinishActivity: () -> Unit,
    startRoute: LinkItNavKey = LinkItNavKey.ScheduleEdit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    // 알림 안내 노출 이력은 앱 설정 저장소(DataStore)에 두어 앱 초기화 시 함께 지워진다
    val notificationPromptViewModel: NotificationPromptViewModel = metroViewModel()
    val isNotificationPrompted by notificationPromptViewModel.isPrompted.collectAsState()
    val isNotificationPermissionMissing = remember(context) {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) != PackageManager.PERMISSION_GRANTED
    }
    var isNotificationSheetDismissed by rememberSaveable { mutableStateOf(false) }
    val showNotificationPermissionSheet =
        isNotificationPermissionMissing && isNotificationPrompted == false && !isNotificationSheetDismissed
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) {
        isNotificationSheetDismissed = true
    }
    val allowNotifications = {
        notificationPromptViewModel.markPrompted()
        isNotificationSheetDismissed = true
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
        notificationPromptViewModel.markPrompted()
        isNotificationSheetDismissed = true
    }

    val navigationState = rememberNavigationState(
        savedStateConfiguration = LinkItSavedStateConfiguration,
        startRoute = startRoute,
        topLevelRoutes = setOf(startRoute),
    )
    val navigator = remember(navigationState) { LinkItNavigator(navigationState) }

    val entryProvider = entryProvider {
        scheduleEditEntry(
            onCreateSchedule = { videoTitle, thumbnailUrl ->
                navigator.navigate(
                    LinkItNavKey.ScheduleAnalysisLoading(
                        videoTitle = videoTitle,
                        thumbnailUrl = thumbnailUrl,
                    ),
                )
            },
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

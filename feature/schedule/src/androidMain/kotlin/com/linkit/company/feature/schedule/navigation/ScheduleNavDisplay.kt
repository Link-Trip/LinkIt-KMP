package com.linkit.company.feature.schedule.navigation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import com.linkit.company.core.navigation.LinkItNavKey
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
    // 튜토리얼 중에는 코치마크 위에 시트가 겹치지 않도록 띄우지 않는다 (research R8)
    val isOnboardingMode by notificationPromptViewModel.isOnboardingMode.collectAsState()
    val isNotificationPermissionMissing = remember(context) {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) != PackageManager.PERMISSION_GRANTED
    }
    var isNotificationSheetDismissed by rememberSaveable { mutableStateOf(false) }
    val showNotificationPermissionSheet = {
        isNotificationPermissionMissing && isNotificationPrompted == false &&
            isOnboardingMode == false && !isNotificationSheetDismissed
    }
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

    ScheduleNavigationHost(
        onClose = onFinishActivity,
        startRoute = startRoute,
        showNotificationPermissionSheet = showNotificationPermissionSheet,
        onAllowNotifications = allowNotifications,
        onDismissNotificationPrompt = dismissNotificationPrompt,
        modifier = modifier.systemBarsPadding(),
    )
}

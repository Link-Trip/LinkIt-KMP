package com.linkit.company.feature.map.mypage.platform

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
internal actual fun rememberNotificationPermissionController(): NotificationPermissionController {
    val context = LocalContext.current
    return remember(context) { AndroidNotificationPermissionController(context.applicationContext) }
}

private class AndroidNotificationPermissionController(
    private val context: Context,
) : NotificationPermissionController {
    override suspend fun isAppNotificationEnabled(): Boolean? =
        runCatching {
            context.getSystemService(NotificationManager::class.java)?.areNotificationsEnabled()
        }.getOrNull()

    override fun openAppNotificationSettings() {
        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
            .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        runCatching { context.startActivity(intent) }
    }
}

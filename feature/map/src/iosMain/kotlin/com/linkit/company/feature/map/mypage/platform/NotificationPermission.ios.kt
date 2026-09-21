package com.linkit.company.feature.map.mypage.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString
import platform.UserNotifications.UNAuthorizationStatusAuthorized
import platform.UserNotifications.UNAuthorizationStatusEphemeral
import platform.UserNotifications.UNAuthorizationStatusProvisional
import platform.UserNotifications.UNUserNotificationCenter
import kotlin.coroutines.resume

@Composable
internal actual fun rememberNotificationPermissionController(): NotificationPermissionController =
    remember { IosNotificationPermissionController() }

private class IosNotificationPermissionController : NotificationPermissionController {
    override suspend fun isAppNotificationEnabled(): Boolean? =
        suspendCancellableCoroutine { continuation ->
            UNUserNotificationCenter.currentNotificationCenter().getNotificationSettingsWithCompletionHandler { settings ->
                val enabled = when (settings?.authorizationStatus) {
                    UNAuthorizationStatusAuthorized,
                    UNAuthorizationStatusProvisional,
                    UNAuthorizationStatusEphemeral,
                    -> true
                    null -> null
                    else -> false
                }
                if (continuation.isActive) continuation.resume(enabled)
            }
        }

    override fun openAppNotificationSettings() {
        val url = NSURL.URLWithString(UIApplicationOpenSettingsURLString) ?: return
        val application = UIApplication.sharedApplication
        if (application.canOpenURL(url)) {
            application.openURL(url, options = emptyMap<Any?, Any>(), completionHandler = null)
        }
    }
}

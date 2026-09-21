package com.linkit.company.feature.map.mypage.platform

import androidx.compose.runtime.Composable

/**
 * 기기의 앱 알림 허용 여부 조회와 알림 설정 화면 이동을 담당하는 플랫폼 경계.
 *
 * 알림 상태의 진실은 기기 설정에 있으므로 앱은 저장하지 않고 진입·복귀 시마다 조회한다.
 */
interface NotificationPermissionController {
    /** 기기 알림 허용 여부. 조회할 수 없으면 null. */
    suspend fun isAppNotificationEnabled(): Boolean?

    /** 기기의 이 앱 알림 설정 화면을 연다. 지원하지 않는 환경이면 아무 것도 하지 않는다. */
    fun openAppNotificationSettings()
}

@Composable
internal expect fun rememberNotificationPermissionController(): NotificationPermissionController

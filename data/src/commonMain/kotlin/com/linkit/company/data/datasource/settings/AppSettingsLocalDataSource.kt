package com.linkit.company.data.datasource.settings

import kotlinx.coroutines.flow.Flow

interface AppSettingsLocalDataSource {
    /** 저장된 enum name. 없으면 null. */
    fun observeMapDisplayType(): Flow<String?>
    suspend fun saveMapDisplayType(value: String)
    suspend fun isNotificationPrompted(): Boolean
    suspend fun saveNotificationPrompted(value: Boolean)
    /** 앱 설정 키 2개(`map_display_type`·`notification_prompted`)만 제거한다. 온보딩 키는 OnboardingLocalDataSource, 인증 키는 AuthLocalDataSource 책임. */
    suspend fun clearAll()
}

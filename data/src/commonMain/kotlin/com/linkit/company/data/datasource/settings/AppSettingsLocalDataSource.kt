package com.linkit.company.data.datasource.settings

import kotlinx.coroutines.flow.Flow

interface AppSettingsLocalDataSource {
    /** 저장된 enum name. 없으면 null. */
    fun observeMapDisplayType(): Flow<String?>
    suspend fun saveMapDisplayType(value: String)
    suspend fun isOnboardingCompleted(): Boolean
    suspend fun saveOnboardingCompleted(value: Boolean)
    suspend fun isNotificationPrompted(): Boolean
    suspend fun saveNotificationPrompted(value: Boolean)
    /** 앱 설정 키 3개만 제거한다. 인증 키는 AuthLocalDataSource 책임. */
    suspend fun clearAll()
}

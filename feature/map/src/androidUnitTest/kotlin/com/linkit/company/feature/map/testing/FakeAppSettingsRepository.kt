package com.linkit.company.feature.map.testing

import com.linkit.company.domain.model.settings.MapDisplayType
import com.linkit.company.domain.repository.AppSettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/** 메모리 기반 앱 설정 저장소. 지도 설정 저장 횟수를 기록한다. */
class FakeAppSettingsRepository(
    initial: MapDisplayType = MapDisplayType.DEFAULT,
) : AppSettingsRepository {
    val mapDisplayType = MutableStateFlow(initial)
    val savedMapDisplayTypes = mutableListOf<MapDisplayType>()
    var onboardingCompleted = false
    var notificationPrompted = false
    var clearAllCount = 0
        private set

    override fun observeMapDisplayType(): Flow<MapDisplayType> = mapDisplayType

    override suspend fun setMapDisplayType(type: MapDisplayType) {
        savedMapDisplayTypes += type
        mapDisplayType.value = type
    }

    override suspend fun isOnboardingCompleted(): Boolean = onboardingCompleted

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        onboardingCompleted = completed
    }

    override suspend fun isNotificationPrompted(): Boolean = notificationPrompted

    override suspend fun setNotificationPrompted(prompted: Boolean) {
        notificationPrompted = prompted
    }

    override suspend fun clearAll() {
        clearAllCount += 1
        mapDisplayType.value = MapDisplayType.DEFAULT
        onboardingCompleted = false
        notificationPrompted = false
    }
}

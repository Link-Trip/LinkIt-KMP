package com.linkit.company.data.repository

import com.linkit.company.data.DataScope
import com.linkit.company.data.datasource.settings.AppSettingsLocalDataSource
import com.linkit.company.domain.model.settings.MapDisplayType
import com.linkit.company.domain.repository.AppSettingsRepository
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Inject
@ContributesBinding(DataScope::class)
class AppSettingsRepositoryImpl(
    private val appSettingsLocalDataSource: AppSettingsLocalDataSource,
) : AppSettingsRepository {

    override fun observeMapDisplayType(): Flow<MapDisplayType> {
        return appSettingsLocalDataSource.observeMapDisplayType().map { stored ->
            MapDisplayType.entries.firstOrNull { it.name == stored } ?: MapDisplayType.DEFAULT
        }
    }

    override suspend fun setMapDisplayType(type: MapDisplayType) {
        appSettingsLocalDataSource.saveMapDisplayType(type.name)
    }

    override suspend fun isNotificationPrompted(): Boolean {
        return appSettingsLocalDataSource.isNotificationPrompted()
    }

    override suspend fun setNotificationPrompted(prompted: Boolean) {
        appSettingsLocalDataSource.saveNotificationPrompted(prompted)
    }

    override suspend fun clearAll() {
        appSettingsLocalDataSource.clearAll()
    }
}

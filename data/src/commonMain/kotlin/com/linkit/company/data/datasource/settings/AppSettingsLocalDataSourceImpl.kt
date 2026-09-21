package com.linkit.company.data.datasource.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.linkit.company.data.DataScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

@Inject
@ContributesBinding(DataScope::class)
class AppSettingsLocalDataSourceImpl(
    private val dataStore: DataStore<Preferences>,
) : AppSettingsLocalDataSource {

    override fun observeMapDisplayType(): Flow<String?> {
        return dataStore.data.map { it[KEY_MAP_DISPLAY_TYPE] }.distinctUntilChanged()
    }

    override suspend fun saveMapDisplayType(value: String) {
        dataStore.edit { it[KEY_MAP_DISPLAY_TYPE] = value }
    }

    override suspend fun isOnboardingCompleted(): Boolean {
        return dataStore.data.first()[KEY_ONBOARDING_COMPLETED] ?: false
    }

    override suspend fun saveOnboardingCompleted(value: Boolean) {
        dataStore.edit { it[KEY_ONBOARDING_COMPLETED] = value }
    }

    override suspend fun isNotificationPrompted(): Boolean {
        return dataStore.data.first()[KEY_NOTIFICATION_PROMPTED] ?: false
    }

    override suspend fun saveNotificationPrompted(value: Boolean) {
        dataStore.edit { it[KEY_NOTIFICATION_PROMPTED] = value }
    }

    override suspend fun clearAll() {
        dataStore.edit { preferences ->
            preferences.remove(KEY_MAP_DISPLAY_TYPE)
            preferences.remove(KEY_ONBOARDING_COMPLETED)
            preferences.remove(KEY_NOTIFICATION_PROMPTED)
        }
    }

    companion object {
        private val KEY_MAP_DISPLAY_TYPE = stringPreferencesKey("map_display_type")
        private val KEY_ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        private val KEY_NOTIFICATION_PROMPTED = booleanPreferencesKey("notification_prompted")
    }
}

package com.linkit.company.data.datasource.tripplan

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.linkit.company.data.DataScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

@Inject
@ContributesBinding(DataScope::class)
class TripPlanLocalDataSourceImpl(
    private val dataStore: DataStore<Preferences>,
) : TripPlanLocalDataSource {

    override fun observeUncheckedIds(): Flow<Set<String>> {
        return dataStore.data.map { it[KEY_UNCHECKED_TRIP_PLAN_IDS].orEmpty() }.distinctUntilChanged()
    }

    override suspend fun addUncheckedId(id: String) {
        dataStore.edit { preferences ->
            preferences[KEY_UNCHECKED_TRIP_PLAN_IDS] = preferences[KEY_UNCHECKED_TRIP_PLAN_IDS].orEmpty() + id
        }
    }

    override suspend fun removeUncheckedId(id: String) {
        dataStore.edit { preferences ->
            val remaining = preferences[KEY_UNCHECKED_TRIP_PLAN_IDS].orEmpty() - id
            if (remaining.isEmpty()) {
                preferences.remove(KEY_UNCHECKED_TRIP_PLAN_IDS)
            } else {
                preferences[KEY_UNCHECKED_TRIP_PLAN_IDS] = remaining
            }
        }
    }

    override suspend fun clearUnchecked() {
        dataStore.edit { it.remove(KEY_UNCHECKED_TRIP_PLAN_IDS) }
    }

    companion object {
        private val KEY_UNCHECKED_TRIP_PLAN_IDS = stringSetPreferencesKey("unchecked_trip_plan_ids")
    }
}

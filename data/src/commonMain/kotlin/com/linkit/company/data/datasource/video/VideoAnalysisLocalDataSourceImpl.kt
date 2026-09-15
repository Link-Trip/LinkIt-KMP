package com.linkit.company.data.datasource.video

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.linkit.company.data.DataScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

@Inject
@ContributesBinding(DataScope::class)
class VideoAnalysisLocalDataSourceImpl(
    private val dataStore: DataStore<Preferences>,
) : VideoAnalysisLocalDataSource {
    override fun observePendingTaskId(): Flow<String?> =
        dataStore.data.map { it[KEY_PENDING_TASK_ID] }.distinctUntilChanged()

    override suspend fun savePendingTaskId(taskId: String, excludedTripPlanIds: Set<String>) {
        require(taskId.isNotBlank())
        dataStore.edit {
            it[KEY_PENDING_TASK_ID] = taskId
            it[KEY_EXCLUDED_TRIP_PLAN_IDS] = excludedTripPlanIds
        }
    }

    override suspend fun getExcludedTripPlanIds(): Set<String> =
        dataStore.data.first()[KEY_EXCLUDED_TRIP_PLAN_IDS].orEmpty()

    override suspend fun clearPendingTaskId(expectedTaskId: String) {
        dataStore.edit { preferences ->
            if (preferences[KEY_PENDING_TASK_ID] == expectedTaskId) {
                preferences.remove(KEY_PENDING_TASK_ID)
                preferences.remove(KEY_EXCLUDED_TRIP_PLAN_IDS)
            }
        }
    }

    private companion object {
        val KEY_PENDING_TASK_ID = stringPreferencesKey("pending_video_analysis_task_id")
        val KEY_EXCLUDED_TRIP_PLAN_IDS = stringSetPreferencesKey("pending_video_analysis_excluded_trip_plan_ids")
    }
}

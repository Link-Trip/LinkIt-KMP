package com.linkit.company.data.datasource.video

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class VideoAnalysisLocalDataSourceTest {
    @Test
    fun storedTaskSurvivesDataSourceRecreationAndOnlyMatchingAcknowledgementClearsIt() = runBlocking {
        val store = InMemoryPreferencesStore()
        val original = VideoAnalysisLocalDataSourceImpl(store)
        original.savePendingTaskId("first", setOf("old-plan"))

        val recreated = VideoAnalysisLocalDataSourceImpl(store)
        assertEquals("first", recreated.observePendingTaskId().first())
        assertEquals(setOf("old-plan"), recreated.getExcludedTripPlanIds())

        recreated.savePendingTaskId("second", setOf("another-plan"))
        original.clearPendingTaskId("first")
        assertEquals("second", recreated.observePendingTaskId().first())
        assertEquals(setOf("another-plan"), recreated.getExcludedTripPlanIds())

        recreated.clearPendingTaskId("second")
        assertNull(recreated.observePendingTaskId().first())
        assertEquals(emptySet(), recreated.getExcludedTripPlanIds())
    }
}

private class InMemoryPreferencesStore : DataStore<Preferences> {
    private val preferences = MutableStateFlow(emptyPreferences())
    override val data: Flow<Preferences> = preferences

    override suspend fun updateData(transform: suspend (Preferences) -> Preferences): Preferences {
        return transform(preferences.value).also { preferences.value = it }
    }
}

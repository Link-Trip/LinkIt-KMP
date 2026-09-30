package com.linkit.company.data.datasource.tripplan

import com.linkit.company.data.datasource.onboarding.temporaryDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class TripPlanLocalDataSourceTest {

    @Test
    fun startsEmpty() = runBlocking {
        val source = TripPlanLocalDataSourceImpl(temporaryDataStore())

        assertEquals(emptySet(), source.observeUncheckedIds().first())
    }

    @Test
    fun addsAndRemovesIds() = runBlocking {
        val source = TripPlanLocalDataSourceImpl(temporaryDataStore())

        source.addUncheckedId("plan-1")
        source.addUncheckedId("plan-2")
        assertEquals(setOf("plan-1", "plan-2"), source.observeUncheckedIds().first())

        source.removeUncheckedId("plan-1")
        assertEquals(setOf("plan-2"), source.observeUncheckedIds().first())

        source.removeUncheckedId("missing")
        assertEquals(setOf("plan-2"), source.observeUncheckedIds().first())
    }

    @Test
    fun clearRemovesEverything() = runBlocking {
        val source = TripPlanLocalDataSourceImpl(temporaryDataStore())
        source.addUncheckedId("plan-1")

        source.clearUnchecked()

        assertEquals(emptySet(), source.observeUncheckedIds().first())
    }
}

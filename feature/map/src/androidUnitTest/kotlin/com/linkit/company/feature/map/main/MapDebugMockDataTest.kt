package com.linkit.company.feature.map.main

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MapDebugMockDataTest {
    @Test
    fun coversSchedulePlaceAndFilterExplorationStates() {
        val schedules = MapDebugMockData.schedules.map { it.toMapScheduleUiModel() }
        val markerIds = schedules.flatMap { schedule ->
            schedule.places.map(MapPlaceUiModel::markerId)
        }

        assertEquals(3, schedules.size)
        assertTrue(schedules.all { it.centerLatitude != null && it.centerLongitude != null })
        assertTrue(schedules.all { it.places.size >= 3 })
        assertEquals(markerIds.size, markerIds.distinct().size)
        assertEquals(setOf("신주쿠", "하라주쿠", "시부야"), schedules.map { it.regionLabel }.toSet())
        assertTrue(schedules.any { MapDurationFilter.SHORT.accepts(it.days) })
        assertTrue(schedules.any { MapDurationFilter.MEDIUM.accepts(it.days) })
        assertTrue(schedules.any { MapDurationFilter.LONG.accepts(it.days) })

        val selectedSchedule = schedules.first()
        val scheduleState = MapUiState(
            loadState = MapLoadState.CONTENT,
            schedules = schedules,
            selectedScheduleId = selectedSchedule.id,
        )
        val placeState = scheduleState.copy(
            selectedPlaceMarkerId = selectedSchedule.places.first().markerId,
        )

        assertEquals(MapSelection.SCHEDULE, scheduleState.selection)
        assertEquals(MapSelection.PLACE, placeState.selection)
        assertEquals(selectedSchedule.places.first(), placeState.selectedPlace)
    }
}

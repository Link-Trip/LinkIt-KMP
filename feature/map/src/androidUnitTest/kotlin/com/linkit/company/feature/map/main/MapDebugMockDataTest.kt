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
        assertEquals(setOf("도쿄"), schedules.map { it.regionLabel }.toSet())
        assertTrue(schedules.any { MapDurationFilter.ONE_NIGHT_TWO_DAYS.accepts(it.days) })
        assertTrue(schedules.any { MapDurationFilter.THREE_NIGHTS_FOUR_DAYS.accepts(it.days) })
        assertTrue(schedules.any { MapDurationFilter.FIVE_NIGHTS_OR_MORE.accepts(it.days) })

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

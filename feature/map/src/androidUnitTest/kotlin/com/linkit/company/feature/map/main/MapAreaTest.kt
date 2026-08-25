package com.linkit.company.feature.map.main

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class MapAreaTest {
    @Test
    fun convexHullKeepsOnlyOutermostCoordinates() {
        val corners = setOf(
            MapCoordinateUiModel(lat = 0.0, lng = 0.0),
            MapCoordinateUiModel(lat = 0.0, lng = 2.0),
            MapCoordinateUiModel(lat = 2.0, lng = 0.0),
            MapCoordinateUiModel(lat = 2.0, lng = 2.0),
        )
        val hull = (corners + MapCoordinateUiModel(lat = 1.0, lng = 1.0)).convexHull()

        assertEquals(corners, hull.toSet())
    }

    @Test
    fun scheduleWithFewerThanThreeCoordinatesHasNoArea() {
        val schedule = MapTestFixtures.schedules.first()

        assertNull(schedule.copy(places = schedule.places.take(2)).toMapAreaUiModel())
    }

    @Test
    fun debugScheduleBuildsAnAreaFromItsOutermostPlaces() {
        val schedule = MapDebugMockData.schedules.first().toMapScheduleUiModel()
        val area = schedule.toMapAreaUiModel()

        assertNotNull(area)
        assertEquals(schedule.id, area?.id)
        assertEquals(4, area?.points?.size)
    }

    @Test
    fun selectedScheduleCameraBoundsContainEveryValidPlaceMarker() {
        val markers = listOf(
            MapMarkerUiModel(
                id = "schedule",
                lat = 37.5,
                lng = 127.0,
                label = "일정",
                type = MapMarkerType.SCHEDULE,
                selected = true,
            ),
            MapMarkerUiModel(
                id = "place-1",
                lat = 37.4,
                lng = 126.9,
                label = "장소 1",
                type = MapMarkerType.PLACE,
            ),
            MapMarkerUiModel(
                id = "place-2",
                lat = 37.6,
                lng = 127.1,
                label = "장소 2",
                type = MapMarkerType.PLACE,
            ),
            MapMarkerUiModel(
                id = "invalid-place",
                lat = Double.NaN,
                lng = 127.2,
                label = "잘못된 장소",
                type = MapMarkerType.PLACE,
            ),
        )

        val bounds = markers.selectedScheduleCameraBounds()

        assertNotNull(bounds)
        assertEquals("schedule", bounds?.id)
        assertEquals(
            listOf(
                MapCoordinateUiModel(37.4, 126.9),
                MapCoordinateUiModel(37.6, 127.1),
            ),
            bounds?.points,
        )
    }
}

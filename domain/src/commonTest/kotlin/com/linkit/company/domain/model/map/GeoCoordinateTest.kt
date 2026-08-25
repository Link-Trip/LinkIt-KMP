package com.linkit.company.domain.model.map

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class GeoCoordinateTest {

    @Test
    fun fromOrNull_acceptsBoundaryCoordinates() {
        assertEquals(
            GeoCoordinate(latitude = -90.0, longitude = -180.0),
            GeoCoordinate.fromOrNull(latitude = -90.0, longitude = -180.0),
        )
        assertEquals(
            GeoCoordinate(latitude = 90.0, longitude = 180.0),
            GeoCoordinate.fromOrNull(latitude = 90.0, longitude = 180.0),
        )
    }

    @Test
    fun fromOrNull_rejectsMissingOrInvalidCoordinates() {
        assertNull(GeoCoordinate.fromOrNull(latitude = null, longitude = 127.0))
        assertNull(GeoCoordinate.fromOrNull(latitude = 37.0, longitude = null))
        assertNull(GeoCoordinate.fromOrNull(latitude = Double.NaN, longitude = 127.0))
        assertNull(GeoCoordinate.fromOrNull(latitude = 37.0, longitude = Double.POSITIVE_INFINITY))
        assertNull(GeoCoordinate.fromOrNull(latitude = 90.1, longitude = 127.0))
        assertNull(GeoCoordinate.fromOrNull(latitude = 37.0, longitude = -180.1))
    }

    @Test
    fun constructorRejectsInvalidCoordinates() {
        assertFailsWith<IllegalArgumentException> {
            GeoCoordinate(latitude = -91.0, longitude = 127.0)
        }
        assertFailsWith<IllegalArgumentException> {
            GeoCoordinate(latitude = 37.0, longitude = 181.0)
        }
    }

    @Test
    fun boundingBoxCenter_returnsCenterOfCoordinateExtremes() {
        val center = listOf(
            GeoCoordinate(latitude = 35.0, longitude = 129.0),
            GeoCoordinate(latitude = 37.0, longitude = 126.0),
            GeoCoordinate(latitude = 36.0, longitude = 128.0),
        ).boundingBoxCenterOrNull()

        assertEquals(GeoCoordinate(latitude = 36.0, longitude = 127.5), center)
    }

    @Test
    fun boundingBoxCenter_handlesEmptyAndSingleCoordinateCollections() {
        assertNull(emptyList<GeoCoordinate>().boundingBoxCenterOrNull())

        val onlyCoordinate = GeoCoordinate(latitude = 37.5665, longitude = 126.978)
        assertEquals(onlyCoordinate, listOf(onlyCoordinate).boundingBoxCenterOrNull())
    }
}

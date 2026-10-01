package com.linkit.company.feature.map.main

import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreLocation.CLLocation
import platform.CoreLocation.CLLocationCoordinate2DMake
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.kCLAuthorizationStatusDenied
import platform.CoreLocation.kCLAuthorizationStatusRestricted
import platform.Foundation.NSDate
import platform.Foundation.NSError
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalForeignApi::class)
class IosCurrentLocationRequestTest {
    @Test
    fun newestLocationIsDeliveredOnlyOnce() {
        val fixture = Fixture()
        fixture.request.locationManager(
            fixture.manager,
            didUpdateLocations = listOf(CLLocation(35.0, 128.0), CLLocation(37.5665, 126.9780)),
        )
        fixture.request.locationManager(fixture.manager, didFailWithError = LocationError)

        assertEquals(listOf(MapCoordinateUiModel(37.5665, 126.9780)), fixture.coordinates)
        assertEquals(0, fixture.failures)
    }

    @Test
    fun deniedAndRestrictedPermissionsReportUnavailableOnce() {
        for (status in listOf(kCLAuthorizationStatusDenied, kCLAuthorizationStatusRestricted)) {
            val fixture = Fixture()
            fixture.request.handleAuthorizationStatus(status)
            fixture.request.handleAuthorizationStatus(status)

            assertTrue(fixture.coordinates.isEmpty())
            assertEquals(1, fixture.failures)
        }
    }

    @Test
    fun emptyOrInvalidLocationReportsUnavailable() {
        val invalidLocation = CLLocation(
            coordinate = CLLocationCoordinate2DMake(37.5665, 126.9780),
            altitude = 0.0,
            horizontalAccuracy = -1.0,
            verticalAccuracy = -1.0,
            timestamp = NSDate(),
        )
        for (locations in listOf(emptyList(), listOf(invalidLocation))) {
            val fixture = Fixture()
            fixture.request.locationManager(fixture.manager, didUpdateLocations = locations)

            assertTrue(fixture.coordinates.isEmpty())
            assertEquals(1, fixture.failures)
        }
    }

    @Test
    fun failureDoesNotAllowLateLocationToMoveTheMap() {
        val fixture = Fixture()
        fixture.request.locationManager(fixture.manager, didFailWithError = LocationError)
        fixture.request.locationManager(
            fixture.manager,
            didUpdateLocations = listOf(CLLocation(37.5665, 126.9780)),
        )

        assertTrue(fixture.coordinates.isEmpty())
        assertEquals(1, fixture.failures)
    }

    @Test
    fun cancellingIgnoresBothLateSuccessAndFailure() {
        val fixture = Fixture()
        fixture.request.cancel()
        fixture.request.locationManager(
            fixture.manager,
            didUpdateLocations = listOf(CLLocation(37.5665, 126.9780)),
        )
        fixture.request.locationManager(fixture.manager, didFailWithError = LocationError)

        assertTrue(fixture.coordinates.isEmpty())
        assertEquals(0, fixture.failures)
    }

    private class Fixture {
        val manager = CLLocationManager()
        val coordinates = mutableListOf<MapCoordinateUiModel>()
        var failures = 0
        val request = IosCurrentLocationRequest(
            manager = manager,
            onLocationAvailable = coordinates::add,
            onLocationUnavailable = { failures++ },
        )
    }
}

@OptIn(ExperimentalForeignApi::class)
private val LocationError = NSError(domain = "CurrentLocationTest", code = 1, userInfo = null)

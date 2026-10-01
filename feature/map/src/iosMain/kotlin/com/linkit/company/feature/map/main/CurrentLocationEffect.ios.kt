package com.linkit.company.feature.map.main

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.CoreLocation.CLAuthorizationStatus
import platform.CoreLocation.CLLocation
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse
import platform.CoreLocation.kCLAuthorizationStatusNotDetermined
import platform.CoreLocation.kCLLocationAccuracyNearestTenMeters
import platform.Foundation.NSError
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class)
@Composable
internal actual fun CurrentLocationEffect(
    requestToken: Int,
    onLocationAvailable: (MapCoordinateUiModel) -> Unit,
    onLocationUnavailable: () -> Unit,
) {
    val currentOnLocationAvailable by rememberUpdatedState(onLocationAvailable)
    val currentOnLocationUnavailable by rememberUpdatedState(onLocationUnavailable)
    DisposableEffect(requestToken) {
        if (requestToken <= 0) return@DisposableEffect onDispose {}

        val request = IosCurrentLocationRequest(
            onLocationAvailable = { currentOnLocationAvailable(it) },
            onLocationUnavailable = { currentOnLocationUnavailable() },
        )
        request.start()
        // CLLocationManager.delegate is weak; retain the request until this effect is disposed.
        onDispose { request.cancel() }
    }
}

@OptIn(ExperimentalForeignApi::class)
internal class IosCurrentLocationRequest(
    private val manager: CLLocationManager = CLLocationManager(),
    private val onLocationAvailable: (MapCoordinateUiModel) -> Unit,
    private val onLocationUnavailable: () -> Unit,
) : NSObject(), CLLocationManagerDelegateProtocol {
    private var completed = false
    private var permissionRequested = false
    private var locationRequested = false

    fun start() {
        if (completed) return
        manager.delegate = this
        manager.desiredAccuracy = kCLLocationAccuracyNearestTenMeters
        handleAuthorizationStatus(manager.authorizationStatus)
    }

    override fun locationManagerDidChangeAuthorization(manager: CLLocationManager) {
        handleAuthorizationStatus(manager.authorizationStatus)
    }

    internal fun handleAuthorizationStatus(status: CLAuthorizationStatus) {
        if (completed) return
        when (status) {
            kCLAuthorizationStatusNotDetermined -> if (!permissionRequested) {
                permissionRequested = true
                manager.requestWhenInUseAuthorization()
            }
            kCLAuthorizationStatusAuthorizedAlways,
            kCLAuthorizationStatusAuthorizedWhenInUse -> if (!locationRequested) {
                locationRequested = true
                manager.requestLocation()
            }
            else -> finish(null)
        }
    }

    override fun locationManager(manager: CLLocationManager, didUpdateLocations: List<*>) {
        val location = didUpdateLocations.lastOrNull() as? CLLocation
        val coordinate = location
            ?.takeIf { it.horizontalAccuracy >= 0 }
            ?.coordinate
            ?.useContents { MapCoordinateUiModel(latitude, longitude) }
            ?.takeIf(MapCoordinateUiModel::hasValidCoordinate)
        finish(coordinate)
    }

    override fun locationManager(manager: CLLocationManager, didFailWithError: NSError) {
        finish(null)
    }

    private fun finish(coordinate: MapCoordinateUiModel?) {
        if (completed) return
        cancel()
        if (coordinate != null) onLocationAvailable(coordinate) else onLocationUnavailable()
    }

    fun cancel() {
        completed = true
        manager.delegate = null
        manager.stopUpdatingLocation()
    }
}

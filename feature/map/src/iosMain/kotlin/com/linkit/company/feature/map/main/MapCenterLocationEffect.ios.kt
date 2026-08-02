package com.linkit.company.feature.map.main

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreLocation.CLGeocoder
import platform.CoreLocation.CLLocation
import platform.CoreLocation.CLPlacemark
import platform.Foundation.NSLocale

@OptIn(ExperimentalForeignApi::class)
@Composable
internal actual fun MapCenterLocationEffect(
    coordinate: MapCoordinateUiModel,
    onLocationResolved: (coordinate: MapCoordinateUiModel, label: String) -> Unit,
) {
    val currentOnLocationResolved by rememberUpdatedState(onLocationResolved)

    DisposableEffect(coordinate.lat, coordinate.lng) {
        val geocoder = CLGeocoder()
        if (coordinate.hasValidCoordinate()) {
            geocoder.reverseGeocodeLocation(
                location = CLLocation(coordinate.lat, coordinate.lng),
                preferredLocale = NSLocale("ko_KR"),
            ) { placemarks, _ ->
                val placemark = placemarks?.firstOrNull() as? CLPlacemark
                val label = placemark?.let {
                    formatMapCenterLocationLabel(
                        country = it.country,
                        administrativeArea = it.administrativeArea,
                        locality = it.locality ?: it.subAdministrativeArea,
                    )
                }
                label?.let { currentOnLocationResolved(coordinate, it) }
            }
        }

        onDispose { geocoder.cancelGeocode() }
    }
}

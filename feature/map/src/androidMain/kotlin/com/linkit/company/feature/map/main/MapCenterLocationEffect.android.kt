package com.linkit.company.feature.map.main

import android.location.Geocoder
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
internal actual fun MapCenterLocationEffect(
    coordinate: MapCoordinateUiModel,
    onLocationResolved: (coordinate: MapCoordinateUiModel, label: String) -> Unit,
) {
    val currentOnLocationResolved by rememberUpdatedState(onLocationResolved)
    val context = LocalContext.current

    if (LocalInspectionMode.current) return

    LaunchedEffect(coordinate.lat, coordinate.lng) {
        if (!coordinate.hasValidCoordinate()) return@LaunchedEffect

        @Suppress("DEPRECATION")
        val address = withContext(Dispatchers.IO) {
            runCatching {
                Geocoder(context, Locale.KOREA)
                    .getFromLocation(coordinate.lat, coordinate.lng, 1)
                    ?.firstOrNull()
            }.getOrNull()
        }
        val label = address?.let {
            formatMapCenterLocationLabel(
                country = it.countryName,
                administrativeArea = it.adminArea,
                locality = it.locality ?: it.subAdminArea,
            )
        }

        label?.let { currentOnLocationResolved(coordinate, it) }
    }
}

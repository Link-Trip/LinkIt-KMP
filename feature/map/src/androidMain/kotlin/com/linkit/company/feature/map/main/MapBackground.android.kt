package com.linkit.company.feature.map.main

import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType as GoogleMapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState

private val Tokyo = LatLng(35.6762, 139.6503)

@Composable
internal actual fun PlatformMapBackground(mapType: MapType, modifier: Modifier) {
    val context = LocalContext.current
    val hasApiKey = remember(context) { context.hasMapsApiKey() }
    if (LocalInspectionMode.current || !hasApiKey) {
        StaticMapBackground(mapType, modifier)
        return
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(Tokyo, 12f)
    }
    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
        contentPadding = PaddingValues(bottom = 337.dp),
        properties = MapProperties(
            mapType = if (mapType == MapType.SATELLITE) {
                GoogleMapType.SATELLITE
            } else {
                GoogleMapType.NORMAL
            },
        ),
        uiSettings = MapUiSettings(
            compassEnabled = false,
            indoorLevelPickerEnabled = false,
            mapToolbarEnabled = false,
            myLocationButtonEnabled = false,
            // ponytail: enable gestures when the mock markers use geographic coordinates.
            rotationGesturesEnabled = false,
            scrollGesturesEnabled = false,
            tiltGesturesEnabled = false,
            zoomControlsEnabled = false,
            zoomGesturesEnabled = false,
        ),
    )
}

@Suppress("DEPRECATION")
private fun Context.hasMapsApiKey(): Boolean = runCatching {
    packageManager
        .getApplicationInfo(packageName, PackageManager.GET_META_DATA)
        .metaData
        ?.getString("com.google.android.geo.API_KEY")
        ?.let { it.isNotBlank() && it != "YOUR_API_KEY" }
        ?: false
}.getOrDefault(false)

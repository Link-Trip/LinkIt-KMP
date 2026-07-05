package com.linkit.company.feature.map.main

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
internal actual fun GoogleMapBackground(modifier: Modifier) {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(TokyoHarajuku, DefaultZoom)
    }

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
        properties = MapProperties(mapType = MapType.NORMAL),
        uiSettings = MapUiSettings(
            compassEnabled = false,
            mapToolbarEnabled = false,
            myLocationButtonEnabled = false,
            zoomControlsEnabled = false,
        ),
    )
}

private val TokyoHarajuku = LatLng(35.6717, 139.7030)
private const val DefaultZoom = 14.2f

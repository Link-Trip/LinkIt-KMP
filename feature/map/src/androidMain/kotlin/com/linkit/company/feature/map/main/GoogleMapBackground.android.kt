package com.linkit.company.feature.map.main

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MarkerComposable
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberUpdatedMarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
internal actual fun GoogleMapBackground(
    modifier: Modifier,
    markers: List<MainMapMarker>,
) {
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
    ) {
        markers.forEach { marker ->
            val position = LatLng(marker.latitude, marker.longitude)
            MarkerComposable(
                marker.id,
                marker.type,
                marker.title,
                marker.count.orEmpty(),
                state = rememberUpdatedMarkerState(position),
                contentDescription = marker.title,
                anchor = MarkerAnchor,
                title = marker.title,
                zIndex = marker.type.zIndex,
            ) {
                when (marker.type) {
                    MainMapMarkerType.Schedule -> ScheduleMarkerChip(text = marker.title)
                    MainMapMarkerType.Place -> PlaceMarker()
                    MainMapMarkerType.Count -> CountMarker(count = marker.count.orEmpty())
                }
            }
        }
    }
}

private val MainMapMarkerType.zIndex: Float
    get() = when (this) {
        MainMapMarkerType.Schedule -> 3f
        MainMapMarkerType.Count -> 2f
        MainMapMarkerType.Place -> 1f
    }

private val TokyoHarajuku = LatLng(35.6717, 139.7030)
private val MarkerAnchor = Offset(0.5f, 0.5f)
private const val DefaultZoom = 14.2f

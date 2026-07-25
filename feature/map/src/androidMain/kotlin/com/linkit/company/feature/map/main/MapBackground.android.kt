package com.linkit.company.feature.map.main

import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.Dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType as GoogleMapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MarkerComposable
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberUpdatedMarkerState
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter

private const val MinZoom = 3f
private const val MaxZoom = 21f

@Composable
internal actual fun PlatformMapBackground(
    mapType: MapType,
    modifier: Modifier,
    markers: List<MapMarkerUiModel>,
    initialCamera: MapCameraUiModel,
    contentPaddingBottom: Dp,
    currentLocation: MapCoordinateUiModel?,
    focusCurrentLocationRequest: Int,
    onMarkerClick: (MapMarkerUiModel) -> Unit,
    onCameraChanged: (MapCameraUiModel) -> Unit,
) {
    val context = LocalContext.current
    val hasApiKey = remember(context) { context.hasMapsApiKey() }
    if (LocalInspectionMode.current || !hasApiKey) {
        StaticMapBackground(
            mapType = mapType,
            modifier = modifier,
            markers = markers,
            initialCamera = initialCamera,
            contentPaddingBottom = contentPaddingBottom,
            onMarkerClick = onMarkerClick,
        )
        return
    }

    val safeInitialCamera = initialCamera.takeIf { it.center.hasValidCoordinate() } ?: DefaultMapCamera
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            safeInitialCamera.center.toLatLng(),
            safeInitialCamera.zoom.coerceIn(MinZoom, MaxZoom),
        )
    }
    val selectedMarker = markers.firstOrNull { it.selected && it.type == MapMarkerType.PLACE }
        ?: markers.firstOrNull(MapMarkerUiModel::selected)
    val currentOnCameraChanged by rememberUpdatedState(onCameraChanged)

    LaunchedEffect(selectedMarker?.id, selectedMarker?.lat, selectedMarker?.lng) {
        selectedMarker?.takeIf(MapMarkerUiModel::hasValidCoordinate)?.let { marker ->
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(marker.lat, marker.lng),
                    cameraPositionState.position.zoom.coerceIn(MinZoom, MaxZoom),
                ),
            )
        }
    }
    LaunchedEffect(focusCurrentLocationRequest, currentLocation) {
        currentLocation
            ?.takeIf(MapCoordinateUiModel::hasValidCoordinate)
            ?.takeIf { focusCurrentLocationRequest > 0 }
            ?.let { location ->
                cameraPositionState.animate(
                    CameraUpdateFactory.newLatLngZoom(
                        location.toLatLng(),
                        cameraPositionState.position.zoom.coerceIn(MinZoom, MaxZoom),
                    ),
                )
            }
    }
    LaunchedEffect(cameraPositionState) {
        snapshotFlow { cameraPositionState.isMoving }
            .distinctUntilChanged()
            .filter { isMoving -> !isMoving }
            .collect {
                val camera = cameraPositionState.position
                currentOnCameraChanged(
                    MapCameraUiModel(
                        center = MapCoordinateUiModel(
                            lat = camera.target.latitude,
                            lng = camera.target.longitude,
                        ),
                        zoom = camera.zoom.coerceIn(MinZoom, MaxZoom),
                    ),
                )
            }
    }

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
        contentPadding = PaddingValues(bottom = contentPaddingBottom),
        properties = MapProperties(
            mapType = if (mapType == MapType.SATELLITE) {
                GoogleMapType.SATELLITE
            } else {
                GoogleMapType.NORMAL
            },
            minZoomPreference = MinZoom,
            maxZoomPreference = MaxZoom,
        ),
        uiSettings = MapUiSettings(
            compassEnabled = false,
            indoorLevelPickerEnabled = false,
            mapToolbarEnabled = false,
            myLocationButtonEnabled = false,
            rotationGesturesEnabled = true,
            scrollGesturesEnabled = true,
            tiltGesturesEnabled = true,
            zoomControlsEnabled = false,
            zoomGesturesEnabled = true,
        ),
    ) {
        markers
            .filter(MapMarkerUiModel::hasValidCoordinate)
            .forEach { marker ->
                key(marker.type, marker.id) {
                    MarkerComposable(
                        marker.id,
                        marker.label,
                        marker.type,
                        marker.selected,
                        marker.thumbnail ?: Unit,
                        state = rememberUpdatedMarkerState(LatLng(marker.lat, marker.lng)),
                        contentDescription = marker.label,
                        tag = marker.id,
                        zIndex = if (marker.selected) 1f else 0f,
                        onClick = {
                            onMarkerClick(marker)
                            true
                        },
                    ) {
                        MapMarkerVisual(marker)
                    }
                }
            }
    }
}

private fun MapCoordinateUiModel.toLatLng(): LatLng = LatLng(lat, lng)

@Suppress("DEPRECATION")
private fun Context.hasMapsApiKey(): Boolean = runCatching {
    packageManager
        .getApplicationInfo(packageName, PackageManager.GET_META_DATA)
        .metaData
        ?.getString("com.google.android.geo.API_KEY")
        ?.let { it.isNotBlank() && it != "YOUR_API_KEY" }
        ?: false
}.getOrDefault(false)

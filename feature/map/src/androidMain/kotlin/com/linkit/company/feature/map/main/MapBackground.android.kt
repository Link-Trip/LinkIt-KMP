package com.linkit.company.feature.map.main

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType as GoogleMapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MarkerComposable
import com.google.maps.android.compose.Polygon
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberUpdatedMarkerState
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlin.math.abs

private const val MinZoom = 3f
private const val MaxZoom = 21f

@Composable
internal actual fun PlatformMapBackground(
    mapType: MapType,
    modifier: Modifier,
    markers: List<MapMarkerUiModel>,
    selectedArea: MapAreaUiModel?,
    initialCamera: MapCameraUiModel,
    contentPaddingBottom: Dp,
    currentLocation: MapCoordinateUiModel?,
    focusCurrentLocationRequest: Int,
    onMarkerClick: (MapMarkerUiModel) -> Unit,
    onCameraChanged: (MapCameraUiModel) -> Unit,
) {
    if (LocalInspectionMode.current) {
        MapInspectionPlaceholder(modifier)
        return
    }

    val safeInitialCamera = initialCamera.takeIf { it.center.hasValidCoordinate() } ?: DefaultMapCamera
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            safeInitialCamera.center.toLatLng(),
            safeInitialCamera.zoom.coerceIn(MinZoom, MaxZoom),
        )
    }
    val selectedPlaceMarker = markers.firstOrNull {
        it.selected && it.type == MapMarkerType.PLACE
    }
    val selectedScheduleBounds = markers.selectedScheduleCameraBounds()
    val areaStrokeWidth = with(LocalDensity.current) { 1.dp.toPx() }
    val cameraBoundsPadding = with(LocalDensity.current) { 48.dp.roundToPx() }
    val currentOnCameraChanged by rememberUpdatedState(onCameraChanged)
    var isMapLoaded by remember { mutableStateOf(false) }
    var hasAppliedCamera by remember { mutableStateOf(false) }

    LaunchedEffect(
        isMapLoaded,
        safeInitialCamera.center.lat,
        safeInitialCamera.center.lng,
        safeInitialCamera.zoom,
    ) {
        if (!isMapLoaded) return@LaunchedEffect
        val currentCamera = cameraPositionState.position
        if (
            abs(currentCamera.target.latitude - safeInitialCamera.center.lat) > 0.000_001 ||
            abs(currentCamera.target.longitude - safeInitialCamera.center.lng) > 0.000_001 ||
            abs(currentCamera.zoom - safeInitialCamera.zoom) > 0.01f
        ) {
            cameraPositionState.move(
                CameraUpdateFactory.newCameraPosition(
                    CameraPosition.fromLatLngZoom(
                        safeInitialCamera.center.toLatLng(),
                        safeInitialCamera.zoom.coerceIn(MinZoom, MaxZoom),
                    ),
                ),
            )
        }
        hasAppliedCamera = true
    }
    LaunchedEffect(
        isMapLoaded,
        selectedScheduleBounds,
        cameraBoundsPadding,
    ) {
        if (!isMapLoaded) return@LaunchedEffect
        val points = selectedScheduleBounds?.points.orEmpty()
        when {
            points.size == 1 -> cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(points.first().toLatLng(), 15f),
            )
            points.size > 1 -> {
                val bounds = LatLngBounds.builder().apply {
                    points.forEach { point -> include(point.toLatLng()) }
                }.build()
                cameraPositionState.animate(
                    CameraUpdateFactory.newLatLngBounds(bounds, cameraBoundsPadding),
                )
            }
        }
    }
    LaunchedEffect(
        selectedPlaceMarker?.id,
        selectedPlaceMarker?.lat,
        selectedPlaceMarker?.lng,
    ) {
        selectedPlaceMarker?.takeIf(MapMarkerUiModel::hasValidCoordinate)?.let { marker ->
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
            .filter { isMoving -> !isMoving && hasAppliedCamera }
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
        onMapLoaded = { isMapLoaded = true },
    ) {
        selectedArea
            ?.points
            ?.filter(MapCoordinateUiModel::hasValidCoordinate)
            ?.takeIf { it.size >= 3 }
            ?.let { points ->
                Polygon(
                    points = points.map(MapCoordinateUiModel::toLatLng),
                    fillColor = MapAreaFillColor,
                    strokeColor = MapAreaStrokeColor,
                    strokeWidth = areaStrokeWidth,
                    tag = selectedArea.id,
                )
            }
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

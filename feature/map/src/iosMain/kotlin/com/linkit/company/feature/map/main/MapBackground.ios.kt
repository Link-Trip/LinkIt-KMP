package com.linkit.company.feature.map.main

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp

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
    StaticMapBackground(
        mapType = mapType,
        modifier = modifier,
        markers = markers,
        initialCamera = initialCamera,
        contentPaddingBottom = contentPaddingBottom,
        onMarkerClick = onMarkerClick,
    )
}

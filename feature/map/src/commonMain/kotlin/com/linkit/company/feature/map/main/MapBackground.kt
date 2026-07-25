package com.linkit.company.feature.map.main

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.linkit.company.core.designsystem.foundation.color.token.PaletteTokens
import com.linkit.company.core.designsystem.theme.LinkItTheme
import linkitcompany.feature.map.generated.resources.Res
import linkitcompany.feature.map.generated.resources.map_background
import linkitcompany.feature.map.generated.resources.map_place_photo
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.tan

private const val StaticMapTileSize = 256.0
private const val StaticMapMinZoom = 0f
private const val StaticMapMaxZoom = 22f
private const val WebMercatorMaxLatitude = 85.05112878
private val StaticMarkerVisibilityMargin = 72.dp

internal enum class MapMarkerType {
    SCHEDULE,
    PLACE,
}

internal data class MapCoordinateUiModel(
    val lat: Double,
    val lng: Double,
)

internal data class MapCameraUiModel(
    val center: MapCoordinateUiModel,
    val zoom: Float,
)

internal data class MapMarkerUiModel(
    val id: String,
    val lat: Double,
    val lng: Double,
    val label: String,
    val type: MapMarkerType,
    val selected: Boolean = false,
    val thumbnail: DrawableResource? = null,
)

internal val DefaultMapCamera = MapCameraUiModel(
    center = MapCoordinateUiModel(lat = 37.5665, lng = 126.9780),
    zoom = 12f,
)

@Composable
internal expect fun PlatformMapBackground(
    mapType: MapType,
    modifier: Modifier,
    markers: List<MapMarkerUiModel> = emptyList(),
    initialCamera: MapCameraUiModel = DefaultMapCamera,
    contentPaddingBottom: Dp = 337.dp,
    currentLocation: MapCoordinateUiModel? = null,
    focusCurrentLocationRequest: Int = 0,
    onMarkerClick: (MapMarkerUiModel) -> Unit = {},
    onCameraChanged: (MapCameraUiModel) -> Unit = {},
)

@Composable
internal fun StaticMapBackground(
    mapType: MapType,
    modifier: Modifier,
    markers: List<MapMarkerUiModel> = emptyList(),
    initialCamera: MapCameraUiModel = DefaultMapCamera,
    contentPaddingBottom: Dp = 337.dp,
    onMarkerClick: (MapMarkerUiModel) -> Unit = {},
) {
    BoxWithConstraints(modifier.clipToBounds()) {
        Image(
            painter = painterResource(Res.drawable.map_background),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize(),
        )
        if (mapType == MapType.SATELLITE) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(LinkItTheme.color.semantic.label.neutral.copy(alpha = .28f)),
            )
        }
        StaticMapMarkers(
            markers = markers,
            initialCamera = initialCamera,
            contentPaddingBottom = contentPaddingBottom,
            onMarkerClick = onMarkerClick,
        )
    }
}

@Composable
private fun androidx.compose.foundation.layout.BoxWithConstraintsScope.StaticMapMarkers(
    markers: List<MapMarkerUiModel>,
    initialCamera: MapCameraUiModel,
    contentPaddingBottom: Dp,
    onMarkerClick: (MapMarkerUiModel) -> Unit,
) {
    val validMarkers = markers
        .filter(MapMarkerUiModel::hasValidCoordinate)
        .sortedBy(MapMarkerUiModel::selected)
    if (validMarkers.isEmpty()) return

    val safeCamera = initialCamera.takeIf { it.center.hasValidCoordinate() } ?: DefaultMapCamera
    val zoom = safeCamera.zoom.coerceIn(StaticMapMinZoom, StaticMapMaxZoom)
    val worldSize = StaticMapTileSize * 2.0.pow(zoom.toDouble())
    val cameraWorldPoint = safeCamera.center.toWebMercatorWorldPoint()
    val safeBottomPadding = contentPaddingBottom.coerceIn(0.dp, maxHeight)
    val availableHeight = maxHeight - safeBottomPadding

    validMarkers.forEach { marker ->
        val markerWorldPoint = MapCoordinateUiModel(marker.lat, marker.lng).toWebMercatorWorldPoint()
        val xOffset = (wrappedWorldDelta(markerWorldPoint.x, cameraWorldPoint.x) * worldSize)
            .toFloat()
            .dp
        val yOffset = ((markerWorldPoint.y - cameraWorldPoint.y) * worldSize)
            .toFloat()
            .dp
        val projectedX = maxWidth / 2 + xOffset
        val projectedY = availableHeight / 2 + yOffset
        val isVisible = projectedX >= -StaticMarkerVisibilityMargin &&
            projectedX <= maxWidth + StaticMarkerVisibilityMargin &&
            projectedY >= -StaticMarkerVisibilityMargin &&
            projectedY <= availableHeight + StaticMarkerVisibilityMargin
        if (!isVisible) return@forEach

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(
                    x = xOffset,
                    y = yOffset - safeBottomPadding / 2,
                )
                .clickable(
                    onClickLabel = marker.label,
                    onClick = { onMarkerClick(marker) },
                ),
        ) {
            MapMarkerVisual(marker)
        }
    }
}

private data class WebMercatorWorldPoint(
    val x: Double,
    val y: Double,
)

private fun MapCoordinateUiModel.toWebMercatorWorldPoint(): WebMercatorWorldPoint {
    val clampedLatitude = lat.coerceIn(-WebMercatorMaxLatitude, WebMercatorMaxLatitude)
    val latitudeRadians = clampedLatitude * PI / 180.0
    return WebMercatorWorldPoint(
        x = (lng + 180.0) / 360.0,
        y = (1.0 - ln(tan(latitudeRadians) + 1.0 / cos(latitudeRadians)) / PI) / 2.0,
    )
}

private fun wrappedWorldDelta(value: Double, center: Double): Double {
    val delta = value - center
    return when {
        delta > .5 -> delta - 1.0
        delta < -.5 -> delta + 1.0
        else -> delta
    }
}

@Composable
internal fun MapMarkerVisual(marker: MapMarkerUiModel, modifier: Modifier = Modifier) {
    when (marker.type) {
        MapMarkerType.SCHEDULE -> ScheduleMapMarkerVisual(marker, modifier)
        MapMarkerType.PLACE -> PlaceMapMarkerVisual(marker, modifier)
    }
}

@Composable
private fun ScheduleMapMarkerVisual(marker: MapMarkerUiModel, modifier: Modifier) {
    val shape = if (marker.selected) LinkItTheme.shape.xl else RoundedCornerShape(10.dp)
    Text(
        text = marker.label,
        style = if (marker.selected) {
            LinkItTheme.typography.body2NormalSemibold
        } else {
            LinkItTheme.typography.label2Medium
        },
        color = PaletteTokens.PingoNeutral700,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier
            .widthIn(max = 120.dp)
            .shadow(5.dp, shape)
            .clip(shape)
            .background(
                if (marker.selected) PaletteTokens.PaleBlue95
                else LinkItTheme.color.semantic.background.elevated.normal,
            )
            .border(
                width = 1.dp,
                color = if (marker.selected) {
                    PaletteTokens.PaleBlue70
                } else {
                    PaletteTokens.PingoNeutral100
                },
                shape = shape,
            )
            .padding(horizontal = 10.dp, vertical = 8.dp),
    )
}

@Composable
private fun PlaceMapMarkerVisual(marker: MapMarkerUiModel, modifier: Modifier) {
    val markerColor = if (marker.selected) {
        PaletteTokens.PingoMapMarkerSelected
    } else {
        PaletteTokens.PingoMapMarkerOutline
    }
    val markerSize = if (marker.selected) 44.dp else 32.dp
    val pointerSize = if (marker.selected) 12.dp else 10.dp
    val shape = RoundedCornerShape(if (marker.selected) 6.dp else 4.dp)
    Column(
        modifier = modifier.width(markerSize),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(markerSize)
                .shadow(3.dp, shape)
                .clip(shape)
                .background(markerColor)
                .padding(2.dp),
        ) {
            Image(
                painter = painterResource(marker.thumbnail ?: Res.drawable.map_place_photo),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(if (marker.selected) 4.dp else 2.dp)),
            )
        }
        Canvas(
            Modifier
                .size(pointerSize),
        ) {
            val pointer = Path().apply {
                moveTo(0f, 0f)
                lineTo(size.width, 0f)
                lineTo(size.width / 2f, size.height)
                close()
            }
            drawPath(pointer, markerColor)
        }
    }
}

internal fun MapCoordinateUiModel.hasValidCoordinate(): Boolean =
    lat.isFinite() && lng.isFinite() && lat in -90.0..90.0 && lng in -180.0..180.0

internal fun MapMarkerUiModel.hasValidCoordinate(): Boolean =
    MapCoordinateUiModel(lat, lng).hasValidCoordinate()

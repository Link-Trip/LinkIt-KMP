package com.linkit.company.feature.map.main

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.interop.UIKitView
import androidx.compose.ui.unit.Dp
import com.linkit.company.core.designsystem.foundation.color.token.PaletteTokens
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.get
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.useContents
import platform.CoreLocation.CLLocationCoordinate2D
import platform.CoreLocation.CLLocationCoordinate2DMake
import platform.MapKit.MKAnnotationProtocol
import platform.MapKit.MKAnnotationView
import platform.MapKit.MKCoordinateRegionMakeWithDistance
import platform.MapKit.MKMapPointForCoordinate
import platform.MapKit.MKMapRectInset
import platform.MapKit.MKMapRectMake
import platform.MapKit.MKMapRectUnion
import platform.MapKit.MKMapTypeSatellite
import platform.MapKit.MKMapTypeStandard
import platform.MapKit.MKMapView
import platform.MapKit.MKMapViewDelegateProtocol
import platform.MapKit.MKMarkerAnnotationView
import platform.MapKit.MKOverlayProtocol
import platform.MapKit.MKOverlayRenderer
import platform.MapKit.MKPointAnnotation
import platform.MapKit.MKPolygon
import platform.MapKit.MKPolygonRenderer
import platform.MapKit.addOverlay
import platform.MapKit.overlays
import platform.MapKit.removeOverlays
import platform.UIKit.UIColor
import platform.UIKit.UIEdgeInsetsMake
import platform.darwin.NSObject
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.log2
import kotlin.math.max
import kotlin.math.pow

private const val MinZoom = 3f
private const val MaxZoom = 21f
private const val MapWidthInTiles = 4.0
private const val EarthCircumferenceMeters = 40_075_016.686
private const val MinimumMapRectPadding = 2_500.0

@OptIn(ExperimentalForeignApi::class)
@Suppress("DEPRECATION")
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
    val delegate = remember { PingoMapViewDelegate() }
    delegate.onMarkerClick = onMarkerClick
    delegate.onCameraChanged = onCameraChanged

    UIKitView(
        factory = {
            MKMapView().apply {
                this.delegate = delegate
                setRotateEnabled(true)
                setScrollEnabled(true)
                setZoomEnabled(true)
            }
        },
        modifier = modifier,
        update = { mapView ->
            mapView.mapType = when (mapType) {
                MapType.DEFAULT -> MKMapTypeStandard
                MapType.SATELLITE -> MKMapTypeSatellite
            }
            mapView.layoutMargins = UIEdgeInsetsMake(
                top = 0.0,
                left = 0.0,
                bottom = contentPaddingBottom.value.toDouble(),
                right = 0.0,
            )
            mapView.showsUserLocation = currentLocation != null
            mapView.replaceMarkers(markers, delegate)
            mapView.replaceArea(selectedArea)

            val selectedPlaceMarker = markers.firstOrNull {
                it.selected && it.type == MapMarkerType.PLACE
            }
            val selectedScheduleBounds = markers.selectedScheduleCameraBounds()
            when {
                currentLocation?.hasValidCoordinate() == true &&
                    focusCurrentLocationRequest > delegate.lastLocationFocusRequest -> {
                    delegate.lastLocationFocusRequest = focusCurrentLocationRequest
                    delegate.lastScheduleBoundsId = selectedScheduleBounds?.id
                    delegate.lastSelectedPlaceId = selectedPlaceMarker?.id
                    mapView.setRegion(
                        currentLocation.toMapRegion(initialCamera.zoom),
                        animated = true,
                    )
                }
                selectedScheduleBounds != null -> {
                    if (selectedScheduleBounds.id != delegate.lastScheduleBoundsId) {
                        delegate.lastScheduleBoundsId = selectedScheduleBounds.id
                        delegate.lastSelectedPlaceId = null
                        mapView.fitCoordinates(
                            points = selectedScheduleBounds.points,
                            contentPaddingBottom = contentPaddingBottom,
                        )
                    }
                }
                selectedPlaceMarker != null -> {
                    delegate.lastScheduleBoundsId = null
                    if (selectedPlaceMarker.id != delegate.lastSelectedPlaceId) {
                        delegate.lastSelectedPlaceId = selectedPlaceMarker.id
                        mapView.setRegion(
                            MapCoordinateUiModel(
                                selectedPlaceMarker.lat,
                                selectedPlaceMarker.lng,
                            ).toMapRegion(initialCamera.zoom),
                            animated = true,
                        )
                    }
                }
                else -> {
                    delegate.lastScheduleBoundsId = null
                    delegate.lastSelectedPlaceId = null
                    val safeTarget = initialCamera.center
                        .takeIf(MapCoordinateUiModel::hasValidCoordinate)
                        ?: DefaultMapCamera.center
                    mapView.setRegion(
                        safeTarget.toMapRegion(initialCamera.zoom),
                        animated = false,
                    )
                }
            }
        },
    )
}

@OptIn(ExperimentalForeignApi::class)
private class PingoMapViewDelegate : NSObject(), MKMapViewDelegateProtocol {
    var onMarkerClick: (MapMarkerUiModel) -> Unit = {}
    var onCameraChanged: (MapCameraUiModel) -> Unit = {}
    var lastScheduleBoundsId: String? = null
    var lastSelectedPlaceId: String? = null
    var lastLocationFocusRequest: Int = 0
    val markersByAnnotation = mutableMapOf<MKAnnotationProtocol, MapMarkerUiModel>()

    override fun mapView(
        mapView: MKMapView,
        viewForAnnotation: MKAnnotationProtocol,
    ): MKAnnotationView? {
        val marker = markersByAnnotation[viewForAnnotation] ?: return null
        return MKMarkerAnnotationView(viewForAnnotation, "pingo-marker").apply {
            markerTintColor = when {
                marker.selected -> PaletteTokens.PingoMapMarkerSelected.toUIColor()
                marker.type == MapMarkerType.SCHEDULE ->
                    PaletteTokens.PingoMapSelectionBorder.toUIColor()
                else -> PaletteTokens.PingoMapMarkerOutline.toUIColor()
            }
            glyphText = if (marker.type == MapMarkerType.SCHEDULE) {
                marker.label.take(1)
            } else {
                null
            }
            canShowCallout = true
        }
    }

    override fun mapView(
        mapView: MKMapView,
        didSelectAnnotationView: MKAnnotationView,
    ) {
        didSelectAnnotationView.annotation
            ?.let(markersByAnnotation::get)
            ?.let(onMarkerClick)
    }

    override fun mapView(
        mapView: MKMapView,
        rendererForOverlay: MKOverlayProtocol,
    ): MKOverlayRenderer {
        if (rendererForOverlay !is MKPolygon) {
            return MKOverlayRenderer(rendererForOverlay)
        }
        return MKPolygonRenderer(rendererForOverlay).apply {
            fillColor = MapAreaFillColor.toUIColor()
            strokeColor = MapAreaStrokeColor.toUIColor()
            lineWidth = 1.0
        }
    }

    override fun mapView(
        mapView: MKMapView,
        regionDidChangeAnimated: Boolean,
    ) {
        val camera = mapView.region.useContents {
            val latitude = center.latitude
            val longitude = center.longitude
            val longitudeDelta = span.longitudeDelta.coerceAtLeast(0.000_001)
            MapCameraUiModel(
                center = MapCoordinateUiModel(latitude, longitude),
                zoom = (log2(360.0 / longitudeDelta) - 2.0)
                    .toFloat()
                    .coerceIn(MinZoom, MaxZoom),
            )
        }
        onCameraChanged(camera)
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun MKMapView.replaceMarkers(
    markers: List<MapMarkerUiModel>,
    delegate: PingoMapViewDelegate,
) {
    val oldAnnotations = annotations.filterIsInstance<MKPointAnnotation>()
    if (oldAnnotations.isNotEmpty()) removeAnnotations(oldAnnotations)
    delegate.markersByAnnotation.clear()

    markers
        .filter(MapMarkerUiModel::hasValidCoordinate)
        .forEach { marker ->
            val annotation = MKPointAnnotation().apply {
                setCoordinate(CLLocationCoordinate2DMake(marker.lat, marker.lng))
                setTitle(marker.label)
            }
            delegate.markersByAnnotation[annotation] = marker
            addAnnotation(annotation)
        }
}

@OptIn(ExperimentalForeignApi::class)
private fun MKMapView.replaceArea(area: MapAreaUiModel?) {
    if (overlays.isNotEmpty()) removeOverlays(overlays)
    area?.toPolygon()?.let(::addOverlay)
}

@OptIn(ExperimentalForeignApi::class)
private fun MKMapView.fitCoordinates(
    points: List<MapCoordinateUiModel>,
    contentPaddingBottom: Dp,
) {
    val validPoints = points.filter(MapCoordinateUiModel::hasValidCoordinate)
    if (validPoints.isEmpty()) return

    var bounds = validPoints
        .map { point ->
            MKMapPointForCoordinate(
                CLLocationCoordinate2DMake(point.lat, point.lng),
            ).useContents { x to y }
        }
        .map { (x, y) -> MKMapRectMake(x, y, 0.0, 0.0) }
        .reduce(::MKMapRectUnion)
    val (width, height) = bounds.useContents { size.width to size.height }
    bounds = MKMapRectInset(
        rect = bounds,
        dx = -max(width * 0.05, MinimumMapRectPadding),
        dy = -max(height * 0.05, MinimumMapRectPadding),
    )
    setVisibleMapRect(
        mapRect = bounds,
        edgePadding = UIEdgeInsetsMake(
            top = 48.0,
            left = 48.0,
            bottom = contentPaddingBottom.value.toDouble() + 48.0,
            right = 48.0,
        ),
        animated = true,
    )
}

@OptIn(ExperimentalForeignApi::class)
private fun MapAreaUiModel.toPolygon(): MKPolygon? {
    val validPoints = points.filter(MapCoordinateUiModel::hasValidCoordinate)
    if (validPoints.size < 3) return null
    return memScoped {
        val coordinates = allocArray<CLLocationCoordinate2D>(validPoints.size)
        validPoints.forEachIndexed { index, point ->
            coordinates[index].latitude = point.lat
            coordinates[index].longitude = point.lng
        }
        MKPolygon.polygonWithCoordinates(
            coords = coordinates,
            count = validPoints.size.toULong(),
        )
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun MapCoordinateUiModel.toMapRegion(zoom: Float) =
    MKCoordinateRegionMakeWithDistance(
        centerCoordinate = CLLocationCoordinate2DMake(lat, lng),
        latitudinalMeters = visibleMetersAtZoom(zoom),
        longitudinalMeters = visibleMetersAtZoom(zoom),
    )

private fun MapCoordinateUiModel.visibleMetersAtZoom(zoom: Float): Double =
    (
        EarthCircumferenceMeters *
            cos(lat * PI / 180.0) /
            2.0.pow(zoom.coerceIn(MinZoom, MaxZoom).toDouble()) *
            MapWidthInTiles
        ).coerceIn(250.0, 20_000_000.0)

@OptIn(ExperimentalForeignApi::class)
private fun Color.toUIColor(): UIColor = UIColor.colorWithRed(
    red = red.toDouble(),
    green = green.toDouble(),
    blue = blue.toDouble(),
    alpha = alpha.toDouble(),
)

package com.linkit.company.feature.map.main

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.AnchoredDraggableDefaults
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.dialog
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.linkit.company.core.designsystem.component.badge.BadgeColor
import com.linkit.company.core.designsystem.component.badge.BadgeSize
import com.linkit.company.core.designsystem.component.badge.LinkItBadge
import com.linkit.company.core.designsystem.component.button.ButtonColor
import com.linkit.company.core.designsystem.component.button.ButtonColors
import com.linkit.company.core.designsystem.component.button.ButtonSize
import com.linkit.company.core.designsystem.component.button.ButtonVariant
import com.linkit.company.core.designsystem.component.button.LinkItButton
import com.linkit.company.core.designsystem.component.menu.LinkItMenuItem
import com.linkit.company.core.designsystem.component.menu.MenuDefaults as LinkItMenuDefaults
import com.linkit.company.core.designsystem.component.menu.MenuItemPadding
import com.linkit.company.core.designsystem.component.popup.LinkItToast
import com.linkit.company.core.designsystem.component.popup.ToastVariant
import com.linkit.company.core.designsystem.component.popup.dialog.DialogDefaults
import com.linkit.company.core.designsystem.component.popup.dialog.LinkItDialog
import com.linkit.company.core.designsystem.foundation.color.token.PaletteTokens
import com.linkit.company.core.designsystem.foundation.icon.LinkItIcon
import com.linkit.company.core.designsystem.foundation.typography.rememberNanumSquareFontFamily
import com.linkit.company.core.designsystem.theme.LinkItTheme
import dev.zacsweers.metrox.viewmodel.metroViewModel
import kotlinx.coroutines.delay
import linkitcompany.feature.map.generated.resources.Res
import linkitcompany.feature.map.generated.resources.map_calendar
import linkitcompany.feature.map.generated.resources.map_empty_schedule_bubble
import linkitcompany.feature.map.generated.resources.map_empty_schedule_character
import linkitcompany.feature.map.generated.resources.map_filter_category
import linkitcompany.feature.map.generated.resources.map_filter_globe
import linkitcompany.feature.map.generated.resources.map_filter_money
import linkitcompany.feature.map.generated.resources.map_place_photo
import linkitcompany.feature.map.generated.resources.map_schedule_thumbnail
import linkitcompany.feature.map.generated.resources.map_selected_thumb_1
import linkitcompany.feature.map.generated.resources.map_selected_thumb_2
import linkitcompany.feature.map.generated.resources.map_selected_thumb_3
import linkitcompany.feature.map.generated.resources.map_selected_thumb_4
import linkitcompany.feature.map.generated.resources.map_selected_thumb_5
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import kotlin.math.roundToInt

@Composable
fun MapScreen(
    onOpenSchedule: (scheduleId: String, title: String, focusedPlaceId: String?) -> Unit,
    navigateToScheduleEdit: () -> Unit,
    navigateToScheduleManual: () -> Unit = {},
    onOpenPlaceDetail: (MapPlaceUiModel) -> Unit = {},
    onOpenStorage: () -> Unit = {},
    onOpenMyPage: () -> Unit = {},
    onPlaceSelectionChanged: (Boolean) -> Unit = {},
    viewModel: MapViewModel = metroViewModel(),
) {
    val debugMapData = rememberMapDebugData()
    LaunchedEffect(viewModel, debugMapData) {
        debugMapData?.let(viewModel::useDebugMapData)
    }
    val uiState by viewModel.uiState.collectAsState()
    val latestOnPlaceSelectionChanged = rememberUpdatedState(onPlaceSelectionChanged)
    LaunchedEffect(uiState.selection) {
        latestOnPlaceSelectionChanged.value(uiState.selection == MapSelection.PLACE)
    }
    MapContent(
        uiState = uiState,
        onIntent = viewModel::onIntent,
        onOpenSchedule = onOpenSchedule,
        onOpenPlaceDetail = onOpenPlaceDetail,
        onCreateFromVideo = navigateToScheduleEdit,
        onCreateFromStorage = onOpenStorage,
        onCreateManually = navigateToScheduleManual,
        onOpenMyPage = onOpenMyPage,
    )
}

@Composable
fun MapContent(
    uiState: MapUiState,
    onIntent: (MapIntent) -> Unit,
    onOpenSchedule: (scheduleId: String, title: String, focusedPlaceId: String?) -> Unit = { _, _, _ -> },
    onOpenPlaceDetail: (MapPlaceUiModel) -> Unit = {},
    onCreateFromVideo: () -> Unit = {},
    onCreateFromStorage: () -> Unit = {},
    onCreateManually: () -> Unit = {},
    onOpenMyPage: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val mapCenter = MapCoordinateUiModel(uiState.cameraLatitude, uiState.cameraLongitude)
    CurrentLocationEffect(
        requestToken = uiState.locationRequestToken,
        onLocationAvailable = { location ->
            onIntent(MapIntent.CurrentLocationResolved(location.lat, location.lng))
        },
        onLocationUnavailable = {
            onIntent(MapIntent.CurrentLocationUnavailable("위치 권한 또는 위치 서비스를 확인해 주세요."))
        },
    )
    LaunchedEffect(uiState.scheduleActionFeedback?.id) {
        if (uiState.scheduleActionFeedback != null) {
            delay(ScheduleActionFeedbackDurationMillis)
            onIntent(MapIntent.DismissScheduleActionFeedback)
        }
    }
    MapCenterLocationEffect(
        coordinate = mapCenter,
        onLocationResolved = { coordinate, label ->
            onIntent(
                MapIntent.MapCenterLocationResolved(
                    latitude = coordinate.lat,
                    longitude = coordinate.lng,
                    label = label,
                ),
            )
        },
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(LinkItTheme.color.semantic.background.normal.alternative),
    ) {
        MapCanvas(uiState = uiState, onIntent = onIntent)
        MapTopActions(
            onOpenMyPage = onOpenMyPage,
            onToggleMapType = { onIntent(MapIntent.ToggleMapType) },
        )

        if (uiState.selection == MapSelection.PLACE) {
            uiState.selectedSchedule?.let { schedule ->
                SelectedScheduleMapMarker(schedule)
            }
        }

        uiState.locationMessage?.let { message ->
            MapNotice(
                message = message,
                topPadding = if (uiState.selection == MapSelection.PLACE) 68.dp else 20.dp,
            )
        }

        if (uiState.selection != MapSelection.PLACE) {
            MapBottomSheetHost(
                uiState = uiState,
                onIntent = onIntent,
                onOpenSchedule = onOpenSchedule,
                onCreateFromVideo = onCreateFromVideo,
                onCreateFromStorage = onCreateFromStorage,
                onCreateManually = onCreateManually,
            )
        } else {
            val schedule = uiState.selectedSchedule
            val place = uiState.selectedPlace
            if (schedule != null && place != null) {
                PlaceInformationCard(
                    place = place,
                    canShowPrevious = uiState.canShowPreviousPlace,
                    canShowNext = uiState.canShowNextPlace,
                    onPrevious = { onIntent(MapIntent.ShowPreviousPlace) },
                    onNext = { onIntent(MapIntent.ShowNextPlace) },
                    onClose = { onIntent(MapIntent.ClosePlace) },
                    onViewInSchedule = {
                        onOpenSchedule(schedule.id, schedule.title, place.placeId)
                    },
                    onOpenPlaceDetail = { onOpenPlaceDetail(place) },
                )
            }
        }

        if (uiState.isComingSoonDialogVisible) {
            LinkItDialog(
                title = "해당 기능은\n곧 출시 예정이에요!",
                description = "조금만 기다려 주세요",
                confirmText = "확인",
                onConfirmClick = { onIntent(MapIntent.DismissComingSoonDialog) },
                onDismissRequest = { onIntent(MapIntent.DismissComingSoonDialog) },
            )
        }

        uiState.scheduleActionFeedback?.let { feedback ->
            LinkItToast(
                text = feedback.message,
                variant = if (feedback.type == MapScheduleActionFeedbackType.SUCCESS) {
                    ToastVariant.Positive
                } else {
                    ToastVariant.Negative
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 20.dp, vertical = 20.dp)
                    .fillMaxWidth()
                    .testTag("map-schedule-action-feedback"),
            )
        }

        when (uiState.scheduleDialog) {
            MapScheduleDialog.RENAME -> ScheduleRenameDialog(
                value = uiState.scheduleNameDraft,
                isLoading = uiState.isScheduleActionInProgress,
                onValueChange = { onIntent(MapIntent.UpdateScheduleName(it)) },
                onConfirm = { onIntent(MapIntent.ConfirmScheduleRename) },
                onDismiss = { onIntent(MapIntent.DismissScheduleDialog) },
            )
            MapScheduleDialog.DELETE -> ScheduleDeleteDialog(
                isLoading = uiState.isScheduleActionInProgress,
                onConfirm = { onIntent(MapIntent.ConfirmScheduleDelete) },
                onDismiss = { onIntent(MapIntent.DismissScheduleDialog) },
            )
            null -> Unit
        }

    }
}

@Composable
private fun BoxScope.MapCanvas(
    uiState: MapUiState,
    onIntent: (MapIntent) -> Unit,
) {
    val markers = uiState.toMapMarkers()
    val selectedArea = uiState.selectedSchedule?.toMapAreaUiModel()
    val camera = MapCameraUiModel(
        center = MapCoordinateUiModel(uiState.cameraLatitude, uiState.cameraLongitude),
        zoom = uiState.cameraZoom,
    )
    val currentLocation = if (
        uiState.currentLocationLatitude != null && uiState.currentLocationLongitude != null
    ) {
        MapCoordinateUiModel(uiState.currentLocationLatitude, uiState.currentLocationLongitude)
    } else {
        null
    }

    PlatformMapBackground(
        mapType = uiState.mapType,
        modifier = Modifier.fillMaxSize(),
        markers = markers,
        selectedArea = selectedArea,
        initialCamera = camera,
        contentPaddingBottom = when (uiState.selection) {
            MapSelection.NONE -> 337.dp
            MapSelection.SCHEDULE -> 189.dp
            MapSelection.PLACE -> 233.dp
        },
        currentLocation = currentLocation,
        focusCurrentLocationRequest = uiState.focusCurrentLocationRequest,
        onMarkerClick = { marker ->
            when (marker.type) {
                MapMarkerType.SCHEDULE -> onIntent(MapIntent.SelectSchedule(marker.id))
                MapMarkerType.PLACE -> uiState.schedules
                    .firstOrNull { schedule ->
                        schedule.places.any { it.markerId == marker.id }
                    }
                    ?.let { schedule ->
                        onIntent(MapIntent.SelectPlace(schedule.id, marker.id))
                    }
            }
        },
        onCameraChanged = { changedCamera ->
            onIntent(
                MapIntent.CameraChanged(
                    latitude = changedCamera.center.lat,
                    longitude = changedCamera.center.lng,
                    zoom = changedCamera.zoom,
                ),
            )
        },
    )
}

internal fun MapScheduleUiModel.toMapAreaUiModel(): MapAreaUiModel? {
    val areaPoints = places
        .map { place -> MapCoordinateUiModel(place.latitude, place.longitude) }
        .convexHull()
    return areaPoints
        .takeIf { it.size >= 3 }
        ?.let { points -> MapAreaUiModel(id = id, points = points) }
}

internal fun Iterable<MapCoordinateUiModel>.convexHull(): List<MapCoordinateUiModel> {
    val sortedPoints = filter(MapCoordinateUiModel::hasValidCoordinate)
        .distinct()
        .sortedWith(compareBy(MapCoordinateUiModel::lng).thenBy(MapCoordinateUiModel::lat))
    if (sortedPoints.size <= 2) return sortedPoints

    fun cross(
        origin: MapCoordinateUiModel,
        first: MapCoordinateUiModel,
        second: MapCoordinateUiModel,
    ): Double = (first.lng - origin.lng) * (second.lat - origin.lat) -
        (first.lat - origin.lat) * (second.lng - origin.lng)

    fun buildHalf(points: Iterable<MapCoordinateUiModel>): List<MapCoordinateUiModel> =
        buildList {
            points.forEach { point ->
                while (size >= 2 && cross(this[size - 2], this[size - 1], point) <= 0.0) {
                    removeAt(lastIndex)
                }
                add(point)
            }
        }

    val lower = buildHalf(sortedPoints)
    val upper = buildHalf(sortedPoints.asReversed())
    return lower.dropLast(1) + upper.dropLast(1)
}

internal fun MapUiState.toMapMarkers(): List<MapMarkerUiModel> = buildList {
    filteredSchedules.forEach { schedule ->
        val latitude = schedule.centerLatitude
        val longitude = schedule.centerLongitude
        val isSelectedPlaceSchedule =
            selection == MapSelection.PLACE && selectedScheduleId == schedule.id
        if (latitude != null && longitude != null && !isSelectedPlaceSchedule) {
            add(
                MapMarkerUiModel(
                    id = schedule.id,
                    lat = latitude,
                    lng = longitude,
                    label = schedule.title,
                    type = MapMarkerType.SCHEDULE,
                    selected = selectedScheduleId == schedule.id,
                ),
            )
        }
    }

    selectedSchedule?.places?.forEachIndexed { index, place ->
        add(
            MapMarkerUiModel(
                id = place.markerId,
                lat = place.latitude,
                lng = place.longitude,
                label = place.name,
                type = MapMarkerType.PLACE,
                selected = selectedPlaceMarkerId == place.markerId,
                thumbnail = placeMarkerThumbnail(index),
            ),
        )
    }
}

private fun placeMarkerThumbnail(index: Int): DrawableResource = when (index % 5) {
    0 -> Res.drawable.map_selected_thumb_1
    1 -> Res.drawable.map_selected_thumb_2
    2 -> Res.drawable.map_selected_thumb_3
    3 -> Res.drawable.map_selected_thumb_4
    else -> Res.drawable.map_selected_thumb_5
}

@Composable
private fun BoxScope.MapTopActions(
    onOpenMyPage: () -> Unit,
    onToggleMapType: () -> Unit,
) {
    Column(
        modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(top = 20.dp, end = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        MapTopAction(LinkItIcon.Communication.PersonFill, "마이페이지", onOpenMyPage)
        MapTopAction(LinkItIcon.Location.Map, "지도 종류 변경", onToggleMapType)
    }
}

@Composable
private fun MapTopAction(
    icon: ImageVector,
    description: String,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(10.dp)
    Box(
        modifier = Modifier
            .size(40.dp)
            .shadow(2.dp, shape)
            .clip(shape)
            .background(LinkItTheme.color.semantic.background.elevated.normal)
            .border(1.dp, LinkItTheme.color.semantic.line.normal.neutral, shape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = description,
            tint = LinkItTheme.color.semantic.label.strong,
            modifier = Modifier.size(22.dp),
        )
    }
}

@Composable
private fun BoxScope.MapNotice(message: String, topPadding: androidx.compose.ui.unit.Dp) {
    Text(
        text = message,
        style = LinkItTheme.typography.caption1Medium,
        color = LinkItTheme.color.semantic.static.white,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .align(Alignment.TopCenter)
            .padding(top = topPadding, start = 64.dp, end = 64.dp)
            .shadow(2.dp, RoundedCornerShape(999.dp))
            .clip(RoundedCornerShape(999.dp))
            .background(PaletteTokens.PingoNeutral700)
            .padding(horizontal = 14.dp, vertical = 9.dp),
    )
}

@Composable
private fun BoxScope.SelectedScheduleMapMarker(schedule: MapScheduleUiModel) {
    val latitude = schedule.centerLatitude ?: MapUiState.DefaultLatitude
    val longitude = schedule.centerLongitude ?: MapUiState.DefaultLongitude
    Box(
        modifier = Modifier
            .align(Alignment.TopCenter)
            .padding(top = 12.dp, start = 72.dp, end = 72.dp)
            .testTag("map-selected-schedule-marker"),
    ) {
        MapMarkerVisual(
            marker = MapMarkerUiModel(
                id = schedule.id,
                lat = latitude,
                lng = longitude,
                label = schedule.title,
                type = MapMarkerType.SCHEDULE,
                selected = true,
            ),
        )
    }
}

@Composable
private fun BoxScope.MapBottomSheetHost(
    uiState: MapUiState,
    onIntent: (MapIntent) -> Unit,
    onOpenSchedule: (scheduleId: String, title: String, focusedPlaceId: String?) -> Unit,
    onCreateFromVideo: () -> Unit,
    onCreateFromStorage: () -> Unit,
    onCreateManually: () -> Unit,
) {
    val selectedSchedule = uiState.selectedSchedule
    val content = if (uiState.selection == MapSelection.SCHEDULE && selectedSchedule != null) {
        MapSheetContent.TravelPreview
    } else {
        MapSheetContent.SavedSchedules
    }
    val draggableState = remember { AnchoredDraggableState(MapSheetAnchor.Resting) }
    var lastSavedAnchor by remember { mutableStateOf(MapSheetAnchor.Resting) }
    var previousContent by remember { mutableStateOf<MapSheetContent?>(null) }

    BoxWithConstraints(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .fillMaxSize()
            .clipToBounds(),
    ) {
        val density = LocalDensity.current
        val containerHeightPx = constraints.maxHeight.toFloat()
        val locationPillHeightPx = with(density) { MapLocationPillHeight.toPx() }
        val collapsedSurfaceHeightPx = with(density) { MapSheetCollapsedSurfaceHeight.toPx() }
        val restingVisibleHeightPx = with(density) {
            when (content) {
                MapSheetContent.SavedSchedules -> containerHeightPx * MapSheetSavedRestingFraction
                MapSheetContent.TravelPreview ->
                    MapLocationPillHeight.toPx() + MapSheetTravelRestingSurfaceHeight.toPx()
            }
        }
        val expandedOffset = -locationPillHeightPx
        val collapsedOffset =
            containerHeightPx - locationPillHeightPx - collapsedSurfaceHeightPx
        val restingOffset =
            (containerHeightPx - restingVisibleHeightPx).coerceIn(expandedOffset, collapsedOffset)
        val anchors = remember(containerHeightPx, content, density) {
            DraggableAnchors {
                if (content == MapSheetContent.SavedSchedules) {
                    MapSheetAnchor.Expanded at expandedOffset
                }
                MapSheetAnchor.Resting at restingOffset
                MapSheetAnchor.Collapsed at collapsedOffset
            }
        }
        SideEffect {
            val contentChanged = previousContent != content
            if (contentChanged && previousContent == MapSheetContent.SavedSchedules) {
                lastSavedAnchor = draggableState.settledValue
            }
            val targetAfterAnchorUpdate = when {
                previousContent == null -> MapSheetAnchor.Resting
                contentChanged && content == MapSheetContent.TravelPreview ->
                    MapSheetAnchor.Resting
                contentChanged && content == MapSheetContent.SavedSchedules -> lastSavedAnchor
                else -> draggableState.targetValue
            }
            draggableState.updateAnchors(
                newAnchors = anchors,
                newTarget = targetAfterAnchorUpdate,
            )
            previousContent = content
        }

        val flingBehavior = AnchoredDraggableDefaults.flingBehavior(
            state = draggableState,
            positionalThreshold = { distance -> distance * .5f },
        )
        @Suppress("DEPRECATION")
        val dragModifier = Modifier.anchoredDraggable(
            state = draggableState,
            orientation = Orientation.Vertical,
            startDragImmediately = true,
            flingBehavior = flingBehavior,
        )
        val isExpanded = content == MapSheetContent.SavedSchedules &&
            draggableState.settledValue == MapSheetAnchor.Expanded
        val isCollapsed = draggableState.settledValue == MapSheetAnchor.Collapsed
        val surfaceShape = when {
            isExpanded -> RoundedCornerShape(0.dp)
            content == MapSheetContent.TravelPreview ->
                RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
            else -> RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
        }
        val locationLabel = uiState.mapCenterLocationLabel ?: "위치 확인 중"

        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset {
                    IntOffset(
                        x = 0,
                        y = draggableState.requireOffset().roundToInt(),
                    )
                }
                .testTag("map-bottom-sheet")
                .semantics {
                    stateDescription = draggableState.settledValue.name
                },
        ) {
            MapLocationPill(locationLabel)
            val sheetSurfaceSizeModifier = if (
                content == MapSheetContent.SavedSchedules &&
                uiState.loadState == MapLoadState.CONTENT &&
                uiState.filteredSchedules.isNotEmpty()
            ) {
                Modifier
                    .fillMaxWidth()
                    .layout { measurable, constraints ->
                        val currentOffset = draggableState.offset
                            .takeUnless(Float::isNaN)
                            ?: restingOffset
                        val visibleHeight =
                            (containerHeightPx - currentOffset - locationPillHeightPx)
                            .roundToInt()
                            .coerceIn(0, constraints.maxHeight)
                        val placeable = measurable.measure(
                            constraints.copy(
                                minHeight = visibleHeight,
                                maxHeight = visibleHeight,
                            ),
                        )
                        layout(placeable.width, placeable.height) {
                            placeable.placeRelative(0, 0)
                        }
                    }
            } else {
                Modifier.fillMaxSize()
            }
            MapSheetSurface(
                modifier = sheetSurfaceSizeModifier.offset(y = MapLocationPillHeight),
                shape = surfaceShape,
                shadowElevation = if (content == MapSheetContent.SavedSchedules) 10.dp else 0.dp,
            ) {
                when (content) {
                    MapSheetContent.SavedSchedules -> SavedScheduleSheetContent(
                        uiState = uiState,
                        onIntent = onIntent,
                        onScheduleClick = { schedule ->
                            onIntent(MapIntent.SelectSchedule(schedule.id))
                        },
                    )
                    MapSheetContent.TravelPreview -> selectedSchedule?.let { schedule ->
                        SelectedScheduleSheetContent(
                            schedule = schedule,
                            menuExpanded = uiState.expandedScheduleMenuId == schedule.id,
                            onBack = { onIntent(MapIntent.ClearSelection) },
                            onToggleMenu = {
                                onIntent(MapIntent.ToggleScheduleMenu(schedule.id))
                            },
                            onDismissMenu = { onIntent(MapIntent.DismissScheduleMenu) },
                            onRename = {
                                onIntent(MapIntent.ShowRenameScheduleDialog(schedule.id))
                            },
                            onDelete = {
                                onIntent(MapIntent.ShowDeleteScheduleDialog(schedule.id))
                            },
                            onOpenSchedule = {
                                onOpenSchedule(schedule.id, schedule.title, null)
                            },
                        )
                    }
                }
            }
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(
                        y = MapLocationPillHeight +
                            if (isCollapsed) (-27).dp else 0.dp,
                    )
                    .size(48.dp)
                    .testTag("map-bottom-sheet-handle")
                    .then(dragModifier),
            )
        }

        if (uiState.selection == MapSelection.NONE) {
            val createControlThresholdPx = with(density) {
                MapCreateControlIconThreshold.toPx()
            }
            val createControlMode by remember(
                containerHeightPx,
                locationPillHeightPx,
                restingOffset,
                createControlThresholdPx,
            ) {
                derivedStateOf {
                    val currentOffset = draggableState.offset
                        .takeUnless(Float::isNaN)
                        ?: restingOffset
                    val visibleSurfaceHeightPx = containerHeightPx -
                        (currentOffset + locationPillHeightPx).coerceAtLeast(0f)
                    if (visibleSurfaceHeightPx >= createControlThresholdPx) {
                        MapCreateControlMode.IconOnly
                    } else {
                        MapCreateControlMode.Labelled
                    }
                }
            }
            CreateScheduleControl(
                expanded = uiState.isCreateMenuExpanded,
                mode = createControlMode,
                onToggle = { onIntent(MapIntent.ToggleCreateMenu) },
                onCreateFromVideo = {
                    onIntent(MapIntent.ToggleCreateMenu)
                    onCreateFromVideo()
                },
                onCreateFromStorage = {
                    onIntent(MapIntent.ShowComingSoonDialog)
                },
                onCreateManually = {
                    onIntent(MapIntent.ShowComingSoonDialog)
                },
            )
        }
    }
}

@Composable
private fun MapSheetSurface(
    modifier: Modifier,
    shape: RoundedCornerShape,
    shadowElevation: Dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .shadow(shadowElevation, shape),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clip(shape)
                .background(LinkItTheme.color.semantic.background.elevated.normal),
        ) {
            SheetHandle()
            content()
        }
    }
}

@Composable
private fun ColumnScope.SavedScheduleSheetContent(
    uiState: MapUiState,
    onIntent: (MapIntent) -> Unit,
    onScheduleClick: (MapScheduleUiModel) -> Unit,
) {
    val nanumSquare = rememberNanumSquareFontFamily()
    Text(
        text = "저장한 일정",
        style = LinkItTheme.typography.body2NormalBold.copy(fontFamily = nanumSquare),
        color = LinkItTheme.color.semantic.label.normal,
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = 20.dp,
                top = if (uiState.loadState == MapLoadState.EMPTY) 16.dp else 12.dp,
                end = 20.dp,
            )
            .height(24.dp),
    )

    when (uiState.loadState) {
        MapLoadState.CONTENT -> MapFilters(uiState = uiState, onIntent = onIntent)
        MapLoadState.EMPTY -> {
            MapFilters(uiState = uiState, onIntent = onIntent)
            Spacer(Modifier.height(16.dp))
        }
        else -> Unit
    }

    when (uiState.loadState) {
        MapLoadState.LOADING -> MapSheetStatus(
            title = "저장한 일정을 불러오는 중이에요",
            showProgress = true,
        )
        MapLoadState.EMPTY -> EmptyScheduleSheetContent(
            onCreateSchedule = { onIntent(MapIntent.ToggleCreateMenu) },
        )
        MapLoadState.ERROR -> ScheduleLoadErrorContent(
            description = uiState.errorMessage
                ?: "네트워크 연결을 확인한 뒤 다시 시도해 주세요.",
            onRetry = { onIntent(MapIntent.RetryLoad) },
        )
        MapLoadState.CONTENT -> ScheduleList(
            schedules = uiState.filteredSchedules,
            expandedScheduleMenuId = uiState.expandedScheduleMenuId,
            onScheduleClick = onScheduleClick,
            onToggleMenu = { scheduleId ->
                onIntent(MapIntent.ToggleScheduleMenu(scheduleId))
            },
            onDismissMenu = { onIntent(MapIntent.DismissScheduleMenu) },
            onRename = { scheduleId ->
                onIntent(MapIntent.ShowRenameScheduleDialog(scheduleId))
            },
            onDelete = { scheduleId ->
                onIntent(MapIntent.ShowDeleteScheduleDialog(scheduleId))
            },
        )
    }
}

@Composable
private fun ColumnScope.SelectedScheduleSheetContent(
    schedule: MapScheduleUiModel,
    menuExpanded: Boolean,
    onBack: () -> Unit,
    onToggleMenu: () -> Unit,
    onDismissMenu: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
    onOpenSchedule: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().height(56.dp).padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = LinkItIcon.Arrow.ChevronLeft,
            contentDescription = "저장한 일정으로 돌아가기",
            tint = LinkItTheme.color.semantic.label.strong,
            modifier = Modifier.size(24.dp).clickable(onClick = onBack),
        )
        Text(
            text = schedule.title,
            style = LinkItTheme.typography.heading2Bold,
            color = LinkItTheme.color.semantic.label.strong,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = 4.dp).weight(1f),
        )
        Box {
            Icon(
                imageVector = LinkItIcon.Utility.MoreHorizontal,
                contentDescription = "일정 더보기",
                tint = LinkItTheme.color.semantic.label.strong,
                modifier = Modifier
                    .size(24.dp)
                    .clickable(onClick = onToggleMenu)
                    .testTag("map-selected-schedule-more"),
            )
            ScheduleMoreMenu(
                expanded = menuExpanded,
                scheduleId = schedule.id,
                onDismiss = onDismissMenu,
                onRename = onRename,
                onDelete = onDelete,
            )
        }
    }
    ScheduleListRow(
        schedule = schedule,
        modifier = Modifier
            .testTag("map-selected-schedule-${schedule.id}")
            .clickable(onClick = onOpenSchedule),
        compact = true,
    )
}

private enum class MapSheetAnchor {
    Collapsed,
    Resting,
    Expanded,
}

private enum class MapSheetContent {
    SavedSchedules,
    TravelPreview,
}

private enum class MapCreateControlMode {
    Labelled,
    IconOnly,
}

private val MapLocationPillHeight = 57.dp
private val MapSheetCollapsedSurfaceHeight = 21.dp
private val MapSheetTravelRestingSurfaceHeight = 189.dp
// 744dp reference viewport: resting sheet surface 337dp - 15dp handle - 36dp title.
private val MapSheetRestingStatusHeight = 286.dp
// Figma's 380dp ruler includes the 76dp app bottom navigation that sits below MapContent.
private val MapCreateControlIconThreshold = 380.dp - 76.dp
private val MapCreateControlLabelledWidth = 97.dp
private val MapCreateControlIconOnlyWidth = 40.dp
private val MapCreateMenuWidth = 171.dp
private val ScheduleMoreMenuWidth = 160.dp
private const val ScheduleActionFeedbackDurationMillis = 3_000L
private const val MapSheetSavedRestingFraction = .53f
private const val MapCreateControlAnimationDurationMillis = 180
private const val MapCreateMenuEnterDurationMillis = 210
private const val MapCreateMenuExitDurationMillis = 180
private const val MapCreateIconTransitionDurationMillis = 160
private const val MapCreateIconTransitionScale = .9f

@Composable
private fun MapFilters(
    uiState: MapUiState,
    onIntent: (MapIntent) -> Unit,
) {
    Row(
        modifier = Modifier.padding(start = 20.dp, top = 8.dp, end = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box {
            FilterPill(
                icon = Res.drawable.map_filter_globe,
                text = uiState.selectedRegion ?: "국가",
                active = uiState.selectedRegion != null,
                onClick = { onIntent(MapIntent.ToggleFilter(MapFilterType.REGION)) },
            )
            FilterOptions(
                expanded = uiState.expandedFilter == MapFilterType.REGION,
                filterType = MapFilterType.REGION,
                options = listOf(null) + uiState.availableRegions,
                selected = uiState.selectedRegion,
                label = { it ?: "전체국가" },
                emptyLabel = "생성된 지역 없음".takeIf { uiState.availableRegions.isEmpty() },
                onDismiss = { onIntent(MapIntent.ToggleFilter(MapFilterType.REGION)) },
                onSelect = { onIntent(MapIntent.SelectRegion(it)) },
            )
        }
        Box {
            FilterPill(
                icon = Res.drawable.map_filter_category,
                text = uiState.selectedStyle?.label ?: "여행 스타일",
                active = uiState.selectedStyle != null,
                onClick = { onIntent(MapIntent.ToggleFilter(MapFilterType.STYLE)) },
            )
            FilterOptions(
                expanded = uiState.expandedFilter == MapFilterType.STYLE,
                filterType = MapFilterType.STYLE,
                options = listOf(null) + MapTravelStyleFilter.entries,
                selected = uiState.selectedStyle,
                label = { it?.label ?: "전체 스타일" },
                onDismiss = { onIntent(MapIntent.ToggleFilter(MapFilterType.STYLE)) },
                onSelect = { onIntent(MapIntent.SelectStyle(it)) },
            )
        }
        Box {
            FilterPill(
                icon = Res.drawable.map_filter_money,
                text = uiState.durationFilter.takeUnless { it == MapDurationFilter.ALL }?.label
                    ?: "기간",
                active = uiState.durationFilter != MapDurationFilter.ALL,
                onClick = { onIntent(MapIntent.ToggleFilter(MapFilterType.DURATION)) },
            )
            FilterOptions(
                expanded = uiState.expandedFilter == MapFilterType.DURATION,
                filterType = MapFilterType.DURATION,
                options = MapDurationFilter.entries,
                selected = uiState.durationFilter,
                label = MapDurationFilter::label,
                onDismiss = { onIntent(MapIntent.ToggleFilter(MapFilterType.DURATION)) },
                onSelect = { onIntent(MapIntent.SelectDuration(it)) },
            )
        }
    }
}

@Composable
private fun <T> FilterOptions(
    expanded: Boolean,
    filterType: MapFilterType,
    options: List<T>,
    selected: T,
    label: (T) -> String,
    emptyLabel: String? = null,
    onDismiss: () -> Unit,
    onSelect: (T) -> Unit,
) {
    val itemColors = LinkItMenuDefaults.itemColors().copy(
        selectedTextColor = LinkItTheme.color.semantic.label.normal,
    )
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        modifier = Modifier
            .widthIn(min = LinkItMenuDefaults.MinWidth)
            .heightIn(max = 224.dp)
            .testTag("map-filter-${filterType.name.lowercase()}-popup"),
        offset = DpOffset(0.dp, 8.dp),
        shape = LinkItMenuDefaults.ContainerShape,
        containerColor = LinkItMenuDefaults.containerColor,
        tonalElevation = 0.dp,
        shadowElevation = LinkItMenuDefaults.ShadowElevation,
        border = BorderStroke(
            LinkItMenuDefaults.BorderWidth,
            LinkItMenuDefaults.borderColor,
        ),
    ) {
        options.forEachIndexed { index, option ->
            if (index > 0) Spacer(Modifier.height(LinkItMenuDefaults.ItemSpacing))
            val isSelected = option == selected
            LinkItMenuItem(
                text = label(option),
                onClick = { onSelect(option) },
                selected = isSelected,
                padding = MenuItemPadding.Compact,
                colors = itemColors,
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .then(
                        if (isSelected) {
                            Modifier.background(
                                LinkItTheme.color.semantic.fill.normal,
                                LinkItMenuDefaults.ItemShape,
                            )
                        } else {
                            Modifier
                        },
                    ),
            )
        }
        emptyLabel?.let { text ->
            Spacer(Modifier.height(LinkItMenuDefaults.ItemSpacing))
            LinkItMenuItem(
                text = text,
                onClick = {},
                enabled = false,
                padding = MenuItemPadding.Compact,
                colors = itemColors,
                modifier = Modifier.padding(horizontal = 8.dp),
            )
        }
    }
}

@Composable
private fun EmptyScheduleSheetContent(
    onCreateSchedule: () -> Unit,
) {
    ScheduleSummaryRow(scheduleCount = 0)
    Spacer(Modifier.height(4.dp))
    ScheduleStateContent(
        title = "아직 등록된 일정이 없습니다.",
        actionLabel = "일정 생성하기",
        onAction = onCreateSchedule,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp),
        visual = { EmptyScheduleIllustration() },
    )
}

@Composable
private fun ScheduleLoadErrorContent(
    description: String,
    onRetry: () -> Unit,
) {
    ScheduleStateContent(
        title = "일정을 불러오지 못했어요",
        description = description,
        actionLabel = "다시 시도",
        onAction = onRetry,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 32.dp, top = 16.dp, end = 32.dp, bottom = 24.dp)
            .semantics { liveRegion = LiveRegionMode.Polite },
        visual = {
            Box(
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(LinkItTheme.color.semantic.status.negative.copy(alpha = .1f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = LinkItIcon.Utility.CircleExclamationFill,
                    contentDescription = null,
                    tint = LinkItTheme.color.semantic.status.negative,
                    modifier = Modifier.size(36.dp),
                )
            }
        },
    )
}

@Composable
private fun ScheduleStateContent(
    title: String,
    actionLabel: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
    description: String? = null,
    visual: @Composable () -> Unit,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        visual()
        Text(
            text = title,
            style = LinkItTheme.typography.body1NormalSemibold,
            color = LinkItTheme.color.semantic.label.normal,
            textAlign = TextAlign.Center,
        )
        description?.let {
            Text(
                text = it,
                style = LinkItTheme.typography.label2Medium,
                color = LinkItTheme.color.semantic.label.alternative,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 6.dp),
            )
        }
        LinkItButton(
            onClick = onAction,
            text = actionLabel,
            size = ButtonSize.Medium,
            modifier = Modifier.padding(top = if (description == null) 8.dp else 16.dp),
        )
    }
}

@Composable
private fun EmptyScheduleIllustration() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(144.dp),
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(x = (-0.45).dp, y = 10.dp)
                .width(188.21.dp)
                .height(124.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clipToBounds(),
            ) {
                Image(
                    painter = painterResource(Res.drawable.map_empty_schedule_character),
                    contentDescription = null,
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            scaleY = 1.1384f
                            transformOrigin = TransformOrigin(0f, 0f)
                        },
                )
            }
            Box(
                modifier = Modifier
                    .offset(x = 133.18.dp, y = 26.21.dp)
                    .width(43.62.dp)
                    .height(42.49.dp)
                    .clipToBounds(),
            ) {
                Image(
                    painter = painterResource(Res.drawable.map_empty_schedule_bubble),
                    contentDescription = null,
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier
                        .offset(y = (-18.36).dp)
                        .fillMaxSize()
                        .graphicsLayer {
                            scaleX = 2.2254f
                            scaleY = 1.4319f
                            transformOrigin = TransformOrigin(0f, 0f)
                        },
                )
            }
        }
    }
}

@Composable
private fun ScheduleSummaryRow(scheduleCount: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(29.dp)
            .padding(horizontal = 20.dp, vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = "총 ${scheduleCount}개 일정",
            style = LinkItTheme.typography.label2Medium,
            color = LinkItTheme.color.semantic.label.alternative,
        )
        Text(
            text = "최신순",
            style = LinkItTheme.typography.label2Medium,
            color = PaletteTokens.PingoNeutral400,
        )
    }
}

@Composable
private fun MapSheetStatus(
    title: String,
    description: String? = null,
    showProgress: Boolean = false,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(MapSheetRestingStatusHeight)
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        if (showProgress) {
            CircularProgressIndicator(
                color = LinkItTheme.color.semantic.primary.normal,
                modifier = Modifier.size(28.dp),
            )
            Spacer(Modifier.height(16.dp))
        }
        Text(
            text = title,
            style = LinkItTheme.typography.body2NormalBold,
            color = LinkItTheme.color.semantic.label.normal,
            textAlign = TextAlign.Center,
        )
        description?.let {
            Text(
                text = it,
                style = LinkItTheme.typography.caption1Regular,
                color = LinkItTheme.color.semantic.label.alternative,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 6.dp),
            )
        }
    }
}

@Composable
private fun ColumnScope.ScheduleList(
    schedules: List<MapScheduleUiModel>,
    expandedScheduleMenuId: String?,
    onScheduleClick: (MapScheduleUiModel) -> Unit,
    onToggleMenu: (String) -> Unit,
    onDismissMenu: () -> Unit,
    onRename: (String) -> Unit,
    onDelete: (String) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth().weight(1f)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(37.dp)
                .padding(horizontal = 20.dp, vertical = 9.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                "총 ${schedules.size}개 일정",
                style = LinkItTheme.typography.label2Medium,
                color = LinkItTheme.color.semantic.label.alternative,
            )
            Text(
                "최신순",
                style = LinkItTheme.typography.label2Medium,
                color = LinkItTheme.color.semantic.label.neutral,
            )
        }

        if (schedules.isEmpty()) {
            MapSheetStatus(
                title = "조건에 맞는 일정이 없어요",
                description = "필터를 바꿔 다시 찾아보세요.",
            )
        } else {
            LazyColumn(
                modifier = Modifier.testTag("map-schedule-list"),
                contentPadding = PaddingValues(bottom = 72.dp),
            ) {
                items(schedules, key = MapScheduleUiModel::id) { schedule ->
                    ScheduleListRow(
                        schedule = schedule,
                        menuExpanded = expandedScheduleMenuId == schedule.id,
                        onToggleMenu = { onToggleMenu(schedule.id) },
                        onDismissMenu = onDismissMenu,
                        onRename = { onRename(schedule.id) },
                        onDelete = { onDelete(schedule.id) },
                        modifier = Modifier
                            .testTag("map-schedule-${schedule.id}")
                            .clickable { onScheduleClick(schedule) },
                    )
                }
            }
        }
    }
}

@Composable
private fun MapLocationPill(text: String) {
    val nanumSquare = rememberNanumSquareFontFamily()
    Box(
        modifier = Modifier.fillMaxWidth().height(MapLocationPillHeight),
        contentAlignment = Alignment.TopCenter,
    ) {
        Text(
            text = text,
            style = LinkItTheme.typography.caption1Bold.copy(fontFamily = nanumSquare),
            color = PaletteTokens.PingoNeutral700,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .testTag("map-center-location-label")
                .padding(top = 12.dp)
                .widthIn(max = 220.dp)
                .shadow(1.dp, RoundedCornerShape(100.dp))
                .clip(RoundedCornerShape(100.dp))
                .background(LinkItTheme.color.semantic.static.white.copy(alpha = .75f))
                .border(1.dp, LinkItTheme.color.semantic.static.white, RoundedCornerShape(100.dp))
                .padding(horizontal = 14.dp, vertical = 8.dp),
        )
    }
}

@Composable
private fun BoxScope.PlaceInformationCard(
    place: MapPlaceUiModel,
    canShowPrevious: Boolean,
    canShowNext: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onClose: () -> Unit,
    onViewInSchedule: () -> Unit,
    onOpenPlaceDetail: () -> Unit,
) {
    val navigationBarBottomPadding =
        WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    Row(
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(end = 20.dp, bottom = 241.dp + navigationBarBottomPadding),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        CircleArrow(
            icon = LinkItIcon.Arrow.ChevronLeft,
            contentDescription = "이전 장소 이동",
            testTag = "map-place-previous",
            enabled = canShowPrevious,
            onClick = onPrevious,
        )
        CircleArrow(
            icon = LinkItIcon.Arrow.ChevronRight,
            contentDescription = "다음 장소 이동",
            testTag = "map-place-next",
            enabled = canShowNext,
            onClick = onNext,
        )
    }
    val cardShape = RoundedCornerShape(16.dp)
    Column(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .padding(
                start = 20.dp,
                end = 20.dp,
                bottom = navigationBarBottomPadding,
            )
            .fillMaxWidth()
            .height(233.dp)
            .shadow(2.dp, cardShape)
            .clip(cardShape)
            .background(LinkItTheme.color.semantic.background.elevated.normal)
            .padding(16.dp)
            .testTag("map-place-card"),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(24.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LinkItBadge(
                text = "${place.day}일차 ${place.itemOrder}번째 여행",
                size = BadgeSize.XSmall,
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                textStyle = LinkItTheme.typography.caption3Regular,
                modifier = Modifier.testTag("map-place-order"),
            )
            Spacer(Modifier.weight(1f))
            Icon(
                imageVector = LinkItIcon.Utility.Close,
                contentDescription = "장소 카드 닫기",
                tint = LinkItTheme.color.semantic.label.strong,
                modifier = Modifier
                    .size(24.dp)
                    .clickable(onClick = onClose)
                    .testTag("map-place-close"),
            )
        }
        Spacer(Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth().height(64.dp)) {
            Image(
                painter = painterResource(Res.drawable.map_place_photo),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(64.dp).clip(RoundedCornerShape(10.dp)),
            )
            Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
                PlaceCategoryTag(place.categoryLabel)
                Text(
                    text = place.name,
                    style = LinkItTheme.typography.headline2Bold,
                    color = LinkItTheme.color.semantic.label.strong,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp),
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(
                        imageVector = LinkItIcon.Location.LocationFill,
                        contentDescription = null,
                        tint = LinkItTheme.color.semantic.label.alternative,
                        modifier = Modifier.size(12.dp),
                    )
                    Text(
                        text = place.address.ifBlank { "주소 정보 없음" },
                        style = LinkItTheme.typography.caption2Medium.copy(
                            lineHeight = 16.5.sp,
                            letterSpacing = 0.sp,
                        ),
                        color = LinkItTheme.color.semantic.label.alternative,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Text(
            text = place.description.ifBlank {
                place.tips.ifBlank { "등록된 장소 설명이 없어요." }
            },
            style = LinkItTheme.typography.caption1Medium.copy(
                lineHeight = 19.5.sp,
                letterSpacing = 0.sp,
            ),
            color = LinkItTheme.color.semantic.label.neutral,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth().height(40.dp),
        )
        Spacer(Modifier.height(11.dp))
        Row(
            modifier = Modifier.fillMaxWidth().height(38.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            PlaceCardAction(
                text = "일정에서 보기",
                testTag = "map-place-view-schedule",
                modifier = Modifier.weight(1f),
                onClick = onViewInSchedule,
            )
            PlaceCardAction(
                text = "장소 상세보기",
                testTag = "map-place-open-detail",
                modifier = Modifier.weight(1f),
                onClick = onOpenPlaceDetail,
            )
        }
    }
}

@Composable
private fun PlaceCategoryTag(text: String) {
    LinkItBadge(
        text = text,
        size = BadgeSize.XSmall,
        color = BadgeColor.Accent,
        accentBackgroundColor = LinkItTheme.color.semantic.accent.foreground.blue,
        accentContentColor = LinkItTheme.color.semantic.accent.foreground.blue,
        textStyle = LinkItTheme.typography.caption3Bold,
    )
}

@Composable
private fun CircleArrow(
    icon: ImageVector,
    contentDescription: String,
    testTag: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .shadow(1.dp, CircleShape)
            .clip(CircleShape)
            .background(LinkItTheme.color.semantic.background.elevated.normal)
            .border(1.dp, PaletteTokens.CoolNeutral95, CircleShape)
            .clickable(enabled = enabled, onClick = onClick)
            .testTag(testTag),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (enabled) {
                LinkItTheme.color.semantic.label.strong
            } else {
                LinkItTheme.color.semantic.label.disable
            },
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
private fun BoxScope.CreateScheduleControl(
    expanded: Boolean,
    mode: MapCreateControlMode,
    onToggle: () -> Unit,
    onCreateFromVideo: () -> Unit,
    onCreateFromStorage: () -> Unit,
    onCreateManually: () -> Unit,
) {
    val nanumSquare = rememberNanumSquareFontFamily()
    val visualMode = if (expanded) MapCreateControlMode.IconOnly else mode
    val iconOnly = visualMode == MapCreateControlMode.IconOnly
    val controlWidth by animateDpAsState(
        targetValue = if (iconOnly) {
            MapCreateControlIconOnlyWidth
        } else {
            MapCreateControlLabelledWidth
        },
        animationSpec = tween(MapCreateControlAnimationDurationMillis),
        label = "map-create-control-width",
    )
    val iconSize by animateDpAsState(
        targetValue = if (iconOnly) 24.dp else 20.dp,
        animationSpec = tween(MapCreateControlAnimationDurationMillis),
        label = "map-create-control-icon-size",
    )
    val controlBackground = if (expanded) {
        Modifier.background(PaletteTokens.PingoNeutral600)
    } else {
        Modifier.background(
            Brush.verticalGradient(
                colors = listOf(
                    PaletteTokens.PingoNeutral600,
                    PaletteTokens.PingoNeutral300,
                ),
            ),
        )
    }
    val latestExpanded = rememberUpdatedState(expanded)
    val exitingInteractionGuard = if (expanded) {
        Modifier
    } else {
        Modifier
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        awaitPointerEvent(PointerEventPass.Initial).changes.forEach {
                            it.consume()
                        }
                    }
                }
            }
            .clearAndSetSemantics {}
    }
    Column(
        modifier = Modifier.align(Alignment.BottomEnd).padding(end = 20.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        AnimatedVisibility(
            visible = expanded,
            modifier = Modifier.testTag("map-create-schedule-menu"),
            enter = fadeIn(
                tween(MapCreateControlAnimationDurationMillis),
            ) + expandVertically(
                animationSpec = tween(MapCreateMenuEnterDurationMillis),
                expandFrom = Alignment.Bottom,
            ),
            exit = fadeOut(
                tween(MapCreateMenuExitDurationMillis),
            ) + shrinkVertically(
                animationSpec = tween(MapCreateMenuExitDurationMillis),
                shrinkTowards = Alignment.Bottom,
            ),
        ) {
            Box(modifier = exitingInteractionGuard) {
                CreateScheduleMenu(
                    onCreateFromVideo = {
                        if (latestExpanded.value) {
                            onCreateFromVideo()
                        }
                    },
                    onCreateFromStorage = onCreateFromStorage,
                    onCreateManually = onCreateManually,
                )
            }
        }
        Box(
            modifier = Modifier
                .width(controlWidth)
                .height(40.dp)
                .clip(RoundedCornerShape(999.dp))
                .then(controlBackground)
                .clickable(onClick = onToggle)
                .testTag("map-create-schedule-control")
                .semantics {
                    contentDescription = if (expanded) {
                        "일정 생성 메뉴 닫기"
                    } else {
                        "일정 생성 메뉴 열기"
                    }
                    stateDescription = visualMode.name
                },
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .offset(x = 8.dp)
                    .size(iconSize),
                contentAlignment = Alignment.Center,
            ) {
                AnimatedContent(
                    targetState = expanded,
                    transitionSpec = {
                        (
                            fadeIn(
                                tween(MapCreateIconTransitionDurationMillis),
                            ) + scaleIn(
                                animationSpec = tween(
                                    MapCreateIconTransitionDurationMillis,
                                ),
                                initialScale = MapCreateIconTransitionScale,
                            )
                        ).togetherWith(
                            fadeOut(
                                tween(MapCreateIconTransitionDurationMillis),
                            ) + scaleOut(
                                animationSpec = tween(
                                    MapCreateIconTransitionDurationMillis,
                                ),
                                targetScale = MapCreateIconTransitionScale,
                            ),
                        )
                    },
                    contentAlignment = Alignment.Center,
                    label = "map-create-control-icon",
                ) { isExpanded ->
                    Icon(
                        imageVector = if (isExpanded) {
                            LinkItIcon.Utility.Close
                        } else {
                            LinkItIcon.Utility.Ai
                        },
                        contentDescription = null,
                        tint = if (isExpanded) {
                            PaletteTokens.CoolNeutral98
                        } else {
                            LinkItTheme.color.semantic.inverse.label
                        },
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
            CreateScheduleControlLabel(
                visible = !iconOnly,
                nanumSquare = nanumSquare,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 32.dp),
            )
        }
    }
}

@Composable
private fun CreateScheduleControlLabel(
    visible: Boolean,
    nanumSquare: FontFamily,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(MapCreateControlAnimationDurationMillis)),
        exit = fadeOut(tween(MapCreateControlAnimationDurationMillis)),
        modifier = modifier,
    ) {
        Text(
            text = "일정 생성",
            style = LinkItTheme.typography.label1NormalMedium.copy(
                fontFamily = nanumSquare,
            ),
            color = PaletteTokens.PingoNeutral50,
            maxLines = 1,
            softWrap = false,
        )
    }
}

@Composable
private fun CreateScheduleMenu(
    onCreateFromVideo: () -> Unit,
    onCreateFromStorage: () -> Unit,
    onCreateManually: () -> Unit,
) {
    Column(
        modifier = Modifier
            .width(MapCreateMenuWidth)
            .clip(RoundedCornerShape(12.dp))
            .background(PaletteTokens.PingoNeutral600)
            .padding(vertical = 8.dp),
    ) {
        CreateScheduleMenuOption(
            icon = LinkItIcon.Control.Link,
            text = "영상 링크로 만들기",
            highlighted = true,
            onClick = onCreateFromVideo,
        )
        CreateScheduleMenuDivider()
        CreateScheduleMenuOption(
            icon = LinkItIcon.Control.Upload,
            text = "보관함에서 가져오기",
            highlighted = false,
            onClick = onCreateFromStorage,
        )
        CreateScheduleMenuDivider()
        CreateScheduleMenuOption(
            icon = LinkItIcon.Control.Customize,
            text = "직접 만들기",
            highlighted = false,
            onClick = onCreateManually,
        )
    }
}

@Composable
private fun CreateScheduleMenuOption(
    icon: ImageVector,
    text: String,
    highlighted: Boolean,
    onClick: () -> Unit,
) {
    val contentColor = if (highlighted) {
        PaletteTokens.PingoNeutral50
    } else {
        PaletteTokens.PingoNeutral400
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(20.dp),
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            style = LinkItTheme.typography.label2Medium,
            color = contentColor,
            maxLines = 1,
            softWrap = false,
        )
    }
}

@Composable
private fun CreateScheduleMenuDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(PaletteTokens.PingoNeutral500),
    )
}

@Composable
private fun SheetHandle() {
    Box(
        modifier = Modifier.fillMaxWidth().height(15.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .width(48.dp)
                .height(3.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(LinkItTheme.color.semantic.label.alternative.copy(alpha = .2f)),
        )
    }
}

@Composable
private fun FilterPill(
    icon: DrawableResource,
    text: String,
    active: Boolean,
    onClick: () -> Unit,
) {
    val nanumSquare = rememberNanumSquareFontFamily()
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(
                if (active) PaletteTokens.PingoMapSelectionBackground
                else LinkItTheme.color.semantic.fill.normal,
            )
            .clickable(onClick = onClick)
            .padding(start = 12.dp, top = 8.dp, end = 14.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(painterResource(icon), null, Modifier.size(16.dp))
        Text(
            text = text,
            style = LinkItTheme.typography.label2Bold.copy(fontFamily = nanumSquare),
            color = LinkItTheme.color.semantic.static.black,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.widthIn(max = 72.dp),
        )
        Icon(
            imageVector = LinkItIcon.Arrow.ChevronDownSmall,
            contentDescription = null,
            tint = LinkItTheme.color.semantic.static.black,
            modifier = Modifier.size(16.dp),
        )
    }
}

@Composable
private fun ScheduleListRow(
    schedule: MapScheduleUiModel,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    menuExpanded: Boolean = false,
    onToggleMenu: () -> Unit = {},
    onDismissMenu: () -> Unit = {},
    onRename: () -> Unit = {},
    onDelete: () -> Unit = {},
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp)) {
            Image(
                painter = painterResource(Res.drawable.map_schedule_thumbnail),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .width(if (compact) 60.dp else 80.dp)
                    .height(if (compact) 75.dp else 100.dp)
                    .clip(RoundedCornerShape(8.dp)),
            )
            Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    schedule.hashtags.take(2).forEach { MiniTag(it.removePrefix("#")) }
                }
                Text(
                    text = schedule.title,
                    style = LinkItTheme.typography.body2NormalBold,
                    color = LinkItTheme.color.semantic.label.normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 6.dp),
                )
                Row(
                    modifier = Modifier.padding(top = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ScheduleMeta(Res.drawable.map_calendar, "${schedule.nights}박${schedule.days}일")
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(12.dp)
                            .background(LinkItTheme.color.semantic.line.solid.normal),
                    )
                    ScheduleMeta(Res.drawable.map_filter_category, "${schedule.itemCount}곳")
                }
                Text(
                    text = "${schedule.regionLabel} · 장소 ${schedule.places.size}개 지도 표시",
                    style = LinkItTheme.typography.caption1Bold,
                    color = LinkItTheme.color.semantic.label.neutral,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
            if (!compact) {
                Box {
                    Icon(
                        imageVector = LinkItIcon.Utility.MoreHorizontal,
                        contentDescription = "일정 더보기",
                        tint = LinkItTheme.color.semantic.label.normal,
                        modifier = Modifier
                            .size(24.dp)
                            .clickable(onClick = onToggleMenu)
                            .testTag("map-schedule-more-${schedule.id}"),
                    )
                    ScheduleMoreMenu(
                        expanded = menuExpanded,
                        scheduleId = schedule.id,
                        onDismiss = onDismissMenu,
                        onRename = onRename,
                        onDelete = onDelete,
                    )
                }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(1.dp)
                .background(LinkItTheme.color.semantic.line.solid.normal),
        )
    }
}

@Composable
private fun ScheduleRenameDialog(
    value: String,
    isLoading: Boolean,
    onValueChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    ScheduleActionDialogSurface(
        onDismiss = onDismiss,
        shadowElevation = 8.dp,
        testTag = "map-schedule-rename-dialog",
    ) {
        Spacer(Modifier.height(12.dp))
        ScheduleDialogDescription(
            title = "변경을 원하는 이름을 적어주세요",
            description = "한글, 영문, 특수문자, 공백 포함\n20자 까지 적을 수 있어요",
            spacing = 8.dp,
            horizontalPadding = 20.dp,
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp),
        ) {
            ScheduleNameTextField(
                value = value,
                onValueChange = onValueChange,
                enabled = !isLoading,
            )
        }
        ScheduleDialogActions {
            LinkItButton(
                onClick = onConfirm,
                text = "확인",
                enabled = value.isNotBlank() && !isLoading,
                size = ButtonSize.Medium,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("map-schedule-rename-confirm"),
            )
            LinkItButton(
                onClick = onDismiss,
                text = "취소",
                enabled = !isLoading,
                variant = ButtonVariant.Outlined,
                color = ButtonColor.Assistive,
                size = ButtonSize.Medium,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("map-schedule-dialog-cancel"),
            )
        }
    }
}

@Composable
private fun ScheduleDeleteDialog(
    isLoading: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    ScheduleActionDialogSurface(
        onDismiss = onDismiss,
        showCloseButton = true,
        shadowElevation = 1.dp,
        testTag = "map-schedule-delete-dialog",
    ) {
        ScheduleDialogDescription(
            title = "정말 일정을 삭제하시겠어요?",
            description = "삭제된 일정은 복구할 수 없어요",
            spacing = 4.dp,
            horizontalPadding = 40.dp,
        )
        ScheduleDialogActions {
            LinkItButton(
                onClick = onConfirm,
                text = "삭제하기",
                enabled = !isLoading,
                colors = scheduleDeleteButtonColors(),
                size = ButtonSize.Medium,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("map-schedule-delete-confirm"),
            )
            LinkItButton(
                onClick = onDismiss,
                text = "돌아가기",
                enabled = !isLoading,
                variant = ButtonVariant.Outlined,
                color = ButtonColor.Assistive,
                size = ButtonSize.Medium,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("map-schedule-dialog-cancel"),
            )
        }
    }
}

@Composable
private fun ScheduleActionDialogSurface(
    onDismiss: () -> Unit,
    shadowElevation: Dp,
    testTag: String,
    showCloseButton: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    Popup(
        alignment = Alignment.Center,
        onDismissRequest = onDismiss,
        properties = PopupProperties(
            focusable = true,
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
        ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DialogDefaults.dimmerColor)
                .semantics { dialog() },
            contentAlignment = Alignment.Center,
        ) {
            val shape = LinkItTheme.shape.xl
            Box(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .widthIn(max = 320.dp)
                    .fillMaxWidth()
                    .shadow(shadowElevation, shape)
                    .clip(shape)
                    .background(DialogDefaults.containerColor)
                    .testTag(testTag),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    content = content,
                )

                if (showCloseButton) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                            .size(40.dp)
                            .clickable(onClick = onDismiss)
                            .testTag("map-schedule-dialog-close"),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = LinkItIcon.Utility.Close,
                            contentDescription = "닫기",
                            tint = LinkItTheme.color.semantic.label.normal,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ScheduleDialogDescription(
    title: String,
    description: String,
    spacing: Dp,
    horizontalPadding: Dp,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = horizontalPadding,
                top = 20.dp,
                end = horizontalPadding,
                bottom = 4.dp,
            ),
        verticalArrangement = Arrangement.spacedBy(spacing),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = title,
            style = LinkItTheme.typography.body1NormalBold,
            color = LinkItTheme.color.semantic.label.strong,
            textAlign = TextAlign.Center,
        )
        Text(
            text = description,
            modifier = Modifier.fillMaxWidth(),
            style = LinkItTheme.typography.label2Medium,
            color = LinkItTheme.color.semantic.label.alternative,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun ScheduleNameTextField(
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean,
) {
    val shape = LinkItTheme.shape.xl
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .testTag("map-schedule-name-input"),
        enabled = enabled,
        singleLine = true,
        textStyle = LinkItTheme.typography.body2NormalMedium.copy(
            color = LinkItTheme.color.semantic.label.normal,
        ),
        cursorBrush = SolidColor(LinkItTheme.color.semantic.label.normal),
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .shadow(1.dp, shape)
                    .clip(shape)
                    .background(LinkItTheme.color.semantic.background.normal.normal)
                    .border(
                        width = LinkItTheme.borderWidth.sm,
                        color = LinkItTheme.color.semantic.line.normal.neutral,
                        shape = shape,
                    )
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                innerTextField()
            }
        },
    )
}

@Composable
private fun ScheduleDialogActions(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        content = content,
    )
}

@Composable
private fun scheduleDeleteButtonColors(): ButtonColors {
    val semantic = LinkItTheme.color.semantic
    return ButtonColors(
        containerColor = semantic.status.negative,
        contentColor = semantic.static.white,
        borderColor = Color.Unspecified,
        disabledContainerColor = semantic.interaction.disable,
        disabledContentColor = semantic.label.disable,
        disabledBorderColor = Color.Unspecified,
    )
}

@Composable
private fun ScheduleMoreMenu(
    expanded: Boolean,
    scheduleId: String,
    onDismiss: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        modifier = Modifier
            .width(ScheduleMoreMenuWidth)
            .testTag("map-schedule-more-menu-$scheduleId"),
        shape = LinkItMenuDefaults.ContainerShape,
        containerColor = LinkItMenuDefaults.containerColor,
        tonalElevation = 0.dp,
        shadowElevation = LinkItMenuDefaults.ShadowElevation,
        border = BorderStroke(
            LinkItMenuDefaults.BorderWidth,
            LinkItMenuDefaults.borderColor,
        ),
    ) {
        LinkItMenuItem(
            text = "일정 이름 변경",
            onClick = onRename,
            leadingIcon = LinkItIcon.Control.Write,
            padding = MenuItemPadding.Regular,
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .testTag("map-schedule-rename"),
        )
        Spacer(Modifier.height(LinkItMenuDefaults.ItemSpacing))
        LinkItMenuItem(
            text = "일정 삭제",
            onClick = onDelete,
            leadingIcon = LinkItIcon.Control.Trash,
            padding = MenuItemPadding.Regular,
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .testTag("map-schedule-delete"),
        )
    }
}

@Composable
private fun ScheduleMeta(icon: DrawableResource, text: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(painterResource(icon), null, Modifier.size(16.dp))
        Text(
            text = text,
            style = LinkItTheme.typography.caption1Bold,
            color = LinkItTheme.color.semantic.label.alternative,
        )
    }
}

@Composable
private fun MiniTag(text: String) {
    val nanumSquare = rememberNanumSquareFontFamily()
    Text(
        text = text,
        style = LinkItTheme.typography.caption2Bold.copy(fontFamily = nanumSquare),
        color = LinkItTheme.color.semantic.label.alternative,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier
            .widthIn(max = 72.dp)
            .border(1.dp, LinkItTheme.color.semantic.line.normal.neutral, RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 5.dp),
    )
}

@Composable
private fun PlaceCardAction(
    text: String,
    testTag: String,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    LinkItButton(
        onClick = onClick,
        text = text,
        modifier = modifier.testTag(testTag),
        color = ButtonColor.Assistive,
        size = ButtonSize.Medium,
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 9.dp),
        textStyle = LinkItTheme.typography.label1NormalRegular,
    )
}

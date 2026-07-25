package com.linkit.company.feature.map.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.linkit.company.core.designsystem.foundation.color.token.PaletteTokens
import com.linkit.company.core.designsystem.foundation.icon.LinkItIcon
import com.linkit.company.core.designsystem.foundation.typography.rememberNanumSquareFontFamily
import com.linkit.company.core.designsystem.theme.LinkItTheme
import dev.zacsweers.metrox.viewmodel.metroViewModel
import linkitcompany.feature.map.generated.resources.Res
import linkitcompany.feature.map.generated.resources.map_calendar
import linkitcompany.feature.map.generated.resources.map_filter_category
import linkitcompany.feature.map.generated.resources.map_filter_globe
import linkitcompany.feature.map.generated.resources.map_place_photo
import linkitcompany.feature.map.generated.resources.map_schedule_thumbnail
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
fun MapScreen(
    onOpenSchedule: (scheduleId: String, title: String, focusedPlaceId: String?) -> Unit,
    navigateToScheduleEdit: () -> Unit,
    navigateToScheduleManual: () -> Unit = {},
    onOpenPlaceDetail: (MapPlaceUiModel) -> Unit = {},
    onOpenStorage: () -> Unit = {},
    onOpenMyPage: () -> Unit = {},
    viewModel: MapViewModel = metroViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
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
    CurrentLocationEffect(
        requestToken = uiState.locationRequestToken,
        onLocationAvailable = { location ->
            onIntent(MapIntent.CurrentLocationResolved(location.lat, location.lng))
        },
        onLocationUnavailable = {
            onIntent(MapIntent.CurrentLocationUnavailable("위치 권한 또는 위치 서비스를 확인해 주세요."))
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

        when (uiState.selection) {
            MapSelection.NONE -> SavedScheduleSheet(
                uiState = uiState,
                onIntent = onIntent,
                onScheduleClick = { schedule ->
                    onOpenSchedule(schedule.id, schedule.title, null)
                },
            )
            MapSelection.SCHEDULE -> uiState.selectedSchedule?.let { schedule ->
                SelectedScheduleSheet(
                    schedule = schedule,
                    onBack = { onIntent(MapIntent.ClearSelection) },
                    onOpenSchedule = { onOpenSchedule(schedule.id, schedule.title, null) },
                )
            }
            MapSelection.PLACE -> {
                val schedule = uiState.selectedSchedule
                val place = uiState.selectedPlace
                if (schedule != null && place != null) {
                    PlaceInformationCard(
                        place = place,
                        placeIndex = uiState.selectedPlaceIndex,
                        placeCount = schedule.places.size,
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
        }

        if (uiState.selection == MapSelection.NONE) {
            CreateScheduleControl(
                expanded = uiState.isCreateMenuExpanded,
                onToggle = { onIntent(MapIntent.ToggleCreateMenu) },
                onCreateFromVideo = {
                    onIntent(MapIntent.ToggleCreateMenu)
                    onCreateFromVideo()
                },
                onCreateFromStorage = {
                    onIntent(MapIntent.ToggleCreateMenu)
                    onCreateFromStorage()
                },
                onCreateManually = onCreateManually,
            )
        }
    }
}

@Composable
private fun BoxScope.MapCanvas(
    uiState: MapUiState,
    onIntent: (MapIntent) -> Unit,
) {
    val markers = uiState.toMapMarkers()
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

    if (uiState.loadState == MapLoadState.LOADING) {
        StaticMapBackground(uiState.mapType, Modifier.fillMaxSize())
    } else {
        PlatformMapBackground(
            mapType = uiState.mapType,
            modifier = Modifier.fillMaxSize(),
            markers = markers,
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

}

private fun MapUiState.toMapMarkers(): List<MapMarkerUiModel> = buildList {
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

    selectedSchedule?.places?.forEach { place ->
        add(
            MapMarkerUiModel(
                id = place.markerId,
                lat = place.latitude,
                lng = place.longitude,
                label = place.name,
                type = MapMarkerType.PLACE,
                selected = selectedPlaceMarkerId == place.markerId,
            ),
        )
    }
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
        MapRoundAction(LinkItIcon.Communication.PersonFill, "마이페이지", true, onOpenMyPage)
        MapRoundAction(LinkItIcon.Location.Map, "지도 종류 변경", false, onToggleMapType)
    }
}

@Composable
private fun MapRoundAction(
    icon: ImageVector,
    description: String,
    round: Boolean,
    onClick: () -> Unit,
) {
    val shape = if (round) CircleShape else RoundedCornerShape(10.dp)
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
            .padding(top = 20.dp, start = 72.dp, end = 72.dp),
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
private fun BoxScope.SavedScheduleSheet(
    uiState: MapUiState,
    onIntent: (MapIntent) -> Unit,
    onScheduleClick: (MapScheduleUiModel) -> Unit,
) {
    val nanumSquare = rememberNanumSquareFontFamily()
    val locationLabel = uiState.filteredSchedules.firstOrNull()?.regionLabel ?: "저장한 일정"
    Column(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .fillMaxWidth()
            .fillMaxHeight(.53f),
    ) {
        MapLocationPill(locationLabel)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .shadow(10.dp, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .background(LinkItTheme.color.semantic.background.elevated.normal),
        ) {
            SheetHandle()
            Text(
                text = "저장한 일정",
                style = LinkItTheme.typography.body2NormalBold.copy(fontFamily = nanumSquare),
                color = LinkItTheme.color.semantic.label.normal,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, top = 12.dp, end = 20.dp)
                    .height(24.dp),
            )

            if (uiState.loadState == MapLoadState.CONTENT) {
                MapFilters(uiState = uiState, onIntent = onIntent)
            }

            when (uiState.loadState) {
                MapLoadState.LOADING -> MapSheetStatus(
                    title = "저장한 일정을 불러오는 중이에요",
                    showProgress = true,
                )
                MapLoadState.EMPTY -> MapSheetStatus(
                    title = "아직 저장한 일정이 없어요",
                    description = "일정 생성으로 첫 일정을 만들어 보세요.",
                )
                MapLoadState.ERROR -> MapSheetStatus(
                    title = "일정을 불러오지 못했어요",
                    description = uiState.errorMessage,
                    actionLabel = "다시 시도",
                    onAction = { onIntent(MapIntent.RetryLoad) },
                )
                MapLoadState.CONTENT -> ScheduleList(
                    schedules = uiState.filteredSchedules,
                    onScheduleClick = onScheduleClick,
                )
            }
        }
    }
}

@Composable
private fun MapFilters(
    uiState: MapUiState,
    onIntent: (MapIntent) -> Unit,
) {
    Row(
        modifier = Modifier.padding(start = 20.dp, top = 8.dp, end = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FilterPill(
            icon = Res.drawable.map_filter_globe,
            text = uiState.selectedRegion ?: "지역",
            active = uiState.selectedRegion != null,
            onClick = { onIntent(MapIntent.ToggleFilter(MapFilterType.REGION)) },
        )
        FilterPill(
            icon = Res.drawable.map_filter_category,
            text = uiState.selectedStyle?.removePrefix("#") ?: "여행 스타일",
            active = uiState.selectedStyle != null,
            onClick = { onIntent(MapIntent.ToggleFilter(MapFilterType.STYLE)) },
        )
        FilterPill(
            icon = Res.drawable.map_calendar,
            text = uiState.durationFilter.takeUnless { it == MapDurationFilter.ALL }?.label ?: "기간",
            active = uiState.durationFilter != MapDurationFilter.ALL,
            onClick = { onIntent(MapIntent.ToggleFilter(MapFilterType.DURATION)) },
        )
    }

    when (uiState.expandedFilter) {
        MapFilterType.REGION -> FilterOptions(
            options = listOf(null) + uiState.availableRegions,
            selected = uiState.selectedRegion,
            label = { it ?: "전체" },
            onSelect = { onIntent(MapIntent.SelectRegion(it)) },
        )
        MapFilterType.STYLE -> FilterOptions(
            options = listOf(null) + uiState.availableStyles,
            selected = uiState.selectedStyle,
            label = { it?.removePrefix("#") ?: "전체" },
            onSelect = { onIntent(MapIntent.SelectStyle(it)) },
        )
        MapFilterType.DURATION -> FilterOptions(
            options = MapDurationFilter.entries,
            selected = uiState.durationFilter,
            label = MapDurationFilter::label,
            onSelect = { onIntent(MapIntent.SelectDuration(it)) },
        )
        null -> Unit
    }
}

@Composable
private fun <T> FilterOptions(
    options: List<T>,
    selected: T,
    label: (T) -> String,
    onSelect: (T) -> Unit,
) {
    LazyRow(
        contentPadding = PaddingValues(start = 20.dp, top = 10.dp, end = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(options) { option ->
            val isSelected = option == selected
            Text(
                text = label(option),
                style = LinkItTheme.typography.caption1Bold,
                color = if (isSelected) {
                    LinkItTheme.color.semantic.primary.heavy
                } else {
                    LinkItTheme.color.semantic.label.neutral
                },
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(
                        if (isSelected) PaletteTokens.PingoMapSelectionBackground
                        else LinkItTheme.color.semantic.fill.normal,
                    )
                    .clickable { onSelect(option) }
                    .padding(horizontal = 12.dp, vertical = 7.dp),
            )
        }
    }
}

@Composable
private fun ColumnScope.MapSheetStatus(
    title: String,
    description: String? = null,
    showProgress: Boolean = false,
    actionLabel: String? = null,
    onAction: () -> Unit = {},
) {
    Column(
        modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 32.dp),
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
        actionLabel?.let {
            Text(
                text = it,
                style = LinkItTheme.typography.label1NormalSemibold,
                color = LinkItTheme.color.semantic.static.white,
                modifier = Modifier
                    .padding(top = 16.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(LinkItTheme.color.semantic.primary.normal)
                    .clickable(onClick = onAction)
                    .padding(horizontal = 20.dp, vertical = 10.dp),
            )
        }
    }
}

@Composable
private fun ColumnScope.ScheduleList(
    schedules: List<MapScheduleUiModel>,
    onScheduleClick: (MapScheduleUiModel) -> Unit,
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
            LazyColumn(contentPadding = PaddingValues(bottom = 72.dp)) {
                items(schedules, key = MapScheduleUiModel::id) { schedule ->
                    ScheduleListRow(
                        schedule = schedule,
                        modifier = Modifier.clickable { onScheduleClick(schedule) },
                    )
                }
            }
        }
    }
}

@Composable
private fun BoxScope.SelectedScheduleSheet(
    schedule: MapScheduleUiModel,
    onBack: () -> Unit,
    onOpenSchedule: () -> Unit,
) {
    Column(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .offset(y = 30.dp)
            .fillMaxWidth()
            .height(246.dp),
    ) {
        MapLocationPill(schedule.regionLabel)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(189.dp)
                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                .background(LinkItTheme.color.semantic.background.elevated.normal),
        ) {
            SheetHandle()
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
            }
            ScheduleListRow(
                schedule = schedule,
                modifier = Modifier.clickable(onClick = onOpenSchedule),
                compact = true,
            )
        }
    }
}

@Composable
private fun MapLocationPill(text: String) {
    val nanumSquare = rememberNanumSquareFontFamily()
    Box(
        modifier = Modifier.fillMaxWidth().height(57.dp),
        contentAlignment = Alignment.TopCenter,
    ) {
        Text(
            text = text,
            style = LinkItTheme.typography.caption1Bold.copy(fontFamily = nanumSquare),
            color = PaletteTokens.PingoNeutral700,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
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
    placeIndex: Int,
    placeCount: Int,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onClose: () -> Unit,
    onViewInSchedule: () -> Unit,
    onOpenPlaceDetail: () -> Unit,
) {
    Row(
        modifier = Modifier.align(Alignment.BottomEnd).padding(end = 20.dp, bottom = 229.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        CircleArrow(LinkItIcon.Arrow.ChevronLeft, placeIndex > 0, onPrevious)
        CircleArrow(LinkItIcon.Arrow.ChevronRight, placeIndex < placeCount - 1, onNext)
    }
    Column(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .offset(y = 10.dp)
            .padding(horizontal = 20.dp)
            .fillMaxWidth()
            .height(233.dp)
            .shadow(8.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(LinkItTheme.color.semantic.background.elevated.normal)
            .padding(vertical = 16.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "${place.day}일차 ${place.itemOrder}번째 장소",
                style = LinkItTheme.typography.caption1Medium,
                color = LinkItTheme.color.semantic.label.alternative,
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(LinkItTheme.color.semantic.fill.normal)
                    .padding(horizontal = 10.dp, vertical = 4.dp),
            )
            Spacer(Modifier.weight(1f))
            Icon(
                imageVector = LinkItIcon.Utility.Close,
                contentDescription = "장소 카드 닫기",
                tint = LinkItTheme.color.semantic.label.strong,
                modifier = Modifier.size(20.dp).clickable(onClick = onClose),
            )
        }
        Row(modifier = Modifier.padding(start = 16.dp, top = 12.dp, end = 16.dp)) {
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
                    style = LinkItTheme.typography.heading2Bold,
                    color = LinkItTheme.color.semantic.label.strong,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp),
                )
                Text(
                    text = "⌖ ${place.address.ifBlank { "주소 정보 없음" }}",
                    style = LinkItTheme.typography.caption2Regular,
                    color = LinkItTheme.color.semantic.label.alternative,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }
        Text(
            text = place.description.ifBlank {
                place.tips.ifBlank { "등록된 장소 설명이 없어요." }
            },
            style = LinkItTheme.typography.caption1Regular,
            color = LinkItTheme.color.semantic.label.neutral,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = 16.dp, top = 12.dp, end = 16.dp),
        )
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, top = 12.dp, end = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            CardAction("일정에서 보기", Modifier.weight(1f), onViewInSchedule)
            CardAction("장소 상세보기", Modifier.weight(1f), onOpenPlaceDetail)
        }
    }
}

@Composable
private fun PlaceCategoryTag(text: String) {
    Text(
        text = text,
        style = LinkItTheme.typography.caption2Medium,
        color = LinkItTheme.color.semantic.accent.foreground.blue,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(LinkItTheme.color.semantic.accent.foreground.blue.copy(alpha = .08f))
            .padding(horizontal = 6.dp, vertical = 3.dp),
    )
}

@Composable
private fun CircleArrow(icon: ImageVector, enabled: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .shadow(4.dp, CircleShape)
            .clip(CircleShape)
            .background(LinkItTheme.color.semantic.background.elevated.normal)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
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
    onToggle: () -> Unit,
    onCreateFromVideo: () -> Unit,
    onCreateFromStorage: () -> Unit,
    onCreateManually: () -> Unit,
) {
    val nanumSquare = rememberNanumSquareFontFamily()
    Column(
        modifier = Modifier.align(Alignment.BottomEnd).padding(end = 20.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (expanded) {
            CreateOption("영상 링크로 만들기", onClick = onCreateFromVideo)
            CreateOption("보관함에서 가져오기", onClick = onCreateFromStorage)
            CreateOption("직접 만들기", enabled = false, onClick = onCreateManually)
        }
        Row(
            modifier = Modifier
                .height(40.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(PaletteTokens.PingoNeutral600)
                .clickable(onClick = onToggle)
                .padding(start = 8.dp, end = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = if (expanded) LinkItIcon.Utility.Close else LinkItIcon.Utility.Ai,
                contentDescription = null,
                tint = LinkItTheme.color.semantic.inverse.label,
                modifier = Modifier.size(20.dp),
            )
            Text(
                text = if (expanded) "닫기" else "일정 생성",
                style = LinkItTheme.typography.label1NormalMedium.copy(fontFamily = nanumSquare),
                color = PaletteTokens.PingoNeutral50,
                modifier = Modifier.padding(start = 4.dp),
            )
        }
    }
}

@Composable
private fun CreateOption(
    text: String,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    Text(
        text = text,
        style = LinkItTheme.typography.label1NormalSemibold,
        color = if (enabled) {
            LinkItTheme.color.semantic.label.strong
        } else {
            LinkItTheme.color.semantic.label.disable
        },
        modifier = Modifier
            .shadow(4.dp, RoundedCornerShape(999.dp))
            .clip(RoundedCornerShape(999.dp))
            .background(LinkItTheme.color.semantic.background.elevated.normal)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 12.dp),
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
                Icon(
                    imageVector = LinkItIcon.Utility.MoreHorizontal,
                    contentDescription = "일정 더보기",
                    tint = LinkItTheme.color.semantic.label.normal,
                    modifier = Modifier.size(24.dp),
                )
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
private fun CardAction(text: String, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .height(42.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(LinkItTheme.color.semantic.fill.normal)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = LinkItTheme.typography.label1NormalSemibold,
            color = LinkItTheme.color.semantic.label.strong,
        )
    }
}

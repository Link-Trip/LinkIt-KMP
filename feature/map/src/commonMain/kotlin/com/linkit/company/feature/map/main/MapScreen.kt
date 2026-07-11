package com.linkit.company.feature.map.main

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.linkit.company.core.designsystem.foundation.icon.LinkItIcon
import com.linkit.company.core.designsystem.foundation.color.token.PaletteTokens
import com.linkit.company.core.designsystem.foundation.typography.rememberNanumSquareFontFamily
import com.linkit.company.core.designsystem.theme.LinkItTheme
import dev.zacsweers.metrox.viewmodel.metroViewModel
import linkitcompany.feature.map.generated.resources.Res
import linkitcompany.feature.map.generated.resources.map_calendar
import linkitcompany.feature.map.generated.resources.map_place_photo
import linkitcompany.feature.map.generated.resources.map_selected_area
import linkitcompany.feature.map.generated.resources.map_selected_thumb_1
import linkitcompany.feature.map.generated.resources.map_selected_thumb_2
import linkitcompany.feature.map.generated.resources.map_selected_thumb_3
import linkitcompany.feature.map.generated.resources.map_selected_thumb_4
import linkitcompany.feature.map.generated.resources.map_selected_thumb_5
import linkitcompany.feature.map.generated.resources.map_filter_category
import linkitcompany.feature.map.generated.resources.map_filter_globe
import linkitcompany.feature.map.generated.resources.map_filter_money
import linkitcompany.feature.map.generated.resources.map_schedule_thumbnail
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
fun MapScreen(
    onOpenSchedule: () -> Unit,
    navigateToScheduleEdit: () -> Unit,
    onOpenMyPage: () -> Unit = {},
    viewModel: MapViewModel = metroViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    MapContent(
        uiState = uiState,
        onIntent = viewModel::onIntent,
        onOpenSchedule = onOpenSchedule,
        navigateToScheduleEdit = navigateToScheduleEdit,
        onOpenMyPage = onOpenMyPage,
    )
}

@Composable
fun MapContent(
    uiState: MapUiState,
    onIntent: (MapIntent) -> Unit,
    onOpenSchedule: () -> Unit = {},
    navigateToScheduleEdit: () -> Unit = {},
    onOpenMyPage: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(LinkItTheme.color.semantic.background.normal.alternative),
    ) {
        StaticMap(uiState)
        MapTopActions(
            onOpenMyPage = onOpenMyPage,
            onToggleMapType = { onIntent(MapIntent.ToggleMapType) },
        )
        MapMarkers(
            selection = uiState.selection,
            onScheduleClick = { onIntent(MapIntent.SelectSchedule) },
            onPlaceClick = { onIntent(MapIntent.SelectPlace) },
        )

        when (uiState.selection) {
            MapSelection.NONE -> SavedScheduleSheet(onScheduleClick = onOpenSchedule)
            MapSelection.SCHEDULE -> SelectedScheduleSheet(
                onBack = { onIntent(MapIntent.ClearSelection) },
                onOpenSchedule = onOpenSchedule,
            )
            MapSelection.PLACE -> PlaceInformationCard(
                selectedPlaceIndex = uiState.selectedPlaceIndex,
                onPrevious = { onIntent(MapIntent.ShowPreviousPlace) },
                onNext = { onIntent(MapIntent.ShowNextPlace) },
                onClose = { onIntent(MapIntent.ClosePlace) },
                navigateToScheduleEdit = navigateToScheduleEdit,
            )
        }

        if (uiState.selection == MapSelection.NONE) {
            CreateScheduleControl(
                expanded = uiState.isCreateMenuExpanded,
                onToggle = { onIntent(MapIntent.ToggleCreateMenu) },
                onCreateFromVideo = navigateToScheduleEdit,
            )
        }
    }
}

@Composable
private fun BoxScope.StaticMap(uiState: MapUiState) {
    PlatformMapBackground(uiState.mapType, Modifier.fillMaxSize())
    if (uiState.selection != MapSelection.NONE) {
        Image(
            painter = painterResource(Res.drawable.map_selected_area),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(
                    x = if (uiState.selection == MapSelection.PLACE) 3.dp else 52.dp,
                    y = if (uiState.selection == MapSelection.PLACE) 94.dp else 142.dp,
                )
                .width(273.dp)
                .height(325.dp),
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
private fun BoxScope.MapMarkers(
    selection: MapSelection,
    onScheduleClick: () -> Unit,
    onPlaceClick: () -> Unit,
) {
    if (selection == MapSelection.NONE) {
        ScheduleMarker(
            text = "도쿄 하라주쿠 여행",
            selected = false,
            modifier = Modifier.align(Alignment.TopStart).offset(51.dp, 80.dp),
            onClick = onScheduleClick,
        )
        ScheduleMarker(
            text = "도쿄 신주쿠 여행",
            selected = false,
            modifier = Modifier.align(Alignment.TopStart).offset(229.dp, 254.dp),
            onClick = onScheduleClick,
        )
        ScheduleMarker(
            text = "도쿄 하라주쿠 여행",
            selected = false,
            modifier = Modifier.align(Alignment.TopStart).offset(73.dp, 214.dp),
            onClick = onScheduleClick,
        )
        ScheduleMarker(
            text = "도쿄 신주쿠 하라주쿠 여행",
            selected = false,
            modifier = Modifier.align(Alignment.TopStart).offset(161.dp, 154.dp),
            onClick = onScheduleClick,
        )
        ClusterMarker(
            count = 2,
            modifier = Modifier.align(Alignment.TopStart).offset(50.dp, 199.dp),
        )
    } else if (selection == MapSelection.SCHEDULE) {
        ScheduleMarker(
            text = "도쿄 하라주쿠 여행",
            selected = false,
            modifier = Modifier.align(Alignment.TopStart).offset((-46).dp, 92.dp),
            onClick = onScheduleClick,
        )
        ScheduleMarker(
            text = "도쿄 하라주쿠 여행",
            selected = false,
            modifier = Modifier.align(Alignment.TopStart).offset((-24).dp, 206.dp),
            onClick = onScheduleClick,
        )
        ScheduleMarker(
            text = "도쿄 신주쿠 여행",
            selected = true,
            modifier = Modifier.align(Alignment.TopCenter).offset(y = 284.dp),
            onClick = onScheduleClick,
        )
        PhotoPlaceMarker(
            Res.drawable.map_selected_thumb_4,
            Modifier.align(Alignment.TopStart).offset(228.dp, 149.dp),
            onPlaceClick,
        )
        PhotoPlaceMarker(
            Res.drawable.map_selected_thumb_2,
            Modifier.align(Alignment.TopStart).offset(103.dp, 197.dp),
            onPlaceClick,
        )
        PhotoPlaceMarker(
            Res.drawable.map_selected_thumb_5,
            Modifier.align(Alignment.TopStart).offset(74.dp, 349.dp),
            onPlaceClick,
        )
        PhotoPlaceMarker(
            Res.drawable.map_selected_thumb_1,
            Modifier.align(Alignment.TopStart).offset(222.dp, 267.dp),
            onPlaceClick,
        )
        PhotoPlaceMarker(
            Res.drawable.map_selected_thumb_3,
            Modifier.align(Alignment.TopStart).offset(268.dp, 426.dp),
            onPlaceClick,
        )
    } else {
        ScheduleMarker(
            text = "도쿄 신주쿠 여행",
            selected = true,
            modifier = Modifier.align(Alignment.TopCenter).offset(y = 12.dp),
            onClick = onScheduleClick,
        )
        PhotoPlaceMarker(
            Res.drawable.map_selected_thumb_2,
            Modifier.align(Alignment.TopStart).offset(53.dp, 147.dp),
            onPlaceClick,
        )
        PhotoPlaceMarker(
            Res.drawable.map_selected_thumb_4,
            Modifier.align(Alignment.TopStart).offset(178.dp, 99.dp),
            onPlaceClick,
        )
        PhotoPlaceMarker(
            Res.drawable.map_selected_thumb_5,
            Modifier.align(Alignment.TopStart).offset(24.dp, 299.dp),
            onPlaceClick,
        )
        PhotoPlaceMarker(
            Res.drawable.map_selected_thumb_3,
            Modifier.align(Alignment.TopStart).offset(258.dp, 376.dp),
            onPlaceClick,
        )
        PhotoPlaceMarker(
            resource = Res.drawable.map_selected_thumb_1,
            modifier = Modifier.align(Alignment.TopStart).offset(158.dp, 240.dp),
            onClick = onPlaceClick,
            selected = true,
        )
    }
}

@Composable
private fun ScheduleMarker(
    text: String,
    selected: Boolean,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(if (selected) 12.dp else 10.dp)
    Box(
        modifier = modifier
            .then(if (selected) Modifier.width(130.dp) else Modifier.widthIn(max = 120.dp))
            .shadow(5.dp, shape)
            .clip(shape)
            .background(
                if (selected) PaletteTokens.PingoMapSelectionBackground
                else LinkItTheme.color.semantic.background.elevated.normal,
            )
            .border(
                width = 1.dp,
                color = if (selected) PaletteTokens.PingoMapSelectionBorder else PaletteTokens.PingoNeutral100,
                shape = shape,
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 11.dp, vertical = 8.dp),
    ) {
        Text(
            text = text,
            style = LinkItTheme.typography.label2Medium,
            color = PaletteTokens.PingoNeutral700,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun PhotoPlaceMarker(
    resource: DrawableResource,
    modifier: Modifier,
    onClick: () -> Unit,
    selected: Boolean = false,
) {
    val pointerColor = if (selected) LinkItTheme.color.semantic.primary.normal else PaletteTokens.PingoNeutral600
    Column(
        modifier = modifier
            .clickable(onClick = onClick)
            .width(if (selected) 44.dp else 38.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(if (selected) 44.dp else 34.dp)
                .shadow(1.dp, RoundedCornerShape(if (selected) 6.dp else 4.dp))
                .clip(RoundedCornerShape(if (selected) 6.dp else 4.dp))
                .background(PaletteTokens.PingoNeutral50)
                .border(
                    width = 2.dp,
                    color = if (selected) LinkItTheme.color.semantic.primary.normal else PaletteTokens.PingoNeutral600,
                    shape = RoundedCornerShape(if (selected) 6.dp else 4.dp),
                )
                .padding(2.dp),
        ) {
            Image(
                painter = painterResource(resource),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(2.dp)),
            )
        }
        Canvas(Modifier.width(if (selected) 24.dp else 11.dp).height(if (selected) 6.dp else 10.dp)) {
            val pointer = Path().apply {
                moveTo(0f, 0f)
                lineTo(size.width, 0f)
                lineTo(size.width / 2f, size.height)
                close()
            }
            drawPath(
                pointer,
                pointerColor,
            )
        }
    }
}

@Composable
private fun ClusterMarker(count: Int, modifier: Modifier) {
    Box(
        modifier = modifier
            .size(26.dp)
            .clip(CircleShape)
            .background(PaletteTokens.Lavender50),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = count.toString(),
            style = LinkItTheme.typography.body2NormalBold,
            color = LinkItTheme.color.semantic.static.white,
        )
    }
}

@Composable
private fun BoxScope.SavedScheduleSheet(onScheduleClick: () -> Unit) {
    val nanumSquare = rememberNanumSquareFontFamily()
    Column(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .fillMaxWidth()
            .height(394.dp),
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().height(57.dp),
            contentAlignment = Alignment.TopCenter,
        ) {
            Text(
                text = "일본, 도쿄",
                style = LinkItTheme.typography.caption1Bold.copy(fontFamily = nanumSquare),
                color = LinkItTheme.color.semantic.primary.heavy,
                modifier = Modifier
                    .padding(top = 12.dp)
                    .shadow(1.dp, RoundedCornerShape(100.dp))
                    .clip(RoundedCornerShape(100.dp))
                    .background(LinkItTheme.color.semantic.static.white.copy(alpha = .6f))
                    .border(1.dp, LinkItTheme.color.semantic.static.white, RoundedCornerShape(100.dp))
                    .padding(horizontal = 14.dp, vertical = 8.dp),
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(337.dp)
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
                    .padding(start = 20.dp, top = 16.dp, end = 20.dp)
                    .height(24.dp),
            )
            Row(
                modifier = Modifier.padding(start = 20.dp, top = 8.dp, end = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterPill(Res.drawable.map_filter_globe, "지역")
                FilterPill(Res.drawable.map_filter_category, "여행 스타일")
                FilterPill(Res.drawable.map_filter_money, "기간")
            }
            Spacer(Modifier.height(20.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(29.dp)
                    .padding(horizontal = 20.dp, vertical = 5.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    "총 8개 일정",
                    style = LinkItTheme.typography.label2Medium,
                    color = LinkItTheme.color.semantic.label.alternative,
                )
                Text(
                    "최신순",
                    style = LinkItTheme.typography.label2Medium,
                    color = LinkItTheme.color.semantic.label.neutral,
                )
            }
            ScheduleListRow(
                title = "도쿄 신주쿠 여행",
                modifier = Modifier.clickable(onClick = onScheduleClick),
            )
            ScheduleListRow(title = "도쿄 신주쿠 여행")
        }
    }
}

@Composable
private fun BoxScope.SelectedScheduleSheet(
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
        MapLocationPill()
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
                    contentDescription = "뒤로가기",
                    tint = LinkItTheme.color.semantic.label.strong,
                    modifier = Modifier.size(24.dp).clickable(onClick = onBack),
                )
                Text(
                    text = "도쿄 신주쿠 여행",
                    style = LinkItTheme.typography.heading2Bold,
                    color = LinkItTheme.color.semantic.label.strong,
                    modifier = Modifier.padding(start = 4.dp).weight(1f),
                )
                Icon(
                    imageVector = LinkItIcon.Utility.MoreHorizontal,
                    contentDescription = "더보기",
                    tint = LinkItTheme.color.semantic.label.normal,
                    modifier = Modifier.size(24.dp),
                )
            }
            ScheduleListRow(
                title = "도쿄 신주쿠 여행",
                modifier = Modifier.clickable(onClick = onOpenSchedule),
                compact = true,
            )
        }
    }
}

@Composable
private fun MapLocationPill() {
    val nanumSquare = rememberNanumSquareFontFamily()
    Box(
        modifier = Modifier.fillMaxWidth().height(57.dp),
        contentAlignment = Alignment.TopCenter,
    ) {
        Text(
            text = "일본, 도쿄",
            style = LinkItTheme.typography.caption1Bold.copy(fontFamily = nanumSquare),
            color = PaletteTokens.PingoNeutral700,
            modifier = Modifier
                .padding(top = 12.dp)
                .shadow(1.dp, RoundedCornerShape(100.dp))
                .clip(RoundedCornerShape(100.dp))
                .background(LinkItTheme.color.semantic.static.white.copy(alpha = .6f))
                .border(1.dp, LinkItTheme.color.semantic.static.white, RoundedCornerShape(100.dp))
                .padding(horizontal = 14.dp, vertical = 8.dp),
        )
    }
}

@Composable
private fun BoxScope.PlaceInformationCard(
    selectedPlaceIndex: Int,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onClose: () -> Unit,
    navigateToScheduleEdit: () -> Unit,
) {
    Row(
        modifier = Modifier.align(Alignment.BottomEnd).padding(end = 20.dp, bottom = 229.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        CircleArrow(LinkItIcon.Arrow.ChevronLeft, selectedPlaceIndex > 0, onPrevious)
        CircleArrow(LinkItIcon.Arrow.ChevronRight, selectedPlaceIndex < 2, onNext)
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
                text = "2일차 ${selectedPlaceIndex}번째 여행",
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
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(10.dp)),
            )
            Column(modifier = Modifier.padding(start = 12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    PlaceCategoryTag("음식점")
                    PlaceCategoryTag("음식점")
                }
                Text(
                    text = "루브르 박물관",
                    style = LinkItTheme.typography.heading2Bold,
                    color = LinkItTheme.color.semantic.label.strong,
                    modifier = Modifier.padding(top = 4.dp),
                )
                Text(
                    text = "⌖ 뤼 드 리볼리, 75001 파리",
                    style = LinkItTheme.typography.caption2Regular,
                    color = LinkItTheme.color.semantic.label.alternative,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }
        Text(
            text = "세계 각지의 유물이 모여있는 박물관 입니다. 모나리자를 위해 드농 윙에 집중하세요.",
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
            CardAction("일정에서 보기", Modifier.weight(1f), navigateToScheduleEdit)
            CardAction("장소 상세보기", Modifier.weight(1f), navigateToScheduleEdit)
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
            tint = if (enabled) LinkItTheme.color.semantic.label.strong else LinkItTheme.color.semantic.label.disable,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
private fun BoxScope.CreateScheduleControl(
    expanded: Boolean,
    onToggle: () -> Unit,
    onCreateFromVideo: () -> Unit,
) {
    val nanumSquare = rememberNanumSquareFontFamily()
    Column(
        modifier = Modifier.align(Alignment.BottomEnd).padding(end = 20.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (expanded) {
            CreateOption("영상 링크로 만들기", onCreateFromVideo)
            CreateOption("보관함에서 가져오기", onCreateFromVideo)
            CreateOption("직접 만들기", onCreateFromVideo)
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
private fun CreateOption(text: String, onClick: () -> Unit) {
    Text(
        text = text,
        style = LinkItTheme.typography.label1NormalSemibold,
        color = LinkItTheme.color.semantic.label.strong,
        modifier = Modifier
            .shadow(4.dp, RoundedCornerShape(999.dp))
            .clip(RoundedCornerShape(999.dp))
            .background(LinkItTheme.color.semantic.background.elevated.normal)
            .clickable(onClick = onClick)
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
private fun FilterPill(icon: DrawableResource, text: String) {
    val nanumSquare = rememberNanumSquareFontFamily()
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(LinkItTheme.color.semantic.fill.normal)
            .padding(start = 12.dp, top = 8.dp, end = 14.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(icon),
            contentDescription = null,
            modifier = Modifier.size(16.dp),
        )
        Text(
            text = text,
            style = LinkItTheme.typography.label2Bold.copy(fontFamily = nanumSquare),
            color = LinkItTheme.color.semantic.static.black,
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
    title: String,
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
                    MiniTag("맛집 중심")
                    MiniTag("쇼핑 중심")
                }
                Text(
                    text = title,
                    style = LinkItTheme.typography.body2NormalBold,
                    color = LinkItTheme.color.semantic.label.normal,
                    modifier = Modifier.padding(top = 6.dp),
                )
                Row(
                    modifier = Modifier.padding(top = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ScheduleMeta(Res.drawable.map_calendar, "3박4일")
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(12.dp)
                            .background(LinkItTheme.color.semantic.line.solid.normal),
                    )
                    ScheduleMeta(Res.drawable.map_filter_money, "82만원")
                }
                Text(
                    text = "AI 한줄 요약된 여행지 정보",
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
                    contentDescription = "더보기",
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
        Image(
            painter = painterResource(icon),
            contentDescription = null,
            modifier = Modifier.size(16.dp),
        )
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
        modifier = Modifier
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

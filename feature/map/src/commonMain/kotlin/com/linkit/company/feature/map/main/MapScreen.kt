package com.linkit.company.feature.map.main

import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.linkit.company.core.designsystem.foundation.icon.LinkItIcon
import com.linkit.company.core.designsystem.theme.LinkItTheme
import dev.zacsweers.metrox.viewmodel.metroViewModel

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
    val background = if (uiState.mapType == MapType.DEFAULT) {
        LinkItTheme.color.semantic.background.normal.alternative
    } else {
        LinkItTheme.color.semantic.label.neutral
    }
    val road = LinkItTheme.color.semantic.static.white.copy(alpha = 0.92f)
    val minorRoad = LinkItTheme.color.semantic.line.solid.normal
    val primary = LinkItTheme.color.semantic.primary.normal

    Canvas(Modifier.fillMaxSize()) {
        drawRect(background)
        val w = size.width
        val h = size.height
        val roads = listOf(
            Offset(-w * .1f, h * .14f) to Offset(w * 1.1f, h * .72f),
            Offset(w * .05f, -h * .05f) to Offset(w * .82f, h * .9f),
            Offset(w * .45f, -h * .05f) to Offset(w * 1.05f, h * .55f),
            Offset(-w * .05f, h * .56f) to Offset(w * .92f, h * .18f),
            Offset(w * .08f, h * .92f) to Offset(w * .98f, h * .42f),
        )
        roads.forEach { (start, end) ->
            drawLine(road, start, end, strokeWidth = 13f, cap = StrokeCap.Round)
            drawLine(minorRoad, start, end, strokeWidth = 2f, cap = StrokeCap.Round)
        }
        repeat(8) { index ->
            val y = h * (.08f + index * .1f)
            drawLine(
                color = minorRoad.copy(alpha = .75f),
                start = Offset(-20f, y),
                end = Offset(w + 20f, y + if (index % 2 == 0) 42f else -30f),
                strokeWidth = 4f,
            )
        }
        if (uiState.selection != MapSelection.NONE) {
            val area = Path().apply {
                moveTo(w * .13f, h * .36f)
                lineTo(w * .52f, h * .18f)
                lineTo(w * .87f, h * .35f)
                lineTo(w * .72f, h * .66f)
                lineTo(w * .28f, h * .62f)
                close()
            }
            drawPath(area, primary.copy(alpha = .16f))
            drawPath(
                path = area,
                color = primary.copy(alpha = .52f),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.5f),
            )
        }
    }

    Text(
        text = "Street Name",
        style = LinkItTheme.typography.caption2Medium,
        color = LinkItTheme.color.semantic.label.assistive,
        modifier = Modifier
            .align(Alignment.CenterEnd)
            .offset(x = (-24).dp, y = (-72).dp),
    )
}

@Composable
private fun BoxScope.MapTopActions(
    onOpenMyPage: () -> Unit,
    onToggleMapType: () -> Unit,
) {
    Column(
        modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(top = 18.dp, end = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        MapRoundAction(LinkItIcon.Communication.PersonFill, "마이페이지", onOpenMyPage)
        MapRoundAction(LinkItIcon.Location.Map, "지도 종류 변경", onToggleMapType)
    }
}

@Composable
private fun MapRoundAction(
    icon: ImageVector,
    description: String,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .shadow(5.dp, CircleShape)
            .clip(CircleShape)
            .background(LinkItTheme.color.semantic.background.elevated.normal)
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
            text = "도쿄 신주쿠 여행",
            selected = false,
            modifier = Modifier.align(Alignment.TopStart).offset(52.dp, 82.dp),
            onClick = onScheduleClick,
        )
        ScheduleMarker(
            text = "도쿄 하라주쿠 여행",
            selected = false,
            modifier = Modifier.align(Alignment.Center).offset(x = 26.dp, y = (-96).dp),
            onClick = onScheduleClick,
        )
        ClusterMarker(
            count = 2,
            modifier = Modifier.align(Alignment.CenterStart).offset(50.dp, (-76).dp),
        )
    } else {
        ScheduleMarker(
            text = "도쿄 신주쿠 여행",
            selected = true,
            modifier = Modifier.align(Alignment.TopCenter).offset(y = 42.dp),
            onClick = onScheduleClick,
        )
        PlaceMarker("🏮", Modifier.align(Alignment.Center).offset(x = 40.dp, y = (-118).dp), onPlaceClick)
        PlaceMarker("🚃", Modifier.align(Alignment.CenterStart).offset(x = 62.dp, y = (-38).dp), onPlaceClick)
        PlaceMarker("🏯", Modifier.align(Alignment.CenterEnd).offset(x = (-64).dp, y = 44.dp), onPlaceClick)
        PlaceMarker("🌳", Modifier.align(Alignment.Center).offset(x = 12.dp, y = 86.dp), onPlaceClick)
        ClusterMarker(3, Modifier.align(Alignment.CenterEnd).offset(x = (-42).dp, y = 124.dp))
    }
}

@Composable
private fun ScheduleMarker(
    text: String,
    selected: Boolean,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    val primary = LinkItTheme.color.semantic.primary.normal
    Box(
        modifier = modifier
            .shadow(5.dp, RoundedCornerShape(10.dp))
            .clip(RoundedCornerShape(10.dp))
            .background(LinkItTheme.color.semantic.background.elevated.normal)
            .border(
                width = if (selected) 2.dp else 0.dp,
                color = if (selected) primary else Color.Transparent,
                shape = RoundedCornerShape(10.dp),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 11.dp, vertical = 8.dp),
    ) {
        Text(
            text = text,
            style = LinkItTheme.typography.label1NormalSemibold,
            color = LinkItTheme.color.semantic.label.strong,
        )
    }
}

@Composable
private fun PlaceMarker(
    emoji: String,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .size(38.dp)
            .shadow(4.dp, RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
            .background(LinkItTheme.color.semantic.label.strong)
            .clickable(onClick = onClick)
            .padding(3.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(5.dp))
                .background(LinkItTheme.color.semantic.background.elevated.normal),
            contentAlignment = Alignment.Center,
        ) {
            Text(emoji)
        }
    }
}

@Composable
private fun ClusterMarker(count: Int, modifier: Modifier) {
    Box(
        modifier = modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(LinkItTheme.color.semantic.primary.normal),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = count.toString(),
            style = LinkItTheme.typography.label1NormalBold,
            color = LinkItTheme.color.semantic.static.white,
        )
    }
}

@Composable
private fun BoxScope.SavedScheduleSheet(onScheduleClick: () -> Unit) {
    Column(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .fillMaxWidth()
            .height(316.dp)
            .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
            .background(LinkItTheme.color.semantic.background.elevated.normal)
            .padding(horizontal = 20.dp),
    ) {
        SheetHandle()
        Text(
            text = "저장한 일정",
            style = LinkItTheme.typography.heading2Bold,
            color = LinkItTheme.color.semantic.label.strong,
            modifier = Modifier.padding(top = 10.dp),
        )
        Row(
            modifier = Modifier.padding(top = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FilterPill("◉  지역")
            FilterPill("♣  여행 테마")
            FilterPill("＄  비용")
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text("총 60개 일정", style = LinkItTheme.typography.caption1Regular, color = LinkItTheme.color.semantic.label.alternative)
            Text("최신순", style = LinkItTheme.typography.caption1Regular, color = LinkItTheme.color.semantic.label.alternative)
        }
        ScheduleListRow(
            title = "도쿄 신주쿠 여행",
            emoji = "🗻",
            modifier = Modifier.padding(top = 16.dp).clickable(onClick = onScheduleClick),
        )
        ScheduleListRow(
            title = "오사카 미식 여행",
            emoji = "🍜",
            modifier = Modifier.padding(top = 12.dp),
        )
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
            .fillMaxWidth()
            .height(184.dp)
            .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
            .background(LinkItTheme.color.semantic.background.elevated.normal)
            .padding(horizontal = 20.dp),
    ) {
        SheetHandle()
        Row(
            modifier = Modifier.fillMaxWidth().height(48.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = LinkItIcon.Arrow.ChevronLeft,
                contentDescription = "뒤로가기",
                tint = LinkItTheme.color.semantic.label.strong,
                modifier = Modifier.size(22.dp).clickable(onClick = onBack),
            )
            Text(
                text = "도쿄 신주쿠 여행",
                style = LinkItTheme.typography.heading2Semibold,
                color = LinkItTheme.color.semantic.label.strong,
                modifier = Modifier.padding(start = 8.dp).weight(1f),
            )
            Icon(
                imageVector = LinkItIcon.Utility.MoreHorizontal,
                contentDescription = "더보기",
                tint = LinkItTheme.color.semantic.label.neutral,
                modifier = Modifier.size(22.dp),
            )
        }
        ScheduleListRow(
            title = "도쿄 신주쿠 여행",
            emoji = "🗻",
            modifier = Modifier.clickable(onClick = onOpenSchedule),
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
        modifier = Modifier.align(Alignment.BottomEnd).padding(end = 20.dp, bottom = 268.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        CircleArrow(LinkItIcon.Arrow.ChevronLeft, selectedPlaceIndex > 0, onPrevious)
        CircleArrow(LinkItIcon.Arrow.ChevronRight, selectedPlaceIndex < 2, onNext)
    }
    Column(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .padding(horizontal = 20.dp, vertical = 14.dp)
            .fillMaxWidth()
            .height(242.dp)
            .shadow(8.dp, RoundedCornerShape(18.dp))
            .clip(RoundedCornerShape(18.dp))
            .background(LinkItTheme.color.semantic.background.elevated.normal)
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "2일차 ${selectedPlaceIndex + 1}번째 여행",
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
        Row(modifier = Modifier.padding(top = 12.dp)) {
            Box(
                modifier = Modifier
                    .size(78.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(LinkItTheme.color.semantic.accent.background.lime),
                contentAlignment = Alignment.Center,
            ) {
                Text("🌳", style = LinkItTheme.typography.display2Bold)
            }
            Column(modifier = Modifier.padding(start = 12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    MiniTag("박물관")
                    MiniTag("조용한")
                }
                Text(
                    text = "루브르 박물관",
                    style = LinkItTheme.typography.heading2Semibold,
                    color = LinkItTheme.color.semantic.label.strong,
                    modifier = Modifier.padding(top = 7.dp),
                )
                Text(
                    text = "⌖ 뤼 드 리볼리, 75001 파리",
                    style = LinkItTheme.typography.caption2Regular,
                    color = LinkItTheme.color.semantic.label.alternative,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
        Text(
            text = "세계 각지의 유물이 모여있는 박물관 입니다. 모나리자를 위해 느긋하게 둘러보세요.",
            style = LinkItTheme.typography.caption1Regular,
            color = LinkItTheme.color.semantic.label.neutral,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 12.dp),
        )
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            CardAction("일정에서 보기", Modifier.weight(1f), navigateToScheduleEdit)
            CardAction("장소 상세보기", Modifier.weight(1f), navigateToScheduleEdit)
        }
    }
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
    Column(
        modifier = Modifier.align(Alignment.BottomEnd).padding(end = 20.dp, bottom = 18.dp),
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
                .height(48.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(LinkItTheme.color.semantic.inverse.background)
                .clickable(onClick = onToggle)
                .padding(horizontal = 17.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = if (expanded) LinkItIcon.Utility.Close else LinkItIcon.Utility.Ai,
                contentDescription = null,
                tint = LinkItTheme.color.semantic.inverse.label,
                modifier = Modifier.size(20.dp),
            )
            Text(
                text = if (expanded) "닫기" else "일정생성",
                style = LinkItTheme.typography.label1NormalSemibold,
                color = LinkItTheme.color.semantic.inverse.label,
                modifier = Modifier.padding(start = 7.dp),
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
        modifier = Modifier.fillMaxWidth().height(18.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .width(48.dp)
                .height(3.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(LinkItTheme.color.semantic.line.normal.strong),
        )
    }
}

@Composable
private fun FilterPill(text: String) {
    Text(
        text = "$text ⌄",
        style = LinkItTheme.typography.label1NormalSemibold,
        color = LinkItTheme.color.semantic.label.strong,
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(LinkItTheme.color.semantic.fill.normal)
            .padding(horizontal = 12.dp, vertical = 9.dp),
    )
}

@Composable
private fun ScheduleListRow(
    title: String,
    emoji: String,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .size(width = 62.dp, height = 74.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(LinkItTheme.color.semantic.accent.background.cyan),
            contentAlignment = Alignment.Center,
        ) {
            Text(emoji, style = LinkItTheme.typography.title1Bold)
        }
        Column(modifier = Modifier.padding(start = 10.dp).weight(1f)) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                MiniTag("# 먹방여행")
                MiniTag("# SNS 핫플레이스")
            }
            Text(
                text = title,
                style = LinkItTheme.typography.heading2Semibold,
                color = LinkItTheme.color.semantic.label.strong,
                modifier = Modifier.padding(top = 6.dp),
            )
            Text(
                text = "▣ 3박4일    ＄ 82만원",
                style = LinkItTheme.typography.caption1Medium,
                color = LinkItTheme.color.semantic.label.alternative,
                modifier = Modifier.padding(top = 3.dp),
            )
            Text(
                text = "AI 한줄 요약된 여행지 정보",
                style = LinkItTheme.typography.caption2Regular,
                color = LinkItTheme.color.semantic.label.alternative,
            )
        }
        Icon(
            imageVector = LinkItIcon.Utility.MoreHorizontal,
            contentDescription = "더보기",
            tint = LinkItTheme.color.semantic.label.alternative,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
private fun MiniTag(text: String) {
    Text(
        text = text,
        style = LinkItTheme.typography.caption3Medium,
        color = LinkItTheme.color.semantic.label.neutral,
        modifier = Modifier
            .border(1.dp, LinkItTheme.color.semantic.line.normal.normal, RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 3.dp),
    )
}

@Composable
private fun CardAction(text: String, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .height(42.dp)
            .border(1.dp, LinkItTheme.color.semantic.line.normal.normal, RoundedCornerShape(8.dp))
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

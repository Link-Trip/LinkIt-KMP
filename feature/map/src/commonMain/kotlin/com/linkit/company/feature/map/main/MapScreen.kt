package com.linkit.company.feature.map.main

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.linkit.company.core.designsystem.foundation.icon.LinkItIcon
import com.linkit.company.core.designsystem.theme.LinkItTheme
import linkitcompany.feature.map.generated.resources.Res
import linkitcompany.feature.map.generated.resources.main_sheet_schedule_thumbnail_1
import linkitcompany.feature.map.generated.resources.main_sheet_schedule_thumbnail_2
import linkitcompany.feature.map.generated.resources.main_sheet_schedule_thumbnail_3
import linkitcompany.feature.map.generated.resources.main_sheet_schedule_thumbnail_4
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun MapScreen(
    navigateToScheduleEdit: () -> Unit,
) {
    var isScheduleSheetExpanded by remember { mutableStateOf(false) }
    var isScheduleSheetOverlayVisible by remember { mutableStateOf(false) }
    var isDraggingFromCollapsedSheet by remember { mutableStateOf(false) }
    var mapContentHeightPx by remember { mutableStateOf(0) }
    var sheetOffsetPx by remember { mutableStateOf(0f) }
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()
    val sheetOffsetAnimation = remember { Animatable(0f) }
    val collapsedSheetOffsetPx = with(density) {
        (mapContentHeightPx - SavedSchedulePanelHeight.toPx()).coerceAtLeast(0f)
    }

    LaunchedEffect(collapsedSheetOffsetPx) {
        if (collapsedSheetOffsetPx > 0f &&
            !isScheduleSheetExpanded &&
            !isScheduleSheetOverlayVisible
        ) {
            sheetOffsetPx = collapsedSheetOffsetPx
            sheetOffsetAnimation.snapTo(collapsedSheetOffsetPx)
        }
    }

    fun animateScheduleSheet(
        targetOffsetPx: Float,
        onFinished: () -> Unit,
    ) {
        coroutineScope.launch {
            sheetOffsetAnimation.stop()
            sheetOffsetAnimation.snapTo(sheetOffsetPx)
            sheetOffsetAnimation.animateTo(
                targetValue = targetOffsetPx,
                animationSpec = MapSheetAnimationSpec,
            ) {
                sheetOffsetPx = value
            }
            sheetOffsetPx = targetOffsetPx
            onFinished()
        }
    }

    fun settleScheduleSheet(expand: Boolean) {
        if (expand) {
            isDraggingFromCollapsedSheet = false
            isScheduleSheetOverlayVisible = true
            isScheduleSheetExpanded = true
            animateScheduleSheet(targetOffsetPx = 0f, onFinished = {})
        } else {
            isScheduleSheetOverlayVisible = true
            animateScheduleSheet(targetOffsetPx = collapsedSheetOffsetPx) {
                isScheduleSheetExpanded = false
                isDraggingFromCollapsedSheet = false
                isScheduleSheetOverlayVisible = false
            }
        }
    }

    val scheduleSheetDragState = rememberDraggableState { delta ->
        if (collapsedSheetOffsetPx > 0f) {
            sheetOffsetPx = (sheetOffsetPx + delta).coerceIn(0f, collapsedSheetOffsetPx)
        }
    }
    val scheduleSheetDragModifier = Modifier.draggable(
        state = scheduleSheetDragState,
        orientation = Orientation.Vertical,
        startDragImmediately = true,
        onDragStarted = {
            if (collapsedSheetOffsetPx > 0f) {
                isScheduleSheetOverlayVisible = true
                isDraggingFromCollapsedSheet = !isScheduleSheetExpanded
                sheetOffsetPx = if (isScheduleSheetExpanded) 0f else collapsedSheetOffsetPx
                coroutineScope.launch {
                    sheetOffsetAnimation.stop()
                    sheetOffsetAnimation.snapTo(sheetOffsetPx)
                }
            }
        },
        onDragStopped = { velocity ->
            if (collapsedSheetOffsetPx > 0f) {
                val shouldExpand = when {
                    velocity <= -MapSheetSettleVelocityThresholdPx -> true
                    velocity >= MapSheetSettleVelocityThresholdPx -> false
                    else -> sheetOffsetPx < collapsedSheetOffsetPx * MapSheetSettleOffsetRatio
                }
                settleScheduleSheet(expand = shouldExpand)
            }
        },
    )
    val expandedSheetVisible = isScheduleSheetExpanded ||
        (isScheduleSheetOverlayVisible && !isDraggingFromCollapsedSheet)
    val collapsedSheetVisible = !isScheduleSheetExpanded || isDraggingFromCollapsedSheet
    val collapsedSheetDragOffsetPx = sheetOffsetPx - collapsedSheetOffsetPx

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { mapContentHeightPx = it.height }
            .background(LinkItTheme.color.semantic.background.normal.alternative),
    ) {
        val spacing = LinkItTheme.spacing

        GoogleMapBackground(
            modifier = Modifier.fillMaxSize(),
            markers = MainMapMarkers,
        )

        if (expandedSheetVisible) {
            ExpandedScheduleSheet(
                onCollapse = { settleScheduleSheet(expand = false) },
                onCreateSchedule = navigateToScheduleEdit,
                headerDragModifier = scheduleSheetDragModifier,
                modifier = Modifier
                    .fillMaxSize()
                    .offset {
                        IntOffset(
                            x = 0,
                            y = sheetOffsetPx.roundToInt(),
                        )
                    },
            )
        }

        if (collapsedSheetVisible) {
            TopFloatingActions(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(top = 20.dp, end = 16.dp),
            )

            LocationChip(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = SavedSchedulePanelHeight + spacing.space12),
            )

            SavedSchedulePanel(
                onExpandRequest = { settleScheduleSheet(expand = true) },
                dragModifier = scheduleSheetDragModifier,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(SavedSchedulePanelHeight)
                    .offset {
                        IntOffset(
                            x = 0,
                            y = collapsedSheetDragOffsetPx.roundToInt(),
                        )
                    },
            )

            CreateScheduleFloatingButton(
                onClick = navigateToScheduleEdit,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = spacing.space20, bottom = spacing.space8)
                    .offset {
                        IntOffset(
                            x = 0,
                            y = collapsedSheetDragOffsetPx.roundToInt(),
                        )
                    },
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun MapScreenPreview() {
    LinkItTheme {
        MapScreen(navigateToScheduleEdit = {})
    }
}

@Composable
private fun CreateScheduleFloatingButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val atomic = LinkItTheme.color.atomic
    val spacing = LinkItTheme.spacing
    Row(
        modifier = modifier
            .height(spacing.space40)
            .widthIn(min = CreateScheduleButtonMinWidth)
            .clip(LinkItTheme.shape.rounded)
            .background(atomic.Neutral600)
            .clickable(onClick = onClick)
            .padding(
                start = spacing.space8,
                top = spacing.space4,
                end = spacing.space12,
                bottom = spacing.space4,
            ),
        horizontalArrangement = Arrangement.spacedBy(spacing.space4, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(spacing.space20),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = LinkItIcon.Control.CreateSchedule,
                contentDescription = null,
                modifier = Modifier.size(CreateScheduleButtonIconSize),
                tint = atomic.BlueGray95,
            )
        }
        Text(
            text = "일정 생성",
            style = LinkItTheme.typography.label1NormalMedium,
            color = atomic.BlueGray95,
            maxLines = 1,
        )
    }
}

@Composable
internal fun ScheduleMarkerChip(
    text: String,
    modifier: Modifier = Modifier,
) {
    val semantic = LinkItTheme.color.semantic
    Box(
        modifier = modifier
            .height(34.dp)
            .shadow(4.dp, RoundedCornerShape(17.dp))
            .clip(RoundedCornerShape(17.dp))
            .background(semantic.static.white)
            .border(1.dp, semantic.line.normal.alternative, RoundedCornerShape(17.dp))
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = LinkItTheme.typography.caption1Semibold,
            color = semantic.label.normal,
            maxLines = 1,
        )
    }
}

@Composable
internal fun CountMarker(
    count: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(width = 28.dp, height = 26.dp)
            .shadow(4.dp, RoundedCornerShape(13.dp))
            .clip(RoundedCornerShape(13.dp))
            .background(LinkItTheme.color.semantic.primary.normal),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = count,
            style = LinkItTheme.typography.caption1Bold,
            color = LinkItTheme.color.semantic.static.white,
            maxLines = 1,
        )
    }
}

@Composable
internal fun PlaceMarker(
    modifier: Modifier = Modifier,
) {
    val semantic = LinkItTheme.color.semantic
    Box(
        modifier = modifier
            .size(32.dp)
            .shadow(4.dp, CircleShape)
            .clip(CircleShape)
            .background(semantic.static.white)
            .border(1.dp, semantic.line.normal.alternative, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = LinkItIcon.Location.LocationFill,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = semantic.primary.normal,
        )
    }
}

@Composable
private fun LocationChip(
    modifier: Modifier = Modifier,
) {
    val atomic = LinkItTheme.color.atomic
    Box(
        modifier = modifier
            .shadow(1.dp, RoundedCornerShape(100.dp))
            .clip(RoundedCornerShape(100.dp))
            .background(atomic.White.copy(alpha = atomic.Opacity60))
            .border(1.dp, atomic.White, RoundedCornerShape(100.dp))
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "일본, 도쿄",
            style = LinkItTheme.typography.caption1Bold.copy(letterSpacing = 0.sp),
            color = atomic.BlueGray20,
            maxLines = 1,
        )
    }
}

@Composable
private fun SavedSchedulePanel(
    onExpandRequest: () -> Unit,
    dragModifier: Modifier,
    modifier: Modifier = Modifier,
) {
    val semantic = LinkItTheme.color.semantic
    val spacing = LinkItTheme.spacing
    Box(
        modifier = modifier
            .shadow(10.dp, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .background(semantic.background.normal.normal)
            .then(dragModifier),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(semantic.background.normal.normal),
        ) {
            BottomSheetHandle(
                modifier = Modifier.clickable(onClick = onExpandRequest),
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(spacing.space16),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(spacing.space8),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .padding(horizontal = spacing.space20),
                        contentAlignment = Alignment.BottomStart,
                    ) {
                        Text(
                            text = "저장한 일정",
                            style = LinkItTheme.typography.body2NormalBold,
                            color = semantic.label.normal,
                            maxLines = 1,
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(34.dp)
                            .padding(horizontal = spacing.space20),
                        horizontalArrangement = Arrangement.spacedBy(spacing.space8),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        ScheduleFilterChips()
                    }
                }
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(spacing.space4),
                ) {
                    DetailInfo()
                    SavedScheduleList(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentPadding = PaddingValues(bottom = CreateButtonPanelReserve),
                        userScrollEnabled = false,
                    )
                }
            }
        }
    }
}

@Composable
private fun ExpandedScheduleSheet(
    onCollapse: () -> Unit,
    onCreateSchedule: () -> Unit,
    headerDragModifier: Modifier,
    modifier: Modifier = Modifier,
) {
    val atomic = LinkItTheme.color.atomic
    Box(
        modifier = modifier
            .background(atomic.White),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(atomic.White),
        ) {
            ExpandedScheduleTopBar(
                onCollapse = onCollapse,
                dragModifier = headerDragModifier,
            )
            ExpandedScheduleContent(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            )
        }
        CreateScheduleIconButton(
            onClick = onCreateSchedule,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 8.dp),
        )
    }
}

@Composable
private fun ExpandedScheduleTopBar(
    onCollapse: () -> Unit,
    dragModifier: Modifier,
    modifier: Modifier = Modifier,
) {
    val atomic = LinkItTheme.color.atomic
    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(statusBarHeight + ExpandedScheduleTopBarHeight)
            .background(atomic.White)
            .then(dragModifier),
    ) {
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .height(ExpandedScheduleTopBarHeight)
                .padding(start = 12.dp, top = 8.dp, end = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(width = 24.dp, height = 44.dp)
                    .clickable(onClick = onCollapse),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = LinkItIcon.Arrow.ChevronLeft,
                    contentDescription = "지도 화면으로 돌아가기",
                    modifier = Modifier.size(24.dp),
                    tint = MapExpandedHeaderTextColor,
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                Text(
                    text = "일정",
                    style = LinkItTheme.typography.headline1Bold.copy(
                        lineHeight = 24.sp,
                        letterSpacing = 0.sp,
                    ),
                    color = MapExpandedHeaderTextColor,
                    maxLines = 1,
                )
            }
        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(1.dp)
                .background(atomic.CoolNeutral98),
        )
    }
}

@Composable
private fun ExpandedScheduleContent(
    modifier: Modifier = Modifier,
) {
    val atomic = LinkItTheme.color.atomic
    Column(
        modifier = modifier
            .background(atomic.White),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp)
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ScheduleFilterChips()
        }
        Spacer(modifier = Modifier.height(20.dp))
        DetailInfo()
        Spacer(modifier = Modifier.height(8.dp))
        SavedScheduleList(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(bottom = ExpandedScheduleListBottomPadding),
        )
    }
}

@Composable
private fun ScheduleFilterChips() {
    FilterChip(text = "지역", icon = LinkItIcon.Utility.Globe)
    FilterChip(text = "여행 스타일", icon = LinkItIcon.Utility.Category)
    FilterChip(text = "기간", icon = LinkItIcon.Utility.AttachMoney)
}

@Composable
private fun SavedScheduleList(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues,
    userScrollEnabled: Boolean = true,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = contentPadding,
        userScrollEnabled = userScrollEnabled,
    ) {
        items(SavedScheduleItems) { item ->
            SavedScheduleCard(item = item)
        }
    }
}

@Composable
private fun CreateScheduleIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val atomic = LinkItTheme.color.atomic
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        atomic.Neutral600,
                        MapFloatingButtonGradientEnd,
                    ),
                ),
                shape = CircleShape,
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = LinkItIcon.Control.CreateSchedule,
            contentDescription = "일정 생성",
            modifier = Modifier.size(24.dp),
            tint = atomic.BlueGray95,
        )
    }
}

@Composable
private fun FilterChip(
    text: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
) {
    val semantic = LinkItTheme.color.semantic
    val spacing = LinkItTheme.spacing
    Row(
        modifier = modifier
            .height(34.dp)
            .clip(LinkItTheme.shape.rounded)
            .background(semantic.fill.normal)
            .padding(start = spacing.space12, end = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spacing.space4),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = Color.Unspecified,
        )
        Text(
            text = text,
            style = LinkItTheme.typography.label2Bold.copy(letterSpacing = 0.sp),
            color = semantic.static.black,
            maxLines = 1,
        )
        Icon(
            imageVector = LinkItIcon.Arrow.ChevronDownSmall,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = semantic.static.black,
        )
    }
}

@Composable
private fun SavedScheduleCard(
    item: SavedScheduleItem,
    modifier: Modifier = Modifier,
) {
    val atomic = LinkItTheme.color.atomic
    val semantic = LinkItTheme.color.semantic
    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(125.dp)
            .background(semantic.background.normal.normal),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Image(
                painter = painterResource(item.thumbnail),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(width = 80.dp, height = 100.dp)
                    .clip(RoundedCornerShape(8.dp)),
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        item.badges.forEach { badge ->
                            ContentBadge(text = badge)
                        }
                    }
                    Icon(
                        imageVector = LinkItIcon.Utility.MoreHorizontal,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = semantic.label.normal,
                    )
                }
                Text(
                    text = item.title,
                    style = LinkItTheme.typography.body2NormalBold.copy(letterSpacing = 0.sp),
                    color = MapExpandedNeutral800,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        ScheduleMeta(icon = LinkItIcon.Utility.Calendar, text = item.duration)
                        Box(
                            modifier = Modifier
                                .size(width = 1.dp, height = 20.dp)
                                .padding(vertical = 4.dp)
                                .background(semantic.line.solid.normal),
                        )
                        ScheduleMeta(icon = LinkItIcon.Utility.Money, text = item.price)
                    }
                    Text(
                        text = item.summary,
                        style = LinkItTheme.typography.caption1Bold.copy(
                            lineHeight = 17.4f.sp,
                            letterSpacing = 0.sp,
                        ),
                        color = MapExpandedNeutral400,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(1.dp)
                .background(atomic.BlueGray95),
        )
    }
}

@Composable
private fun BottomSheetHandle(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(15.dp)
            .padding(top = 8.dp, bottom = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(width = 48.dp, height = 3.dp)
                .clip(LinkItTheme.shape.rounded)
                .background(LinkItTheme.color.atomic.CoolNeutral50.copy(alpha = 0.2f)),
        )
    }
}

@Composable
private fun DetailInfo(
    modifier: Modifier = Modifier,
) {
    val semantic = LinkItTheme.color.semantic
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(29.dp)
            .padding(horizontal = 20.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "총 8개 일정",
            style = LinkItTheme.typography.label2Medium.copy(letterSpacing = 0.sp),
            color = semantic.label.alternative,
            maxLines = 1,
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "최신순",
            style = LinkItTheme.typography.label2Medium.copy(letterSpacing = 0.sp),
            color = MapExpandedNeutral400,
            maxLines = 1,
        )
    }
}

@Composable
private fun ContentBadge(
    text: String,
    modifier: Modifier = Modifier,
) {
    val semantic = LinkItTheme.color.semantic
    Text(
        text = text,
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .border(1.dp, semantic.line.normal.neutral, RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 5.dp),
        style = LinkItTheme.typography.caption2Medium.copy(letterSpacing = 0.sp),
        color = semantic.label.alternative,
        maxLines = 1,
    )
}

@Composable
private fun ScheduleMeta(
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MapExpandedNeutral300,
        )
        Text(
            text = text,
            style = LinkItTheme.typography.caption1Bold.copy(
                lineHeight = 16.2f.sp,
                letterSpacing = 0.sp,
            ),
            color = MapExpandedNeutral300,
            maxLines = 1,
        )
    }
}

@Composable
private fun TopFloatingActions(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        MapFloatingActionButton(
            icon = LinkItIcon.Communication.PersonFill,
            contentDescription = "마이페이지",
        )
        MapFloatingActionButton(
            icon = LinkItIcon.Location.Map,
            contentDescription = "지도 유형",
        )
    }
}

@Composable
private fun MapFloatingActionButton(
    icon: ImageVector,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    val semantic = LinkItTheme.color.semantic
    Box(
        modifier = modifier
            .size(40.dp)
            .shadow(1.dp, CircleShape)
            .clip(CircleShape)
            .background(semantic.background.normal.normal)
            .border(1.dp, semantic.line.normal.neutral, CircleShape)
            .clickable(onClick = {}),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(20.dp),
            tint = semantic.label.normal,
        )
    }
}

private val SavedSchedulePanelHeight = 360.dp
private val ExpandedScheduleTopBarHeight = 52.dp
private val ExpandedScheduleListBottomPadding = 64.dp
private val CreateScheduleButtonMinWidth = 101.dp
private val CreateScheduleButtonIconSize = 15.dp
private val CreateButtonPanelReserve = 64.dp
private val MapExpandedHeaderTextColor = Color(0xFF3E3E3E)
private val MapExpandedNeutral800 = Color(0xFF17191F)
private val MapExpandedNeutral400 = Color(0xFF5D6470)
private val MapExpandedNeutral300 = Color(0xFF7B8696)
private val MapFloatingButtonGradientEnd = Color(0xFF77859E)
private val MapSheetAnimationSpec = spring<Float>(
    dampingRatio = Spring.DampingRatioNoBouncy,
    stiffness = Spring.StiffnessMediumLow,
)
private const val MapSheetSettleVelocityThresholdPx = 1_200f
private const val MapSheetSettleOffsetRatio = 0.58f

private data class SavedScheduleItem(
    val thumbnail: DrawableResource,
    val title: String,
    val badges: List<String>,
    val duration: String,
    val price: String,
    val summary: String,
)

private val SavedScheduleItems = listOf(
    SavedScheduleItem(
        thumbnail = Res.drawable.main_sheet_schedule_thumbnail_1,
        title = "도쿄 신주쿠 여행",
        badges = listOf("맛집 중심", "쇼핑 중심"),
        duration = "3박4일",
        price = "82만원",
        summary = "AI 한줄 요약된 여행지 정보",
    ),
    SavedScheduleItem(
        thumbnail = Res.drawable.main_sheet_schedule_thumbnail_2,
        title = "도쿄 신주쿠 여행",
        badges = listOf("맛집 중심", "쇼핑 중심"),
        duration = "3박4일",
        price = "82만원",
        summary = "AI 한줄 요약된 여행지 정보",
    ),
    SavedScheduleItem(
        thumbnail = Res.drawable.main_sheet_schedule_thumbnail_3,
        title = "도쿄 신주쿠 여행",
        badges = listOf("맛집 중심", "쇼핑 중심"),
        duration = "3박4일",
        price = "82만원",
        summary = "AI 한줄 요약된 여행지 정보",
    ),
    SavedScheduleItem(
        thumbnail = Res.drawable.main_sheet_schedule_thumbnail_4,
        title = "도쿄 신주쿠 여행",
        badges = listOf("맛집 중심", "쇼핑 중심"),
        duration = "3박4일",
        price = "82만원",
        summary = "AI 한줄 요약된 여행지 정보",
    ),
)

internal enum class MainMapMarkerType {
    Schedule,
    Place,
    Count,
}

internal data class MainMapMarker(
    val id: String,
    val type: MainMapMarkerType,
    val latitude: Double,
    val longitude: Double,
    val title: String,
    val count: String? = null,
)

internal val MainMapMarkers = listOf(
    MainMapMarker(
        id = "schedule-harajuku-west",
        type = MainMapMarkerType.Schedule,
        latitude = 35.6719,
        longitude = 139.7026,
        title = "도쿄 하라주쿠 여행",
    ),
    MainMapMarker(
        id = "schedule-shinjuku",
        type = MainMapMarkerType.Schedule,
        latitude = 35.6909,
        longitude = 139.7003,
        title = "도쿄 신주쿠 여행",
    ),
    MainMapMarker(
        id = "schedule-harajuku-east",
        type = MainMapMarkerType.Schedule,
        latitude = 35.6692,
        longitude = 139.7076,
        title = "도쿄 하라주쿠 여행",
    ),
    MainMapMarker(
        id = "schedule-yoyogi",
        type = MainMapMarkerType.Schedule,
        latitude = 35.6762,
        longitude = 139.6956,
        title = "도쿄 하라주쿠 여행",
    ),
    MainMapMarker(
        id = "cluster-harajuku",
        type = MainMapMarkerType.Count,
        latitude = 35.6697,
        longitude = 139.7042,
        title = "겹친 일정",
        count = "2",
    ),
    MainMapMarker(
        id = "place-takeshita",
        type = MainMapMarkerType.Place,
        latitude = 35.6721,
        longitude = 139.7038,
        title = "다케시타 거리",
    ),
    MainMapMarker(
        id = "place-meiji",
        type = MainMapMarkerType.Place,
        latitude = 35.6764,
        longitude = 139.6993,
        title = "메이지 신궁",
    ),
    MainMapMarker(
        id = "place-omotesando",
        type = MainMapMarkerType.Place,
        latitude = 35.6652,
        longitude = 139.7121,
        title = "오모테산도",
    ),
)

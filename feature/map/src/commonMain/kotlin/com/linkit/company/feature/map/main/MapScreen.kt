package com.linkit.company.feature.map.main

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.linkit.company.core.designsystem.component.button.ButtonSize
import com.linkit.company.core.designsystem.component.button.LinkItButton
import com.linkit.company.core.designsystem.foundation.icon.LinkItIcon
import com.linkit.company.core.designsystem.theme.LinkItTheme

@Composable
fun MapScreen(
    navigateToScheduleEdit: () -> Unit,
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(LinkItTheme.color.semantic.background.normal.alternative),
    ) {
        val widthScale = (maxWidth.value / FigmaScreenWidth).coerceIn(0.88f, 1.2f)
        val panelHeight = (maxHeight.value * PanelHeightRatio).dp.coerceIn(300.dp, 360.dp)

        GoogleMapBackground(modifier = Modifier.fillMaxSize())

        MapMarkerLayer(widthScale = widthScale)

        TopFloatingActions(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 20.dp, end = 16.dp),
        )

        LocationChip(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = panelHeight + 12.dp),
        )

        SavedSchedulePanel(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(panelHeight),
        )

        LinkItButton(
            onClick = navigateToScheduleEdit,
            text = "일정 생성",
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 24.dp)
                .height(40.dp)
                .widthIn(min = 101.dp),
            size = ButtonSize.Medium,
            shadow = true,
        )
    }
}

@Composable
private fun MapMarkerLayer(widthScale: Float) {
    PlaceMarker(
        modifier = Modifier.offset(x = figmaX(224f, widthScale), y = figmaY(132f, widthScale)),
    )
    PlaceMarker(
        modifier = Modifier.offset(x = figmaX(117f, widthScale), y = figmaY(298f, widthScale)),
    )
    PlaceMarker(
        modifier = Modifier.offset(x = figmaX(300f, widthScale), y = figmaY(182f, widthScale)),
    )

    ScheduleMarkerChip(
        text = "도쿄 하라주쿠 여행",
        modifier = Modifier.offset(x = figmaX(51f, widthScale), y = figmaY(80f, widthScale)),
    )
    ScheduleMarkerChip(
        text = "도쿄 신주쿠 여행",
        modifier = Modifier.offset(x = figmaX(229f, widthScale), y = figmaY(254f, widthScale)),
    )
    ScheduleMarkerChip(
        text = "도쿄 하라주쿠 여행",
        modifier = Modifier.offset(x = figmaX(73f, widthScale), y = figmaY(214f, widthScale)),
    )
    ScheduleMarkerChip(
        text = "도쿄 하라주쿠 여행",
        modifier = Modifier.offset(x = figmaX(161f, widthScale), y = figmaY(154f, widthScale)),
    )
    CountMarker(
        count = "2",
        modifier = Modifier.offset(x = figmaX(50f, widthScale), y = figmaY(199f, widthScale)),
    )
}

@Composable
private fun ScheduleMarkerChip(
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
private fun CountMarker(
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
private fun PlaceMarker(
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

private fun figmaX(value: Float, widthScale: Float): Dp = (value * widthScale).dp

private fun figmaY(value: Float, widthScale: Float): Dp = ((value - FigmaStatusBarHeight).coerceAtLeast(0f) * widthScale).dp

@Composable
private fun LocationChip(
    modifier: Modifier = Modifier,
) {
    val semantic = LinkItTheme.color.semantic
    Row(
        modifier = modifier
            .height(32.dp)
            .shadow(4.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(semantic.static.white)
            .border(1.dp, semantic.line.normal.alternative, RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(
            imageVector = LinkItIcon.Location.LocationFill,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = semantic.primary.normal,
        )
        Text(
            text = "일본, 도쿄",
            style = LinkItTheme.typography.caption1Semibold,
            color = semantic.label.normal,
            maxLines = 1,
        )
        Icon(
            imageVector = LinkItIcon.Arrow.ChevronDownSmall,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = semantic.label.alternative,
        )
    }
}

@Composable
private fun SavedSchedulePanel(
    modifier: Modifier = Modifier,
) {
    val semantic = LinkItTheme.color.semantic
    Box(
        modifier = modifier
            .shadow(16.dp, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(semantic.background.normal.normal),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 20.dp, top = 12.dp, end = 20.dp, bottom = 20.dp),
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 42.dp, height = 4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(semantic.line.normal.neutral),
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "저장한 일정",
                style = LinkItTheme.typography.title3Bold,
                color = semantic.label.normal,
                maxLines = 1,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FilterChip(text = "지역")
                FilterChip(text = "여행 스타일")
                FilterChip(text = "기간")
            }

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "총 8개 일정",
                    style = LinkItTheme.typography.label1NormalSemibold,
                    color = semantic.label.normal,
                    maxLines = 1,
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "최신순",
                    style = LinkItTheme.typography.caption1Medium,
                    color = semantic.label.alternative,
                    maxLines = 1,
                )
                Icon(
                    imageVector = LinkItIcon.Arrow.ChevronDownSmall,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = semantic.label.alternative,
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = CreateButtonPanelReserve),
            ) {
                item {
                    SavedScheduleCard(
                        title = "도쿄 신주쿠 여행",
                        styleTag = "맛집 중심",
                        duration = "3박4일",
                        price = "82만원",
                        thumbnailBrush = Brush.linearGradient(
                            listOf(
                                semantic.accent.background.lightBlue,
                                semantic.accent.background.lime,
                            ),
                        ),
                    )
                }
                item {
                    SavedScheduleCard(
                        title = "도쿄 하라주쿠 여행",
                        styleTag = "쇼핑 중심",
                        duration = "2박3일",
                        price = "64만원",
                        thumbnailBrush = Brush.linearGradient(
                            listOf(
                                semantic.accent.background.pink,
                                semantic.accent.background.cyan,
                            ),
                        ),
                    )
                }
            }
        }
    }
}

@Composable
private fun FilterChip(
    text: String,
    modifier: Modifier = Modifier,
) {
    val semantic = LinkItTheme.color.semantic
    Row(
        modifier = modifier
            .height(32.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(semantic.fill.normal)
            .padding(start = 12.dp, end = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = text,
            style = LinkItTheme.typography.caption1Medium,
            color = semantic.label.neutral,
            maxLines = 1,
        )
        Icon(
            imageVector = LinkItIcon.Arrow.ChevronDownSmall,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = semantic.label.alternative,
        )
    }
}

@Composable
private fun SavedScheduleCard(
    title: String,
    styleTag: String,
    duration: String,
    price: String,
    thumbnailBrush: Brush,
    modifier: Modifier = Modifier,
) {
    val semantic = LinkItTheme.color.semantic
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(76.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(semantic.background.elevated.normal)
            .border(1.dp, semantic.line.normal.normal, RoundedCornerShape(8.dp))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .width(64.dp)
                .fillMaxHeight()
                .clip(RoundedCornerShape(8.dp))
                .background(thumbnailBrush),
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SmallTag(text = styleTag)
                SmallTag(text = duration)
            }
            Text(
                text = title,
                style = LinkItTheme.typography.label1NormalBold,
                color = semantic.label.normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "$price · AI 한줄 요약된 여행지 정보",
                style = LinkItTheme.typography.caption1Regular,
                color = semantic.label.alternative,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun SmallTag(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(LinkItTheme.color.semantic.fill.alternative)
            .padding(horizontal = 7.dp, vertical = 3.dp),
        style = LinkItTheme.typography.caption1Medium,
        color = LinkItTheme.color.semantic.label.neutral,
        maxLines = 1,
    )
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
            .shadow(6.dp, CircleShape)
            .clip(CircleShape)
            .background(semantic.static.white)
            .border(1.dp, semantic.line.normal.alternative, CircleShape)
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

private const val FigmaScreenWidth = 375f
private const val FigmaStatusBarHeight = 38f
private const val PanelHeightRatio = 0.49f
private val CreateButtonPanelReserve = 64.dp

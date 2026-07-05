package com.linkit.company.feature.map.main

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
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

        GoogleMapBackground(modifier = Modifier.fillMaxSize())

        MapMarkerLayer(widthScale = widthScale)

        TopFloatingActions(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 20.dp, end = 16.dp),
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

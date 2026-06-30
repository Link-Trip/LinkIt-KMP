package com.linkit.company.core.designsystem.component.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow

/**
 * 화면 상단에 위치하는 내비게이션 바.
 *
 * Figma "Pingo v3.0.3 - Navigation / Top Navigation" 을 구현한 것으로, [variant] 에 따라 제목 정렬과
 * 타이포그래피가 달라진다. Leading/Trailing 영역은 슬롯으로 열려 있으며, 표준 뒤로 가기 버튼은
 * [TopNavigationDefaults.BackButton] 으로 채울 수 있다.
 *
 * @param title 표시할 제목. [TopNavigationVariant.Floating] 에서는 무시된다.
 * @param variant 바의 표시 형태. 기본값은 [TopNavigationVariant.Normal].
 * @param navigationIcon 좌측(Leading) 슬롯. 보통 [TopNavigationDefaults.BackButton] 을 넣는다.
 * @param actions 우측(Trailing) 슬롯. [RowScope] 에서 액션 아이콘들을 가로로 배치한다.
 */
@Composable
fun LinkItTopNavigation(
    title: String = "",
    modifier: Modifier = Modifier,
    variant: TopNavigationVariant = TopNavigationVariant.Normal,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    containerColor: Color = TopNavigationDefaults.containerColor,
    titleColor: Color = TopNavigationDefaults.titleColor,
    titleStyle: TextStyle = TopNavigationDefaults.titleStyle(variant),
) {
    val barModifier = modifier
        .fillMaxWidth()
        .height(TopNavigationDefaults.Height)
        .background(containerColor)
        .padding(horizontal = TopNavigationDefaults.HorizontalPadding)

    when (variant) {
        TopNavigationVariant.Normal -> CenteredTopNavigation(
            title = title,
            titleColor = titleColor,
            titleStyle = titleStyle,
            navigationIcon = navigationIcon,
            actions = actions,
            modifier = barModifier,
        )

        TopNavigationVariant.Extended,
        TopNavigationVariant.Reduced,
        -> StartTopNavigation(
            title = title,
            titleColor = titleColor,
            titleStyle = titleStyle,
            navigationIcon = navigationIcon,
            actions = actions,
            modifier = barModifier,
        )

        TopNavigationVariant.Floating -> FloatingTopNavigation(
            navigationIcon = navigationIcon,
            actions = actions,
            modifier = barModifier,
        )
    }
}

/** Normal: 제목을 가운데 정렬하고 Leading/Trailing 을 양 끝에 겹쳐 배치한다. */
@Composable
private fun CenteredTopNavigation(
    title: String,
    titleColor: Color,
    titleStyle: TextStyle,
    navigationIcon: @Composable () -> Unit,
    actions: @Composable RowScope.() -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        Text(
            text = title,
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .padding(horizontal = TopNavigationDefaults.IconButtonSize),
            color = titleColor,
            style = titleStyle,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        LeadingSlot(navigationIcon, Modifier.align(Alignment.CenterStart))
        TrailingSlot(actions, Modifier.align(Alignment.CenterEnd))
    }
}

/** Extended/Reduced: Leading 옆에 제목을 왼쪽 정렬하고 Trailing 을 끝에 배치한다. */
@Composable
private fun StartTopNavigation(
    title: String,
    titleColor: Color,
    titleStyle: TextStyle,
    navigationIcon: @Composable () -> Unit,
    actions: @Composable RowScope.() -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(TopNavigationDefaults.LeadingTitleSpacing),
    ) {
        navigationIcon()
        Text(
            text = title,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = TopNavigationDefaults.TitleHorizontalPadding),
            color = titleColor,
            style = titleStyle,
            textAlign = TextAlign.Start,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        actions()
    }
}

/** Floating: 제목 없이 Leading 과 Trailing 만 양 끝에 배치한다. */
@Composable
private fun FloatingTopNavigation(
    navigationIcon: @Composable () -> Unit,
    actions: @Composable RowScope.() -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        navigationIcon()
        Spacer(Modifier.weight(1f))
        actions()
    }
}

@Composable
private fun BoxScope.LeadingSlot(
    navigationIcon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) { navigationIcon() }
}

@Composable
private fun BoxScope.TrailingSlot(
    actions: @Composable RowScope.() -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        content = actions,
    )
}

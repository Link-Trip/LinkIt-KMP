package com.linkit.company.core.designsystem.component.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.linkit.company.core.designsystem.theme.LinkItTheme

/**
 * [LinkItBottomNavigation] 의 기본 값을 제공한다. Material3 의 `NavigationBarDefaults` 와 동일한 역할.
 */
object BottomNavigationDefaults {

    /** 탭 아이콘 크기. */
    val IconSize: Dp = 24.dp

    /** 하단 시스템 바(홈 인디케이터) 영역을 위한 기본 [WindowInsets]. */
    val windowInsets: WindowInsets
        @Composable
        get() = WindowInsets.navigationBars

    val containerColor: Color
        @Composable
        @ReadOnlyComposable
        get() = LinkItTheme.color.semantic.background.normal.normal

    val dividerColor: Color
        @Composable
        @ReadOnlyComposable
        get() = LinkItTheme.color.semantic.line.normal.neutral

    /** 선택된 탭의 아이콘·라벨 색. */
    val selectedColor: Color
        @Composable
        @ReadOnlyComposable
        get() = LinkItTheme.color.semantic.label.normal

    /** 선택되지 않은 탭의 아이콘·라벨 색. */
    val unselectedColor: Color
        @Composable
        @ReadOnlyComposable
        get() = LinkItTheme.color.semantic.interaction.inactive

    val labelStyle: TextStyle
        @Composable
        @ReadOnlyComposable
        get() = LinkItTheme.typography.caption2Medium

    /** 상단 구분선 두께. */
    fun dividerThickness(platform: BottomNavigationPlatform): Dp = when (platform) {
        BottomNavigationPlatform.iOS -> 0.5.dp
        BottomNavigationPlatform.Android -> 1.dp
    }

    /** 아이콘과 라벨 사이 간격. */
    fun itemContentGap(platform: BottomNavigationPlatform): Dp = when (platform) {
        BottomNavigationPlatform.iOS -> 3.dp
        BottomNavigationPlatform.Android -> 6.dp
    }

    /** 탭의 상하 여백. */
    fun itemVerticalPadding(platform: BottomNavigationPlatform): Dp = when (platform) {
        BottomNavigationPlatform.iOS -> 4.dp
        BottomNavigationPlatform.Android -> 9.dp
    }
}

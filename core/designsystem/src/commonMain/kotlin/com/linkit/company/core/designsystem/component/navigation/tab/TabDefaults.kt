package com.linkit.company.core.designsystem.component.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.linkit.company.core.designsystem.theme.LinkItTheme

/**
 * [LinkItTabRow]·[LinkItTab] 의 기본 값을 제공한다. Material3 의 `TabRowDefaults` 와 동일한 역할.
 */
object TabDefaults {

    /** 선택 인디케이터(언더라인) 두께. */
    val IndicatorThickness: Dp = 2.dp

    /** [LinkItTabRow] 의 `horizontalPadding = true` 일 때 좌우에 적용하는 여백. */
    val HorizontalPadding: Dp = 20.dp

    /** 탭 하나의 최소 너비. */
    val MinTabWidth: Dp = 32.dp

    val containerColor: Color
        @Composable
        @ReadOnlyComposable
        get() = Color.Transparent

    /** 선택된 탭의 텍스트·인디케이터 색. */
    val selectedColor: Color
        @Composable
        @ReadOnlyComposable
        get() = LinkItTheme.color.semantic.label.strong

    /** 선택되지 않은 탭의 텍스트 색. */
    val unselectedColor: Color
        @Composable
        @ReadOnlyComposable
        get() = LinkItTheme.color.semantic.label.assistive

    /** 비활성(disabled) 탭의 텍스트 색. */
    val disabledColor: Color
        @Composable
        @ReadOnlyComposable
        get() = LinkItTheme.color.semantic.label.disable

    val dividerColor: Color
        @Composable
        @ReadOnlyComposable
        get() = LinkItTheme.color.semantic.line.normal.alternative

    val textStyle: TextStyle
        @Composable
        @ReadOnlyComposable
        get() = LinkItTheme.typography.body2NormalBold

    fun height(size: TabSize): Dp = when (size) {
        TabSize.Medium -> 48.dp
        TabSize.Small -> 40.dp
    }
}

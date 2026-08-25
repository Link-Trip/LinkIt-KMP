package com.linkit.company.core.designsystem.component.popup.dialog

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.linkit.company.core.designsystem.theme.LinkItTheme

/** Figma "Pingo v3.0.3 - Popup"의 공통 레이아웃과 색상 규격. */
object DialogDefaults {
    val MaxWidth: Dp = 320.dp
    val HorizontalMargin: Dp = 20.dp
    val ContentPadding: PaddingValues = PaddingValues(
        start = 40.dp,
        top = 20.dp,
        end = 40.dp,
        bottom = 4.dp,
    )
    val ActionPadding: PaddingValues = PaddingValues(horizontal = 20.dp, vertical = 20.dp)
    val ContentSpacing: Dp = 4.dp
    val CloseButtonInset: Dp = 12.dp
    val CloseButtonSize: Dp = 40.dp
    val CloseIconSize: Dp = 24.dp
    val ShadowElevation: Dp = 1.dp

    val containerColor: Color
        @Composable
        @ReadOnlyComposable
        get() = LinkItTheme.color.semantic.background.elevated.normal

    val titleColor: Color
        @Composable
        @ReadOnlyComposable
        get() = LinkItTheme.color.semantic.label.strong

    val descriptionColor: Color
        @Composable
        @ReadOnlyComposable
        get() = LinkItTheme.color.semantic.label.alternative

    val closeIconColor: Color
        @Composable
        @ReadOnlyComposable
        get() = LinkItTheme.color.semantic.label.normal

    val dimmerColor: Color
        @Composable
        @ReadOnlyComposable
        get() = LinkItTheme.color.semantic.material.dimmer
}

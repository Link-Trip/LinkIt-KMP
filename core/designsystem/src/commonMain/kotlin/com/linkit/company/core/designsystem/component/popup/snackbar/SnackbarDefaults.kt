package com.linkit.company.core.designsystem.component.popup

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.linkit.company.core.designsystem.foundation.color.token.PaletteTokens
import com.linkit.company.core.designsystem.theme.LinkItTheme

object SnackbarDefaults {

    val MaxWidth: Dp = 420.dp

    val MinContentHeight: Dp = 32.dp

    val IconSize: Dp = 24.dp

    val ContentSpacing: Dp = 8.dp

    val ActionSpacing: Dp = 12.dp

    val ContentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 8.dp)

    val ActionPadding: PaddingValues = PaddingValues(horizontal = 2.dp, vertical = 4.dp)

    val shape: Shape
        @Composable
        @ReadOnlyComposable
        get() = LinkItTheme.shape.xl

    val containerColor: Color
        @Composable
        @ReadOnlyComposable
        get() = LinkItTheme.color.semantic.inverse.background.copy(alpha = BackgroundAlpha)

    val overlayColor: Color
        @Composable
        @ReadOnlyComposable
        get() = LinkItTheme.color.semantic.primary.normal.copy(alpha = OverlayAlpha)

    val contentColor: Color
        @Composable
        @ReadOnlyComposable
        get() = LinkItTheme.color.semantic.static.white.copy(alpha = ContentAlpha)

    val actionColor: Color
        @Composable
        @ReadOnlyComposable
        get() = LinkItTheme.color.semantic.static.white

    val headingTextStyle: TextStyle
        @Composable
        @ReadOnlyComposable
        get() = LinkItTheme.typography.label1NormalBold

    val descriptionTextStyle: TextStyle
        @Composable
        @ReadOnlyComposable
        get() = LinkItTheme.typography.label2Medium

    val actionTextStyle: TextStyle
        @Composable
        @ReadOnlyComposable
        get() = LinkItTheme.typography.body2NormalBold

    private const val BackgroundAlpha = PaletteTokens.Opacity52
    private const val OverlayAlpha = PaletteTokens.Opacity5
    private const val ContentAlpha = PaletteTokens.Opacity88
}

package com.linkit.company.core.designsystem.foundation.color

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.linkit.company.core.designsystem.foundation.color.token.ColorTokens

data class LinkItColor(
    val primary500: Color,
    val primary100: Color,
    val gray900: Color,
    val gray800: Color,
    val gray700: Color,
    val gray600: Color,
    val gray500: Color,
    val gray400: Color,
    val gray300: Color,
    val gray200: Color,
    val gray100: Color,
    val dimmed: Color,
    val surfaceBlack: Color,
    val gradient3: Brush,
    val gradient2: Brush,
)

internal val LocalColor = staticCompositionLocalOf {
    LinkItColor(
        primary500 = ColorTokens.primary500,
        primary100 = ColorTokens.primary100,
        gray900 = ColorTokens.gray900,
        gray800 = ColorTokens.gray800,
        gray700 = ColorTokens.gray700,
        gray600 = ColorTokens.gray600,
        gray500 = ColorTokens.gray500,
        gray400 = ColorTokens.gray400,
        gray300 = ColorTokens.gray300,
        gray200 = ColorTokens.gray200,
        gray100 = ColorTokens.gray100,
        dimmed = ColorTokens.dimmed,
        surfaceBlack = ColorTokens.surfaceBlack,
        gradient3 = ColorTokens.gradient3,
        gradient2 = ColorTokens.gradient2,
    )
}

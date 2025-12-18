package com.linkit.company.core.designsystem.tokens

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

internal object ColorTokens {
    val primary500: Color = Color(0XFF666FFF)
    val primary100: Color = Color(0XFFF6F6FF)

    val gray900: Color = Color(0XFF27292C)
    val gray800: Color = Color(0XFF2B2D34)
    val gray700: Color = Color(0XFF404449)
    val gray600: Color = Color(0XFF707276)
    val gray500: Color = Color(0XFF878E99)
    val gray400: Color = Color(0XFFC0C5CC)
    val gray300: Color = Color(0XFFE6E7EB)
    val gray200: Color = Color(0XFFF5F5F6)
    val gray100: Color = Color(0XFFFAFAFB)

    val dimmed: Color = Color(0X4D121212)
    val surfaceBlack: Color = Color(0XF727292C)

    val gradient3: Brush = Brush.linearGradient(
        listOf(
            Color(0xFFEAF2FF),
            Color(0xFFEDF1FF),
            Color(0xFFEEEFFF),
            Color(0xFFECEFFF),
        ),
    )
    val gradient2: Brush = Brush.linearGradient(
        listOf(
            Color(0xFFF6FBFF),
            Color(0xFFF9FBFF),
            Color(0xFFFBF9FF),
            Color(0xFFF9F9FF),
        ),
    )
}
package com.linkit.company.core.designsystem.foundation.color.token

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color


internal object ColorTokens {
    val primary500: Color = Color(0xFF666FFF)
    val primary100: Color = Color(0xFFF6F6FF)

    val gray900: Color = Color(0xFF27292C)
    val gray800: Color = Color(0xFF2B2D34)
    val gray700: Color = Color(0xFF404449)
    val gray600: Color = Color(0xFF707276)
    val gray500: Color = Color(0xFF878E99)
    val gray400: Color = Color(0xFFC0C5CC)
    val gray300: Color = Color(0xFFE6E7EB)
    val gray200: Color = Color(0xFFF5F5F6)
    val gray100: Color = Color(0xFFFAFAFB)

    val dimmed: Color = Color(0x4D121212)
    val surfaceBlack: Color = Color(0xF727292C)

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

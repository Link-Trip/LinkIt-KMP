package com.linkit.company.core.designsystem.foundation.border.token

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

internal object BorderTokens {
    // Figma의 깨진 {unit.1} → 1.dp 확정 (Border width sm 관례값)
    val WidthSm: Dp = 1.dp
    val WidthMd: Dp = 2.dp
    val WidthLg: Dp = 4.dp
}

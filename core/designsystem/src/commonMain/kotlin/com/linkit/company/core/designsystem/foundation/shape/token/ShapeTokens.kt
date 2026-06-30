package com.linkit.company.core.designsystem.foundation.shape.token

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp

internal object ShapeTokens {
    val CornerSm = RoundedCornerShape(2.dp)
    val CornerMd = RoundedCornerShape(4.dp)
    val CornerLg = RoundedCornerShape(8.dp)
    val CornerXl = RoundedCornerShape(12.dp)
    val CornerXxl = RoundedCornerShape(16.dp)
    val CornerRounded = RoundedCornerShape(percent = 50)
    val CornerNone = RectangleShape
}

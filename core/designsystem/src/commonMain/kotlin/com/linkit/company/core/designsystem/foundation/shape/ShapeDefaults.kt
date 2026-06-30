package com.linkit.company.core.designsystem.foundation.shape

import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.ui.graphics.Shape
import com.linkit.company.core.designsystem.foundation.shape.token.ShapeTokens

object ShapeDefaults {
    val Sm: CornerBasedShape = ShapeTokens.CornerSm
    val Md: CornerBasedShape = ShapeTokens.CornerMd
    val Lg: CornerBasedShape = ShapeTokens.CornerLg
    val Xl: CornerBasedShape = ShapeTokens.CornerXl
    val Xxl: CornerBasedShape = ShapeTokens.CornerXxl
    val Rounded: CornerBasedShape = ShapeTokens.CornerRounded
    val None: Shape = ShapeTokens.CornerNone
}

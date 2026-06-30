package com.linkit.company.core.designsystem.foundation.shape.token

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Shape
import com.linkit.company.core.designsystem.foundation.shape.fromToken
import com.linkit.company.core.designsystem.theme.LinkItTheme

internal enum class ShapeAccessKeyToken {
    CornerSm,
    CornerMd,
    CornerLg,
    CornerXl,
    CornerXxl,
    CornerRounded,
    CornerNone,
}

internal val ShapeAccessKeyToken.value: Shape
    @Composable
    @ReadOnlyComposable
    get() = LinkItTheme.shape.fromToken(this)

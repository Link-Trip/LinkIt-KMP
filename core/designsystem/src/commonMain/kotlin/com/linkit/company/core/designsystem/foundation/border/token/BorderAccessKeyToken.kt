package com.linkit.company.core.designsystem.foundation.border.token

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.unit.Dp
import com.linkit.company.core.designsystem.foundation.border.fromToken
import com.linkit.company.core.designsystem.theme.LinkItTheme

internal enum class BorderAccessKeyToken {
    WidthSm,
    WidthMd,
    WidthLg,
}

internal val BorderAccessKeyToken.value: Dp
    @Composable
    @ReadOnlyComposable
    get() = LinkItTheme.borderWidth.fromToken(this)

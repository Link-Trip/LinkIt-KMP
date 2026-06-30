package com.linkit.company.core.designsystem.foundation.spacing.token

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.unit.Dp
import com.linkit.company.core.designsystem.foundation.spacing.fromToken
import com.linkit.company.core.designsystem.theme.LinkItTheme

internal enum class SpacingAccessKeyToken {
    Space0,
    Space2,
    Space4,
    Space8,
    Space12,
    Space16,
    Space20,
    Space24,
    Space32,
    Space40,
    Space44,
    Space48,
    Space52,
    Space64,
}

internal val SpacingAccessKeyToken.value: Dp
    @Composable
    @ReadOnlyComposable
    get() = LinkItTheme.spacing.fromToken(this)

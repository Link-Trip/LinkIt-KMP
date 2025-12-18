package com.linkit.company.core.designsystem.theme

import androidx.compose.material3.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import com.linkit.company.core.designsystem.foundation.color.LinkitColor
import com.linkit.company.core.designsystem.foundation.color.LocalColor
import com.linkit.company.core.designsystem.foundation.radius.LinkitRadius
import com.linkit.company.core.designsystem.foundation.radius.LocalRadius
import com.linkit.company.core.designsystem.foundation.typography.LinkitTypography
import com.linkit.company.core.designsystem.foundation.typography.LocalTypography

@Composable
fun LinkitTheme(
    color: LinkitColor = LinkitTheme.color,
    typography: LinkitTypography = LinkitTheme.typography,
    radius: LinkitRadius = LinkitTheme.radius,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalColor provides color,
        LocalTypography provides typography,
        LocalRadius provides radius,
    ) {
        ProvideTextStyle(value = typography.base1Medium, content = content)
    }
}

object LinkitTheme {
    val color: LinkitColor
        @Composable
        @ReadOnlyComposable
        get() = LocalColor.current

    val typography: LinkitTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalTypography.current

    val radius: LinkitRadius
        @Composable
        @ReadOnlyComposable
        get() = LocalRadius.current
}
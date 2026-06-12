package com.linkit.company.core.designsystem.theme

import androidx.compose.material3.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import com.linkit.company.core.designsystem.foundation.color.LinkItColor
import com.linkit.company.core.designsystem.foundation.color.LocalColor
import com.linkit.company.core.designsystem.foundation.radius.LinkItRadius
import com.linkit.company.core.designsystem.foundation.radius.LocalRadius
import com.linkit.company.core.designsystem.foundation.typography.LinkItTypography
import com.linkit.company.core.designsystem.foundation.typography.LocalTypography
import com.linkit.company.core.designsystem.foundation.typography.provideDefaultFontFamily

@Composable
fun LinkItTheme(
    color: LinkItColor = LinkItTheme.color,
    typography: LinkItTypography = LinkItTheme.typography,
    radius: LinkItRadius = LinkItTheme.radius,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalColor provides color,
        LocalTypography provides typography.provideDefaultFontFamily(),
        LocalRadius provides radius,
    ) {
        ProvideTextStyle(value = typography.base1Medium, content = content)
    }
}

object LinkItTheme {
    val color: LinkItColor
        @Composable
        @ReadOnlyComposable
        get() = LocalColor.current

    val typography: LinkItTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalTypography.current

    val radius: LinkItRadius
        @Composable
        @ReadOnlyComposable
        get() = LocalRadius.current
}

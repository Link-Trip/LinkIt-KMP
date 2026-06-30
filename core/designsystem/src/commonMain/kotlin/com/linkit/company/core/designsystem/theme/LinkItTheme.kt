package com.linkit.company.core.designsystem.theme

import androidx.compose.material3.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import com.linkit.company.core.designsystem.foundation.border.LinkItBorderWidth
import com.linkit.company.core.designsystem.foundation.border.LocalBorderWidth
import com.linkit.company.core.designsystem.foundation.color.ColorScheme
import com.linkit.company.core.designsystem.foundation.color.LocalColorScheme
import com.linkit.company.core.designsystem.foundation.color.lightColorScheme
import com.linkit.company.core.designsystem.foundation.shape.LinkItShapes
import com.linkit.company.core.designsystem.foundation.shape.LocalShapes
import com.linkit.company.core.designsystem.foundation.spacing.LinkItSpacing
import com.linkit.company.core.designsystem.foundation.spacing.LocalSpacing
import com.linkit.company.core.designsystem.foundation.typography.LinkItTypography
import com.linkit.company.core.designsystem.foundation.typography.LocalTypography
import com.linkit.company.core.designsystem.foundation.typography.provideDefaultFontFamily

@Composable
fun LinkItTheme(
    color: ColorScheme = LinkItTheme.color,
    typography: LinkItTypography = LinkItTheme.typography,
    shape: LinkItShapes = LinkItTheme.shape,
    borderWidth: LinkItBorderWidth = LinkItTheme.borderWidth,
    spacing: LinkItSpacing = LinkItTheme.spacing,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalColorScheme provides color,
        LocalTypography provides typography.provideDefaultFontFamily(),
        LocalShapes provides shape,
        LocalBorderWidth provides borderWidth,
        LocalSpacing provides spacing,
    ) {
        ProvideTextStyle(value = typography.body1NormalMedium, content = content)
    }
}

object LinkItTheme {
    val color: ColorScheme
        @Composable
        @ReadOnlyComposable
        get() = LocalColorScheme.current

    val typography: LinkItTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalTypography.current

    val shape: LinkItShapes
        @Composable
        @ReadOnlyComposable
        get() = LocalShapes.current

    val borderWidth: LinkItBorderWidth
        @Composable
        @ReadOnlyComposable
        get() = LocalBorderWidth.current

    val spacing: LinkItSpacing
        @Composable
        @ReadOnlyComposable
        get() = LocalSpacing.current
}

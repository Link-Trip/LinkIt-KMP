package com.linkit.company.core.designsystem.legacy.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LinkItColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = White,
    secondary = PrimaryBlueBorder,
    onSecondary = White,
    background = White,
    onBackground = Black,
    surface = White,
    onSurface = Black,
    surfaceVariant = G1,
    onSurfaceVariant = G6,
    outline = BorderDefault,
    outlineVariant = G2,
    error = TagRedText,
    onError = White,
)

@Composable
fun LinkItTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = LinkItColorScheme,
        typography = LinkItTypography,
        content = content,
    )
}

package com.linkit.company.feature.map.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.linkit.company.core.designsystem.theme.LinkItTheme

@Composable
internal actual fun GoogleMapBackground(modifier: Modifier) {
    Box(
        modifier = modifier.background(LinkItTheme.color.semantic.background.normal.alternative),
    )
}

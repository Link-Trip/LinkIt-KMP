package com.linkit.company.feature.map.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import com.linkit.company.core.designsystem.theme.LinkItTheme
import linkitcompany.feature.map.generated.resources.Res
import linkitcompany.feature.map.generated.resources.map_background
import org.jetbrains.compose.resources.painterResource

@Composable
internal expect fun PlatformMapBackground(mapType: MapType, modifier: Modifier)

@Composable
internal fun StaticMapBackground(mapType: MapType, modifier: Modifier) {
    Box(modifier) {
        Image(
            painter = painterResource(Res.drawable.map_background),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize(),
        )
        if (mapType == MapType.SATELLITE) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(LinkItTheme.color.semantic.label.neutral.copy(alpha = .28f)),
            )
        }
    }
}

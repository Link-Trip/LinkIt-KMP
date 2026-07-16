package com.linkit.company.feature.map.main

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
internal actual fun PlatformMapBackground(mapType: MapType, modifier: Modifier) {
    StaticMapBackground(mapType, modifier)
}

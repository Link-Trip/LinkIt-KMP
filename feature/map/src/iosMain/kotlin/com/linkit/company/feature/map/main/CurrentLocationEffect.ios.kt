package com.linkit.company.feature.map.main

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState

@Composable
internal actual fun CurrentLocationEffect(
    requestToken: Int,
    onLocationAvailable: (MapCoordinateUiModel) -> Unit,
    onLocationUnavailable: () -> Unit,
) {
    val currentOnLocationUnavailable by rememberUpdatedState(onLocationUnavailable)
    LaunchedEffect(requestToken) {
        if (requestToken > 0) currentOnLocationUnavailable()
    }
}

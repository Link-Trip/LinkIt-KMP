package com.linkit.company.feature.map.main

import androidx.compose.runtime.Composable

/**
 * Resolves the device location once whenever [requestToken] advances to a positive value.
 *
 * Callers should increment the token for each user-initiated request. A platform reports either
 * one usable coordinate through [onLocationAvailable] or a permission/provider failure through
 * [onLocationUnavailable].
 */
@Composable
internal expect fun CurrentLocationEffect(
    requestToken: Int,
    onLocationAvailable: (MapCoordinateUiModel) -> Unit,
    onLocationUnavailable: () -> Unit,
)

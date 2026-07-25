package com.linkit.company.feature.map.main

import androidx.compose.runtime.Composable
import com.linkit.company.domain.model.map.TripPlanMapData

/**
 * Returns local map data only when the current platform is running a debuggable build.
 *
 * Release builds and platforms without a debug data provider return null and keep using the API.
 */
@Composable
internal expect fun rememberMapDebugData(): List<TripPlanMapData>?

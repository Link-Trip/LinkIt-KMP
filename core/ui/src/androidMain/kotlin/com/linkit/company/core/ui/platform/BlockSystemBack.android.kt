package com.linkit.company.core.ui.platform

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable

@Composable
actual fun BlockSystemBack(enabled: Boolean) {
    BackHandler(enabled = enabled) {}
}

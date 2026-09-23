package com.linkit.company.core.designsystem.component.coachmark

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable

@Composable
internal actual fun CoachMarkBackHandler() {
    BackHandler(enabled = true) {}
}

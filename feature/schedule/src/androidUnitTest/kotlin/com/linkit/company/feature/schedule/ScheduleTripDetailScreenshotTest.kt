package com.linkit.company.feature.schedule

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.captureRoboImage
import com.linkit.company.core.designsystem.theme.LinkItTheme
import org.jetbrains.compose.resources.PreviewContextConfigurationEffect
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(manifest = Config.NONE, sdk = [35], qualifiers = "w375dp-h1600dp-mdpi")
class ScheduleTripDetailScreenshotTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun mapPreview() = capture(ScheduleUiState(), 812.dp)

    @Test
    fun itinerary() = capture(ScheduleUiState(showTripMapPreview = false), 905.dp)

    @Test
    fun videoSummary() = capture(
        ScheduleUiState(tripDetailTab = TripDetailTab.SUMMARY, showTripMapPreview = false),
        1528.dp,
    )

    private fun capture(state: ScheduleUiState, height: Dp) {
        composeRule.setContent {
            CompositionLocalProvider(LocalInspectionMode provides true) {
                PreviewContextConfigurationEffect()
                LinkItTheme {
                    var ready by remember { mutableStateOf(false) }
                    LaunchedEffect(Unit) { ready = true }
                    Box(
                        Modifier
                            .requiredSize(375.dp, height)
                            .graphicsLayer { alpha = if (ready) 1f else .999f },
                    ) {
                        ScheduleTripDetailContent(uiState = state, onIntent = {})
                    }
                }
            }
        }
        composeRule.onRoot().captureRoboImage()
    }
}

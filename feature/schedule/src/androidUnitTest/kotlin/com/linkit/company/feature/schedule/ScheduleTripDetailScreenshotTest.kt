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
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
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
import kotlin.test.assertEquals

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
    fun itineraryMoreMenu() = capture(
        state = ScheduleUiState(
            showTripMapPreview = false,
            tripDetailMenuExpanded = true,
        ),
        height = 905.dp,
        tripPlanId = "trip-plan-1",
    )

    @Test
    fun videoSummary() = capture(
        ScheduleUiState(tripDetailTab = TripDetailTab.SUMMARY, showTripMapPreview = false),
        1528.dp,
    )

    @Test
    fun menuItemsDispatchIntents() {
        var dispatchedIntent: ScheduleIntent? = null
        composeRule.setContent {
            LinkItTheme {
                ScheduleTripDetailContent(
                    uiState = ScheduleUiState(
                        showTripMapPreview = false,
                        tripDetailMenuExpanded = true,
                    ),
                    tripPlanId = "trip-plan-1",
                    title = "도쿄 신주쿠 여행",
                    onIntent = { dispatchedIntent = it },
                )
            }
        }

        composeRule.onNodeWithText("일정 이름 변경").performClick()

        assertEquals(
            ScheduleIntent.ShowTripDetailRenameDialog(
                tripPlanId = "trip-plan-1",
                currentTitle = "도쿄 신주쿠 여행",
            ),
            dispatchedIntent,
        )

        composeRule.onNodeWithText("일정 삭제").performClick()

        assertEquals(
            ScheduleIntent.ShowTripDetailDeleteDialog("trip-plan-1"),
            dispatchedIntent,
        )
    }

    private fun capture(
        state: ScheduleUiState,
        height: Dp,
        tripPlanId: String? = null,
    ) {
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
                        ScheduleTripDetailContent(
                            uiState = state,
                            tripPlanId = tripPlanId,
                            onIntent = {},
                        )
                    }
                }
            }
        }
        composeRule.onRoot().captureRoboImage()
    }
}

package com.linkit.company.feature.map.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertValueEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeWithVelocity
import androidx.compose.ui.unit.dp
import com.linkit.company.core.designsystem.theme.LinkItTheme
import org.jetbrains.compose.resources.PreviewContextConfigurationEffect
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w375dp-h744dp-mdpi")
class MapBottomSheetGestureTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun sheetTracksFingerAndReturnsToNearestAnchor() {
        setMapContent()
        val initialTop = sheetTop()
        val handleCenter = handleCenterInRoot()

        composeRule.onRoot().performTouchInput {
            down(handleCenter)
        }
        composeRule.onRoot().performTouchInput {
            moveTo(handleCenter + Offset(x = 0f, y = -80f), delayMillis = 200)
        }
        composeRule.waitForIdle()

        assertTrue(
            "The sheet should move continuously with the pointer while it is held",
            sheetTop() < initialTop - 30f,
        )

        composeRule.onRoot().performTouchInput {
            advanceEventTime(1_000)
            up()
        }
        composeRule.waitForIdle()

        sheet().assertValueEquals("Resting")
        assertEquals(initialTop.toDouble(), sheetTop().toDouble(), 1.0)
    }

    @Test
    fun slowReleasePastUpperMidpointSettlesExpanded() {
        setMapContent()

        slowDragBy(deltaY = -240f)

        sheet().assertValueEquals("Expanded")
    }

    @Test
    fun slowReleasePastLowerMidpointSettlesCollapsed() {
        setMapContent()

        slowDragBy(deltaY = 190f)

        sheet().assertValueEquals("Collapsed")
    }

    @Test
    fun fastShortSwipeUsesVelocityDirection() {
        setMapContent()

        fastSwipeBy(deltaY = -80f)

        sheet().assertValueEquals("Expanded")
    }

    @Test
    fun bothExtremeAnchorsCanReturnToResting() {
        setMapContent()

        fastSwipeBy(deltaY = -80f)
        sheet().assertValueEquals("Expanded")
        fastSwipeBy(deltaY = 80f)
        sheet().assertValueEquals("Resting")

        fastSwipeBy(deltaY = 80f)
        sheet().assertValueEquals("Collapsed")
        fastSwipeBy(deltaY = -80f)
        sheet().assertValueEquals("Resting")
    }

    @Test
    fun reverseDragInterruptsSettlingAnimationWithoutPositionJump() {
        setMapContent()
        composeRule.mainClock.autoAdvance = false

        composeRule.onNodeWithTag(HandleTag).performTouchInput {
            swipeWithVelocity(
                start = center,
                end = center + Offset(x = 0f, y = -160f),
                endVelocity = 1_200f,
            )
        }
        composeRule.mainClock.advanceTimeBy(48)
        val topDuringSettle = sheetTop()
        val handleDuringSettle = handleCenterInRoot()

        composeRule.onRoot().performTouchInput {
            down(handleDuringSettle)
            moveTo(
                position = handleDuringSettle + Offset(x = 0f, y = 40f),
                delayMillis = 100,
            )
            moveTo(
                position = handleDuringSettle + Offset(x = 0f, y = 120f),
                delayMillis = 100,
            )
        }
        composeRule.mainClock.advanceTimeBy(32)
        val topAfterReverseDrag = sheetTop()
        assertTrue(
            "The interrupted sheet should immediately follow the reverse drag " +
                "(settling=$topDuringSettle, dragged=$topAfterReverseDrag)",
            topAfterReverseDrag > topDuringSettle + 30f,
        )

        composeRule.onRoot().performTouchInput {
            advanceEventTime(1_000)
            up()
        }
        composeRule.mainClock.autoAdvance = true
        composeRule.waitForIdle()

        sheet().assertValueEquals("Resting")
    }

    @Test
    fun scheduleRowShowsPreviewBeforeOpeningScheduleDetail() {
        var detailOpenCount = 0
        setMapContent(
            stateReducer = { state, intent ->
                when (intent) {
                    is MapIntent.SelectSchedule -> state.copy(
                        selectedScheduleId = intent.scheduleId,
                        selectedPlaceMarkerId = null,
                    )
                    else -> state
                }
            },
            onOpenSchedule = { detailOpenCount++ },
        )

        composeRule
            .onNodeWithTag("map-schedule-${MapTestFixtures.SeoulScheduleId}")
            .performClick()

        composeRule
            .onNodeWithTag("map-selected-schedule-${MapTestFixtures.SeoulScheduleId}")
            .assertIsDisplayed()
        sheet().assertValueEquals("Resting")
        composeRule.runOnIdle {
            assertEquals(0, detailOpenCount)
        }

        composeRule
            .onNodeWithTag("map-selected-schedule-${MapTestFixtures.SeoulScheduleId}")
            .performClick()
        composeRule.runOnIdle {
            assertEquals(1, detailOpenCount)
        }
    }

    private fun setMapContent(
        initialState: MapUiState = MapTestFixtures.contentState(),
        stateReducer: (MapUiState, MapIntent) -> MapUiState = { state, _ -> state },
        onOpenSchedule: () -> Unit = {},
    ) {
        composeRule.setContent {
            var state by remember { mutableStateOf(initialState) }
            CompositionLocalProvider(LocalInspectionMode provides true) {
                PreviewContextConfigurationEffect()
                LinkItTheme {
                    Box(Modifier.requiredSize(375.dp, 744.dp)) {
                        MapContent(
                            uiState = state,
                            onIntent = { intent ->
                                state = stateReducer(state, intent)
                            },
                            onOpenSchedule = { _, _, _ -> onOpenSchedule() },
                        )
                    }
                }
            }
        }
        composeRule.waitForIdle()
        sheet().assertValueEquals("Resting")
    }

    private fun slowDragBy(deltaY: Float) {
        val handleCenter = handleCenterInRoot()
        composeRule.onRoot().performTouchInput {
            down(handleCenter)
            moveTo(
                position = handleCenter + Offset(x = 0f, y = deltaY),
                delayMillis = 500,
            )
            advanceEventTime(1_000)
            up()
        }
        composeRule.waitForIdle()
    }

    private fun fastSwipeBy(deltaY: Float) {
        composeRule.onNodeWithTag(HandleTag).performTouchInput {
            swipeWithVelocity(
                start = center,
                end = center + Offset(x = 0f, y = deltaY),
                endVelocity = 1_200f,
            )
        }
        composeRule.waitForIdle()
    }

    private fun sheet() = composeRule.onNodeWithTag(SheetTag)

    private fun sheetTop(): Float = sheet().fetchSemanticsNode().boundsInRoot.top

    private fun handleCenterInRoot(): Offset =
        composeRule.onNodeWithTag(HandleTag).fetchSemanticsNode().boundsInRoot.center

    private companion object {
        const val SheetTag = "map-bottom-sheet"
        const val HandleTag = "map-bottom-sheet-handle"
    }
}

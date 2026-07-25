package com.linkit.company.feature.map.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.captureRoboImage
import com.linkit.company.core.designsystem.theme.LinkItTheme
import org.jetbrains.compose.resources.PreviewContextConfigurationEffect
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w375dp-h744dp-mdpi")
class MapScreenshotTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun defaultMap() = capture(MapTestFixtures.contentState())

    @Test
    fun scheduleSelected() = capture(
        MapTestFixtures.contentState(selectedScheduleId = MapTestFixtures.SeoulScheduleId),
    )

    @Test
    fun placeSelected() = capture(
        MapTestFixtures.contentState(
            selectedScheduleId = MapTestFixtures.SeoulScheduleId,
            selectedPlaceMarkerId = MapTestFixtures.MarketMarkerId,
        ),
    )

    @Test
    fun loadingMap() = capture(MapUiState(loadState = MapLoadState.LOADING))

    @Test
    fun emptyMap() = capture(MapUiState(loadState = MapLoadState.EMPTY))

    @Test
    fun errorMap() = capture(
        MapUiState(
            loadState = MapLoadState.ERROR,
            errorMessage = "네트워크 연결을 확인하고 다시 시도해 주세요.",
        ),
    )

    @Test
    fun topActionsMatchFigma() {
        composeRule.setContent {
            CompositionLocalProvider(LocalInspectionMode provides true) {
                PreviewContextConfigurationEffect()
                LinkItTheme {
                    MapContent(uiState = MapTestFixtures.contentState(), onIntent = {})
                }
            }
        }

        assertEquals(1, composeRule.onAllNodesWithContentDescription("마이페이지").fetchSemanticsNodes().size)
        assertEquals(1, composeRule.onAllNodesWithContentDescription("지도 종류 변경").fetchSemanticsNodes().size)
        assertEquals(0, composeRule.onAllNodesWithContentDescription("현재 위치로 이동").fetchSemanticsNodes().size)
    }

    private fun capture(state: MapUiState) {
        composeRule.setContent {
            CompositionLocalProvider(LocalInspectionMode provides true) {
                PreviewContextConfigurationEffect()
                LinkItTheme {
                    Box(Modifier.requiredSize(375.dp, 744.dp)) {
                        MapContent(uiState = state, onIntent = {})
                    }
                }
            }
        }

        composeRule.onRoot().captureRoboImage()
    }
}

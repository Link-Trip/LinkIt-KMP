package com.linkit.company.feature.explore

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
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
@Config(sdk = [35], qualifiers = "w375dp-h1600dp-mdpi")
class ExploreScreenshotTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun initialCountryExplore() {
        capture(ExploreUiState(), 979.dp)
    }

    @Test
    fun japanCountryExplore() {
        capture(ExploreUiState(selectedCountry = ExploreCountry.JAPAN), 1416.dp)
    }

    @Test
    fun themeExplore() {
        capture(ExploreUiState(selectedTab = ExploreTab.THEME), 979.dp)
    }

    private fun capture(state: ExploreUiState, height: androidx.compose.ui.unit.Dp) {
        composeRule.setContent {
            CompositionLocalProvider(LocalInspectionMode provides true) {
                PreviewContextConfigurationEffect()
                LinkItTheme {
                    Box(Modifier.requiredSize(375.dp, height)) {
                        ExploreContent(
                            uiState = state,
                            onIntent = {},
                        )
                    }
                }
            }
        }

        composeRule.onRoot().captureRoboImage()
    }
}

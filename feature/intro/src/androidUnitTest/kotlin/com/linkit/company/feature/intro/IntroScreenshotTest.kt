package com.linkit.company.feature.intro

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
@Config(manifest = Config.NONE, sdk = [35], qualifiers = "w375dp-h812dp-mdpi")
class IntroScreenshotTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun globe() = capture(IntroUiState(IntroStage.GLOBE))

    @Test
    fun seoul() = capture(IntroUiState(IntroStage.SEOUL))

    private fun capture(state: IntroUiState) {
        composeRule.setContent {
            CompositionLocalProvider(LocalInspectionMode provides true) {
                PreviewContextConfigurationEffect()
                LinkItTheme {
                    Box(Modifier.requiredSize(375.dp, 812.dp)) {
                        IntroContent(uiState = state)
                    }
                }
            }
        }
        composeRule.onRoot().captureRoboImage()
    }
}

package com.linkit.company.feature.schedule

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
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
class ScheduleAnalysisLoadingScreenshotTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun loading() = capture(showNotificationPermissionSheet = false)

    @Test
    fun notificationPermissionSheet() = capture(showNotificationPermissionSheet = true)

    @Test
    fun displaysYouTubeVideoTitle() {
        setScheduleContent(
            videoTitle = "유부남과 함께 오사카 좋은 놀이공원 가보기 【오사카上】",
            showNotificationPermissionSheet = false,
        )

        composeRule
            .onNodeWithText("유부남과 함께 오사카 좋은 놀이공원 가보기 【오사카上】")
            .assertIsDisplayed()
    }

    private fun capture(showNotificationPermissionSheet: Boolean) {
        setScheduleContent(showNotificationPermissionSheet = showNotificationPermissionSheet)
        composeRule.onRoot().captureRoboImage()
    }

    private fun setScheduleContent(
        videoTitle: String? = null,
        showNotificationPermissionSheet: Boolean,
    ) {
        composeRule.setContent {
            CompositionLocalProvider(LocalInspectionMode provides true) {
                PreviewContextConfigurationEffect()
                LinkItTheme {
                    Box(Modifier.requiredSize(375.dp, 812.dp)) {
                        ScheduleAnalysisLoadingScreen(
                            videoTitle = videoTitle,
                            showNotificationPermissionSheet = showNotificationPermissionSheet,
                        )
                    }
                }
            }
        }
    }
}

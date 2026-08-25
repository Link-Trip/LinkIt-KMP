package com.linkit.company.feature.schedule

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
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
class ScheduleEditScreenshotTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun emptyVideoLink() = capture(ScheduleUiState())

    @Test
    fun filledVideoLink() = capture(
        ScheduleUiState(
            videoLink = "https://www.youtube.com/watch?v=OrGmEVTD04I",
        ),
    )

    @Test
    fun invalidVideoLink() = capture(
        ScheduleUiState(
            videoLink = "https://example.com/video",
            videoLinkError = VideoLinkError.WRONG_FORMAT,
        ),
    )

    @Test
    fun existingSchedulePopup() = capture(
        ScheduleUiState(
            videoLink = "https://youtu.be/OrGmEVTD04I",
            existingSchedule = ExistingScheduleUiModel(
                tripPlanId = "trip-plan-id",
                title = "오사카 여행",
            ),
        ),
    )

    @Test
    fun recommendedVideosSwipeLeft() {
        setScheduleContent(ScheduleUiState())

        composeRule
            .onNodeWithTag(RecommendedVideoListTestTag)
            .assert(hasScrollAction())
            .performTouchInput { swipeLeft() }

        composeRule
            .onNodeWithText("유명 신혼 여행지에 혼자 당당히 여행가는 사람【몰디브】")
            .assertIsDisplayed()
    }

    @Test
    fun recommendedVideoKeepsCopyLabelAfterClick() {
        setScheduleContent(ScheduleUiState())

        composeRule
            .onAllNodesWithText("링크복사")[0]
            .performClick()

        composeRule
            .onAllNodesWithText("링크복사")[0]
            .assertIsDisplayed()
        composeRule
            .onAllNodesWithText("복사완료")
            .assertCountEquals(0)
    }

    private fun capture(state: ScheduleUiState) {
        setScheduleContent(state)
        composeRule.onRoot().captureRoboImage()
    }

    private fun setScheduleContent(state: ScheduleUiState) {
        composeRule.setContent {
            CompositionLocalProvider(LocalInspectionMode provides true) {
                PreviewContextConfigurationEffect()
                LinkItTheme {
                    Box(Modifier.requiredSize(375.dp, 812.dp)) {
                        ScheduleEditContent(uiState = state, onIntent = {})
                    }
                }
            }
        }
    }
}

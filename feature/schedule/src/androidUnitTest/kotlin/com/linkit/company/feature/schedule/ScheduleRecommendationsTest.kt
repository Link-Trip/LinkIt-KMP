package com.linkit.company.feature.schedule

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
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
@Config(manifest = Config.NONE, sdk = [35], qualifiers = "w375dp-h812dp-mdpi")
class ScheduleRecommendationsTest {
    @get:Rule val composeRule = createComposeRule()
    private var clipboard: ClipboardManager? = null

    @Test
    fun expandsAllLoadedVideosAndSelectsActualUrl() {
        val intents = mutableListOf<ScheduleIntent>()
        setContent(
            ScheduleUiState(
                recommendedVideos = ScheduleRecommendationFixtures,
                isLoadingRecommendedVideos = false,
            ),
            intents::add,
        )

        composeRule.onNodeWithTag("recommended-video-recommended-4").assertDoesNotExist()
        composeRule.onNodeWithTag("recommended-video-expand").performClick()
        composeRule.onNodeWithTag("recommended-video-copy-recommended-4").performScrollTo().performClick()
        composeRule.runOnIdle {
            assertEquals("https://youtu.be/recommended-4", clipboard?.getText()?.text)
            assertEquals(listOf<ScheduleIntent>(ScheduleIntent.ToggleRecommendedVideos), intents)
        }
        // Tap the title: the card's geometric center may belong to its separate copy action.
        composeRule.onNodeWithText(ScheduleRecommendationFixtures[4].title, useUnmergedTree = true)
            .performScrollTo().performClick()
        composeRule.onNodeWithText("https://youtu.be/recommended-4").performScrollTo().assertIsDisplayed()
        assertEquals<ScheduleIntent>(ScheduleIntent.UpdateVideoLink("https://youtu.be/recommended-4"), intents.last())
        composeRule.onNodeWithTag("recommended-video-expand").performScrollTo().performClick()
        composeRule.onNodeWithTag("recommended-video-recommended-4").assertDoesNotExist()
        composeRule.onRoot().captureRoboImage()
    }

    @Test
    fun copyActionWritesServerUrlWithoutSelectingOrSubmittingVideo() {
        val intents = mutableListOf<ScheduleIntent>()
        setContent(
            ScheduleUiState(
                recommendedVideos = ScheduleRecommendationFixtures,
                isLoadingRecommendedVideos = false,
            ),
            intents::add,
        )

        composeRule.onNodeWithText("조회수 12,345회").assertIsDisplayed()
        composeRule.onNodeWithTag("recommended-video-copy-recommended-0").performClick()
        composeRule.runOnIdle {
            assertEquals("https://youtu.be/recommended-0", clipboard?.getText()?.text)
            assertEquals(emptyList<ScheduleIntent>(), intents)
        }
    }

    @Test
    fun submittingDisablesRecommendationSelectionAndPasteButKeepsCopyAvailable() {
        val intents = mutableListOf<ScheduleIntent>()
        setContent(
            ScheduleUiState(
                recommendedVideos = ScheduleRecommendationFixtures,
                isLoadingRecommendedVideos = false,
                videoLink = "https://youtu.be/submitted-video",
                isSubmittingVideoLink = true,
            ),
            intents::add,
        )

        composeRule.onNodeWithTag("recommended-video-recommended-0").assertIsNotEnabled()
        composeRule.onNodeWithText(ScheduleRecommendationFixtures[0].title, useUnmergedTree = true).performClick()
        composeRule.onNodeWithTag("recommended-video-copy-recommended-0").assertIsEnabled().performClick()
        composeRule.onNodeWithText("복사한 링크 붙여넣기").assertIsNotEnabled().performClick()
        composeRule.onNodeWithText("https://youtu.be/submitted-video").assertIsDisplayed()
        composeRule.runOnIdle {
            assertEquals("https://youtu.be/recommended-0", clipboard?.getText()?.text)
            assertEquals(emptyList<ScheduleIntent>(), intents)
        }
    }

    @Test
    fun errorOffersRetryWithoutBlockingLinkEntry() {
        val intents = mutableListOf<ScheduleIntent>()
        setContent(
            ScheduleUiState(
                isLoadingRecommendedVideos = false,
                recommendedVideosError = "추천 영상을 불러오지 못했어요.",
                videoLink = "https://youtu.be/manual-link",
            ),
            intents::add,
        )

        composeRule.onNodeWithText("추천 영상을 불러오지 못했어요.").assertIsDisplayed()
        composeRule.onNodeWithText("다시 시도").performClick()
        composeRule.onNodeWithText("일정 생성하기").performClick()
        assertEquals(
            listOf<ScheduleIntent>(ScheduleIntent.LoadRecommendedVideos, ScheduleIntent.SubmitVideoLink),
            intents,
        )
        composeRule.onRoot().captureRoboImage()
    }

    @Test
    fun emptyResponseDoesNotShowHardcodedRecommendations() {
        setContent(ScheduleUiState(isLoadingRecommendedVideos = false))

        composeRule.onNodeWithText("추천 영상이 아직 없어요.").assertIsDisplayed()
        composeRule.onNodeWithTag(RecommendedVideoListTestTag).assertDoesNotExist()
        composeRule.onNodeWithText("더보기").assertDoesNotExist()
    }

    @Test
    fun loadingShowsProgressInsteadOfMockVideos() {
        setContent(ScheduleUiState())

        composeRule.onNodeWithTag("recommended-video-loading").assertIsDisplayed()
        composeRule.onNodeWithTag(RecommendedVideoListTestTag).assertDoesNotExist()
    }

    private fun setContent(initialState: ScheduleUiState, onIntent: (ScheduleIntent) -> Unit = {}) {
        composeRule.setContent {
            CompositionLocalProvider(LocalInspectionMode provides true) {
                PreviewContextConfigurationEffect()
                clipboard = LocalClipboardManager.current
                var state by remember { mutableStateOf(initialState) }
                LinkItTheme {
                    Box(Modifier.requiredSize(375.dp, 812.dp)) {
                        ScheduleEditContent(
                            uiState = state,
                            onIntent = { intent ->
                                when (intent) {
                                    ScheduleIntent.ToggleRecommendedVideos -> state = state.copy(
                                        areRecommendedVideosExpanded = !state.areRecommendedVideosExpanded,
                                    )
                                    is ScheduleIntent.UpdateVideoLink -> state = state.copy(videoLink = intent.link)
                                    else -> Unit
                                }
                                onIntent(intent)
                            },
                        )
                    }
                }
            }
        }
    }
}

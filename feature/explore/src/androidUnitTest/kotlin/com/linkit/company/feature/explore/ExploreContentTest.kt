package com.linkit.company.feature.explore

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToKey
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

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ExploreContentTest {
    @get:Rule val composeRule = createComposeRule()

    @Test
    fun videoCardShowsResponseMetadataAndOpensItsOwnUrl() {
        var openedUrl: String? = null
        composeRule.setContent {
            PreviewContextConfigurationEffect()
            LinkItTheme {
                ExploreContent(
                    uiState = exploreFixtureState().copy(selectedTab = ExploreTab.THEME, videos = listOf(exploreVideo("one"))),
                    onIntent = {},
                    onOpenUrl = { openedUrl = it },
                )
            }
        }

        composeRule.onNodeWithText("조회수 12345회 · 12:34 · 2026-09-21").assertExists()
        composeRule.onNodeWithText("실제 응답 영상 one").performClick()
        assertEquals("https://www.youtube.com/watch?v=one", openedUrl)
    }

    @Test
    fun countryLayoutKeepsCompactFiltersPortraitDestinationsAndShortVideoThumbnails() {
        composeRule.setContent {
            PreviewContextConfigurationEffect()
            LinkItTheme {
                ExploreContent(
                    uiState = exploreFixtureState().copy(channels = emptyList(), videos = listOf(exploreVideo("one"))),
                    onIntent = {},
                )
            }
        }

        composeRule.onNodeWithTag("explore-filter-전체").assertHeightIsEqualTo(34.dp)
        composeRule.onNodeWithTag("explore-country-일본").assertWidthIsEqualTo(128.dp).assertHeightIsEqualTo(160.dp)
        composeRule.onNodeWithTag("explore-video-feed").performScrollToKey("one")
        composeRule.onNodeWithTag("explore-video-thumbnail", useUnmergedTree = true)
            .performScrollTo().assertHeightIsEqualTo(112.dp)
    }

    @Test
    fun themeHeadingPrecedesFiltersAndKeepsLargeVideoThumbnails() {
        composeRule.setContent {
            PreviewContextConfigurationEffect()
            LinkItTheme {
                ExploreContent(
                    uiState = exploreFixtureState().copy(selectedTab = ExploreTab.THEME, videos = listOf(exploreVideo("one"))),
                    onIntent = {},
                )
            }
        }

        val heading = composeRule.onNodeWithText("테마별 추천 영상").fetchSemanticsNode().boundsInRoot
        val filter = composeRule.onNodeWithTag("explore-filter-전체").fetchSemanticsNode().boundsInRoot
        assertTrue(heading.bottom < filter.top)
        composeRule.onNodeWithTag("explore-video-thumbnail", useUnmergedTree = true).assertHeightIsEqualTo(168.dp)
    }

    @Test
    fun selectingCreatorChangesVisibleRecentVideoAndItsLink() {
        var openedUrl: String? = null
        composeRule.setContent {
            PreviewContextConfigurationEffect()
            var state by remember { mutableStateOf(exploreFixtureState()) }
            LinkItTheme {
                RecommendedCreatorsContent(
                    onBack = {},
                    uiState = state,
                    onIntent = { intent ->
                        if (intent is ExploreIntent.SelectChannel) state = state.copy(selectedChannelId = intent.channelId)
                    },
                    onOpenUrl = { openedUrl = it },
                )
            }
        }

        composeRule.onNodeWithText("크리에이터 creator2").performScrollTo().performClick()
        composeRule.onNodeWithText("최근 영상 creator2").assertExists().performClick()
        assertEquals("https://youtu.be/recent-creator2", openedUrl)
    }
}

package com.linkit.company.feature.schedule

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
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
import com.linkit.company.core.designsystem.component.coachmark.CoachMarkDefaults
import com.linkit.company.core.designsystem.theme.LinkItTheme
import com.linkit.company.domain.model.onboarding.TutorialStep
import com.linkit.company.domain.model.video.DiscoverVideo
import org.jetbrains.compose.resources.PreviewContextConfigurationEffect
import org.junit.Assert.assertEquals
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
    fun emptyVideoLink() = capture(contentState())

    @Test
    fun filledVideoLink() = capture(
        contentState(
            videoLink = "https://www.youtube.com/watch?v=OrGmEVTD04I",
        ),
    )

    @Test
    fun invalidVideoLink() = capture(
        contentState(
            videoLink = "https://example.com/video",
            videoLinkError = VideoLinkError.WRONG_FORMAT,
        ),
    )

    @Test
    fun existingSchedulePopup() = capture(
        contentState(
            videoLink = "https://youtu.be/OrGmEVTD04I",
            existingSchedule = ExistingScheduleUiModel(
                tripPlanId = "trip-plan-id",
                title = "오사카 여행",
            ),
        ),
    )

    /** FR-019 — 추천 영상 로딩 */
    @Test
    fun recommendedVideosLoading() {
        setScheduleContent(ScheduleUiState())
        composeRule.onNodeWithTag(ScheduleEditTestTags.RecommendedLoading).assertIsDisplayed()
        composeRule.onRoot().captureRoboImage()
    }

    /** FR-019 — 추천 영상 실패 + 다시 시도 */
    @Test
    fun recommendedVideosError() {
        val intents = mutableListOf<ScheduleIntent>()
        setScheduleContent(ScheduleUiState(recommendedVideos = RecommendedVideosState.Error), onIntent = intents::add)
        composeRule.onNodeWithText(ScheduleEditStrings.RecommendedError).assertIsDisplayed()
        composeRule.onRoot().captureRoboImage()
        composeRule.onNodeWithTag(ScheduleEditTestTags.RecommendedRetry).performClick()
        composeRule.runOnIdle { assertEquals(listOf(ScheduleIntent.LoadRecommendedVideos), intents) }
    }

    /** 17789:47177 — 튜토리얼 3단계: 첫 `링크복사` 코치마크 + 건너뛰기 */
    @Test
    fun tutorialStep3CopyLink() {
        setScheduleContent(contentState(tutorialStep = TutorialStep.COPY_LINK, isGuideVisible = true))
        composeRule.onNodeWithTag(CoachMarkDefaults.OverlayTestTag).assertIsDisplayed()
        composeRule.onNodeWithText(ScheduleEditStrings.Step3).assertIsDisplayed()
        composeRule.onRoot().captureRoboImage()
    }

    /** 지연 전에는 코치마크가 뜨지 않는다(FR-020) */
    @Test
    fun tutorialStep3BeforeGuideDelayHasNoOverlay() {
        setScheduleContent(contentState(tutorialStep = TutorialStep.COPY_LINK, isGuideVisible = false))
        assertEquals(0, composeRule.onAllNodesWithText(ScheduleEditStrings.Step3).fetchSemanticsNodes().size)
    }

    /** 17789:47208 — 튜토리얼 4단계: 붙여넣기 칩 코치마크 + 클립보드 토스트 */
    @Test
    fun tutorialStep4PasteLinkWithClipboardToast() {
        setScheduleContent(
            contentState(
                tutorialStep = TutorialStep.PASTE_LINK,
                hasClipboardText = true,
                clipboardToastUrl = "https://youtu.be/OrGmEVTD04I",
            ),
        )
        composeRule.onNodeWithText(ScheduleEditStrings.Step4).assertIsDisplayed()
        composeRule.onNodeWithTag(ScheduleEditTestTags.ClipboardToast).assertIsDisplayed()
        composeRule.onRoot().captureRoboImage()
    }

    /** 17789:47195 — 자유 조작 단계 + 클립보드 토스트, 붙여넣기 칩 활성 */
    @Test
    fun clipboardToastInFreeStep() {
        val intents = mutableListOf<ScheduleIntent>()
        setScheduleContent(
            contentState(
                tutorialStep = TutorialStep.FREE,
                hasClipboardText = true,
                clipboardToastUrl = "https://youtu.be/OrGmEVTD04I",
            ),
            onIntent = intents::add,
        )
        composeRule.onNodeWithTag(ScheduleEditTestTags.PasteChip).assertIsEnabled()
        composeRule.onNodeWithTag(ScheduleEditTestTags.Skip).assertIsDisplayed()
        composeRule.onRoot().captureRoboImage()

        composeRule.onNodeWithTag(ScheduleEditTestTags.ClipboardToastClose).performClick()
        composeRule.onNodeWithTag(ScheduleEditTestTags.ClipboardToast).performClick()
        composeRule.onNodeWithTag(ScheduleEditTestTags.Skip).performClick()
        composeRule.runOnIdle {
            assertEquals(
                listOf(
                    ScheduleIntent.DismissClipboardToast,
                    ScheduleIntent.ApplyClipboardToast,
                    ScheduleIntent.SkipOnboarding,
                ),
                intents,
            )
        }
    }

    /** 온보딩 오류 토스트 */
    @Test
    fun errorToast() {
        setScheduleContent(
            contentState(
                tutorialStep = TutorialStep.FREE,
                videoLink = "https://youtu.be/other",
                videoLinkError = VideoLinkError.INVALID_LINK,
                errorToast = ScheduleEditStrings.ToastNotRecommended,
            ),
        )
        composeRule.onNodeWithTag(ScheduleEditTestTags.ErrorToast).assertIsDisplayed()
        composeRule.onRoot().captureRoboImage()
    }

    /** FR-022 — 클립보드가 비어 있으면 붙여넣기 칩 비활성 */
    @Test
    fun pasteChipDisabledWithoutClipboardText() {
        setScheduleContent(contentState(hasClipboardText = false))
        composeRule.onNodeWithTag(ScheduleEditTestTags.PasteChip).assertIsNotEnabled()
    }

    @Test
    fun recommendedVideosSwipeLeft() {
        setScheduleContent(contentState())

        composeRule
            .onNodeWithTag(RecommendedVideoListTestTag)
            .assert(hasScrollAction())
            .performTouchInput { swipeLeft() }

        composeRule
            .onNodeWithText("유명 신혼 여행지에 혼자 당당히 여행가는 사람【몰디브】")
            .assertIsDisplayed()
    }

    @Test
    fun recommendedVideoCopyEmitsIntentAndKeepsLabel() {
        val intents = mutableListOf<ScheduleIntent>()
        setScheduleContent(contentState(), onIntent = intents::add)

        composeRule
            .onAllNodesWithText(ScheduleEditStrings.Copy)[0]
            .performClick()

        composeRule
            .onAllNodesWithText(ScheduleEditStrings.Copy)[0]
            .assertIsDisplayed()
        composeRule
            .onAllNodesWithText("복사완료")
            .assertCountEquals(0)
        composeRule.runOnIdle {
            assertEquals(listOf(ScheduleIntent.CopyRecommendedLink(SampleVideos.first().videoUrl)), intents)
        }
    }

    private fun capture(state: ScheduleUiState) {
        setScheduleContent(state)
        composeRule.onRoot().captureRoboImage()
    }

    private fun setScheduleContent(
        state: ScheduleUiState,
        onIntent: (ScheduleIntent) -> Unit = {},
    ) {
        composeRule.setContent {
            CompositionLocalProvider(LocalInspectionMode provides true) {
                PreviewContextConfigurationEffect()
                LinkItTheme {
                    Box(Modifier.requiredSize(375.dp, 812.dp)) {
                        ScheduleEditContent(uiState = state, onIntent = onIntent)
                    }
                }
            }
        }
    }

    private fun contentState(
        videoLink: String = "",
        videoLinkError: VideoLinkError? = null,
        existingSchedule: ExistingScheduleUiModel? = null,
        tutorialStep: TutorialStep? = null,
        isGuideVisible: Boolean = false,
        hasClipboardText: Boolean = false,
        clipboardToastUrl: String? = null,
        errorToast: String? = null,
    ) = ScheduleUiState(
        videoLink = videoLink,
        videoLinkError = videoLinkError,
        existingSchedule = existingSchedule,
        recommendedVideos = RecommendedVideosState.Content(SampleVideos),
        tutorialStep = tutorialStep,
        isGuideVisible = isGuideVisible,
        hasClipboardText = hasClipboardText,
        clipboardToastUrl = clipboardToastUrl,
        errorToast = errorToast,
    )

    private companion object {
        /** 서버 `discover/category` 상위 항목 형태. 썸네일은 인스펙션 모드에서 로드하지 않는다 */
        val SampleVideos = listOf(
            sampleVideo("OrGmEVTD04I", "유부남과 함께 오사카 좋은 놀이공원 가보기 【오사카上】", 1_130_000),
            sampleVideo("zt1UffHle7o", "\"갸루들이 안 보이네요..?\" 24년 만의 도쿄 방문기", 930_000),
            sampleVideo("X4JVeFd19fU", "유명 신혼 여행지에 혼자 당당히 여행가는 사람【몰디브】", 810_000),
        )

        fun sampleVideo(id: String, title: String, viewCount: Long) = DiscoverVideo(
            videoId = id,
            videoUrl = "https://youtu.be/$id",
            title = title,
            description = "",
            thumbnailUrl = "https://i.ytimg.com/vi/$id/hqdefault.jpg",
            channelId = "channel",
            channelTitle = "채널",
            viewCount = viewCount,
            likeCount = 0,
            duration = "PT10M",
            publishedAt = "2026-01-01T00:00:00Z",
            region = "일본",
            country = "일본",
            city = "도쿄",
            theme = "FOOD",
        )
    }
}

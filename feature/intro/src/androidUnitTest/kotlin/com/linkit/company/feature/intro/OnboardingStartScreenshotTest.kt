package com.linkit.company.feature.intro

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.captureRoboImage
import com.linkit.company.core.designsystem.theme.LinkItTheme
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
class OnboardingStartScreenshotTest {
    @get:Rule
    val composeRule = createComposeRule()

    /** 18058:86645 — 온보딩 시작 화면 */
    @Test
    fun onboardingStart() {
        setContent()
        composeRule.onRoot().captureRoboImage()
    }

    /** 001 FR-026 — 앱 초기화 완료 토스트 (#48 IntroScreenshotTest 에서 이동) */
    @Test
    fun onboardingStart_resetCompletedToast() {
        setContent(showResetCompletedToast = true)
        composeRule.onNodeWithTag(OnboardingStartTestTags.ResetToast).assertIsDisplayed()
        composeRule.onNodeWithText(ResetCompletedToastMessage).assertIsDisplayed()
        composeRule.onRoot().captureRoboImage()
    }

    /** 작은 화면: 본문은 스크롤, 버튼은 하단 고정 */
    @Test
    fun onboardingStart_smallHeight() {
        setContent(height = 560.dp)
        composeRule.onNodeWithTag(OnboardingStartTestTags.SeeHowTo).assertIsDisplayed()
        composeRule.onNodeWithTag(OnboardingStartTestTags.StartNow).assertIsDisplayed()
        composeRule.onRoot().captureRoboImage()
    }

    @Test
    fun onboardingStart_buttonsEmitIntents() {
        val intents = mutableListOf<IntroIntent>()
        composeRule.setContent {
            CompositionLocalProvider(LocalInspectionMode provides true) {
                PreviewContextConfigurationEffect()
                LinkItTheme {
                    Box(Modifier.requiredSize(375.dp, 812.dp)) {
                        OnboardingStartScreen(uiState = IntroUiState(), onIntent = intents::add)
                    }
                }
            }
        }
        composeRule.onNodeWithTag(OnboardingStartTestTags.SeeHowTo).performClick()
        composeRule.onNodeWithTag(OnboardingStartTestTags.StartNow).performClick()
        composeRule.runOnIdle {
            assertEquals(listOf(IntroIntent.TapSeeHowTo, IntroIntent.TapStartNow), intents)
        }
    }

    private fun setContent(
        showResetCompletedToast: Boolean = false,
        height: androidx.compose.ui.unit.Dp = 812.dp,
    ) {
        composeRule.setContent {
            CompositionLocalProvider(LocalInspectionMode provides true) {
                PreviewContextConfigurationEffect()
                LinkItTheme {
                    Box(Modifier.requiredSize(375.dp, height)) {
                        OnboardingStartContent(
                            onSeeHowTo = {},
                            onStartNow = {},
                            showResetCompletedToast = showResetCompletedToast,
                        )
                    }
                }
            }
        }
    }
}

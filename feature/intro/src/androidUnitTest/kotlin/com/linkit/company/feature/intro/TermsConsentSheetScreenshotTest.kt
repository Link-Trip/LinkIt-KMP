package com.linkit.company.feature.intro

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.captureRoboImage
import com.linkit.company.core.designsystem.theme.LinkItTheme
import com.linkit.company.domain.model.terms.TermsDocumentType
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
class TermsConsentSheetScreenshotTest {
    @get:Rule
    val composeRule = createComposeRule()

    /** 18580:37864 — 미체크, `동의하고 시작하기` 비활성 */
    @Test
    fun termsSheet_unchecked() {
        setContent(TermsSheetState())
        composeRule.onNodeWithTag(TermsConsentTestTags.Start).assertIsNotEnabled()
        composeRule.onRoot().captureRoboImage()
    }

    /** 18580:39134 — 전체 체크, 버튼 활성 */
    @Test
    fun termsSheet_allChecked() {
        setContent(TermsSheetState(serviceAgreed = true, privacyAgreed = true))
        composeRule.onNodeWithTag(TermsConsentTestTags.Start).assertIsEnabled()
        composeRule.onRoot().captureRoboImage()
    }

    /** 하나만 체크: 전체 동의 해제, 버튼 비활성 (FR-007·FR-008) */
    @Test
    fun termsSheet_partiallyChecked() {
        setContent(TermsSheetState(serviceAgreed = true))
        composeRule.onNodeWithTag(TermsConsentTestTags.Start).assertIsNotEnabled()
        composeRule.onRoot().captureRoboImage()
    }

    @Test
    fun termsSheet_rowsEmitIntents() {
        val intents = mutableListOf<IntroIntent>()
        setContent(TermsSheetState(serviceAgreed = true, privacyAgreed = true), onIntent = intents::add)

        composeRule.onNodeWithTag(TermsConsentTestTags.AllCheckbox).performClick()
        composeRule.onNodeWithTag(TermsConsentTestTags.ServiceCheckbox).performClick()
        composeRule.onNodeWithTag(TermsConsentTestTags.PrivacyCheckbox).performClick()
        composeRule.onNodeWithTag(TermsConsentTestTags.ServiceDetail).performClick()
        composeRule.onNodeWithTag(TermsConsentTestTags.PrivacyDetail).performClick()
        composeRule.onNodeWithTag(TermsConsentTestTags.Start).performClick()

        composeRule.runOnIdle {
            assertEquals(
                listOf(
                    IntroIntent.ToggleAllTerms,
                    IntroIntent.ToggleServiceTerms,
                    IntroIntent.TogglePrivacyTerms,
                    IntroIntent.OpenTermsDetail(TermsDocumentType.SERVICE),
                    IntroIntent.OpenTermsDetail(TermsDocumentType.PRIVACY),
                    IntroIntent.AgreeAndStart,
                ),
                intents,
            )
        }
    }

    private fun setContent(
        state: TermsSheetState,
        onIntent: (IntroIntent) -> Unit = {},
    ) {
        composeRule.setContent {
            CompositionLocalProvider(LocalInspectionMode provides true) {
                PreviewContextConfigurationEffect()
                LinkItTheme {
                    Box(Modifier.requiredWidth(375.dp)) {
                        TermsConsentSheetContent(state = state, onIntent = onIntent)
                    }
                }
            }
        }
    }
}

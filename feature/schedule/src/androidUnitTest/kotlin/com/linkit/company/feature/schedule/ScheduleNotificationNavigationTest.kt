package com.linkit.company.feature.schedule

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.entryProvider
import com.linkit.company.core.designsystem.theme.LinkItTheme
import com.linkit.company.core.navigation.LinkItNavDisplay
import com.linkit.company.core.navigation.LinkItNavKey
import com.linkit.company.core.navigation.LinkItSavedStateConfiguration
import com.linkit.company.core.navigation.rememberNavigationState
import com.linkit.company.feature.schedule.navigation.scheduleEditEntry
import org.jetbrains.compose.resources.PreviewContextConfigurationEffect
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(manifest = Config.NONE, sdk = [35], qualifiers = "w375dp-h812dp-mdpi")
class ScheduleNotificationNavigationTest {
    @get:Rule val composeRule = createComposeRule()
    private val isPrompted = mutableStateOf<Boolean?>(false)
    private val isDismissed = mutableStateOf(false)

    @Test
    fun laterDismissesPromptWithoutChangingNavigationEntry() {
        setNavigationContent()
        composeRule.onNodeWithText("완료되면 바로 알려드릴게요!").assertIsDisplayed()

        composeRule.onNodeWithText("나중에 할게요").performClick()

        assertPromptDismissed()
    }

    @Test
    fun closeDismissesPromptWithoutChangingNavigationEntry() {
        setNavigationContent()
        composeRule.onNodeWithText("완료되면 바로 알려드릴게요!").assertIsDisplayed()

        composeRule.onNodeWithContentDescription("닫기").performClick()

        assertPromptDismissed()
    }

    @Test
    fun promptAppearsWhenSettingsFinishLoadingAfterEntryWasCached() {
        isPrompted.value = null
        setNavigationContent()
        composeRule.onNodeWithText("완료되면 바로 알려드릴게요!").assertDoesNotExist()

        composeRule.runOnIdle { isPrompted.value = false }

        composeRule.onNodeWithText("완료되면 바로 알려드릴게요!").assertIsDisplayed()
    }

    private fun assertPromptDismissed() {
        composeRule.onNodeWithText("완료되면 바로 알려드릴게요!").assertDoesNotExist()
        composeRule.onNodeWithText("나중에 할게요").assertDoesNotExist()
        composeRule.onNodeWithText("메인화면으로 돌아가기").assertIsDisplayed()
        composeRule.runOnIdle {
            assertEquals(true, isPrompted.value)
            assertTrue(isDismissed.value)
        }
    }

    private fun setNavigationContent() {
        composeRule.setContent {
            CompositionLocalProvider(LocalInspectionMode provides true) {
                PreviewContextConfigurationEffect()
                LinkItTheme {
                    val route = LinkItNavKey.ScheduleAnalysisLoading(videoTitle = "실제 영상 제목")
                    val navigationState = rememberNavigationState(
                        savedStateConfiguration = LinkItSavedStateConfiguration,
                        startRoute = route,
                        topLevelRoutes = setOf(route),
                    )
                    val provider = entryProvider {
                        scheduleEditEntry(
                            onCreateSchedule = { _, _ -> },
                            onOpenExistingSchedule = { _, _ -> },
                            onReturnHome = {},
                            showNotificationPermissionSheet = {
                                isPrompted.value == false && !isDismissed.value
                            },
                            onAllowNotifications = {},
                            onDismissNotificationPrompt = {
                                isPrompted.value = true
                                isDismissed.value = true
                            },
                            onConfirmAnalysis = {},
                            onBack = {},
                        )
                    }
                    Box(Modifier.requiredSize(375.dp, 812.dp)) {
                        LinkItNavDisplay(
                            backStack = navigationState.currentTopLevelBackStack,
                            onBack = {},
                            entryProvider = provider,
                        )
                    }
                }
            }
        }
    }
}

package com.linkit.company.feature.map.main

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
import com.linkit.company.domain.model.video.CostBasis
import com.linkit.company.domain.model.video.VideoScheduleCreationState
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
@Config(sdk = [35], qualifiers = "w375dp-h744dp-mdpi")
class MapApiContentTest {
    @get:Rule val composeRule = createComposeRule()

    @Test
    fun estimatedCostsPreserveMissingBounds() {
        val schedule = MapTestFixtures.schedules.first()
        assertEquals("최소 10만원", schedule.copy(estimatedMinCost = 100_000).estimatedCostLabel())
        assertEquals("최대 20만원", schedule.copy(estimatedMaxCost = 200_000).estimatedCostLabel())
        assertEquals("비용 정보 없음", schedule.estimatedCostLabel())
        assertEquals("10,500~20,300원", schedule.copy(estimatedMinCost = 10_500, estimatedMaxCost = 20_300).estimatedCostLabel())
    }

    @Test
    fun enrichedScheduleDisplaysAnalysisSummaryAndWideCostRange() {
        val schedule = MapTestFixtures.schedules.first().copy(
            analysisSummary = "궁궐과 시장을 둘러보는 서울 문화 여행",
            estimatedMinCost = 820_000,
            estimatedMaxCost = 1_200_000,
            costBasis = CostBasis.ITEM_ESTIMATED,
        )
        setMapContent(MapTestFixtures.contentState().copy(schedules = listOf(schedule)))

        composeRule.onNodeWithText("궁궐과 시장을 둘러보는 서울 문화 여행").assertIsDisplayed()
        composeRule.onNodeWithText("82~120만원").assertIsDisplayed()
        composeRule.onRoot().captureRoboImage()
    }

    @Test
    fun pendingAnalysisShowsProgressAndOpensInProgressExplanation() {
        val intents = mutableListOf<MapIntent>()
        setMapContent(
            state = MapTestFixtures.contentState().copy(
                videoCreationState = VideoScheduleCreationState.InProgress("pending-task"),
            ),
            onIntent = intents::add,
        )

        composeRule.onNodeWithText("일정 생성중...").assertIsDisplayed()
        composeRule.onNodeWithTag("map-video-creation-progress").performClick()
        assertEquals(listOf<MapIntent>(MapIntent.ShowCreationInProgressDialog), intents)
        composeRule.onRoot().captureRoboImage()
    }

    @Test
    fun completionOpensCreatedTripIdAndAcknowledgesMatchingTask() {
        val intents = mutableListOf<MapIntent>()
        var openedSchedule: Triple<String, String, String?>? = null
        setMapContent(
            state = MapTestFixtures.contentState().copy(
                videoCreationState = VideoScheduleCreationState.Completed(
                    taskId = "completed-task",
                    tripPlanId = "created-trip-42",
                    title = "새로 생성된 서울 여행",
                ),
            ),
            onIntent = intents::add,
            onOpenSchedule = { id, title, focusedPlaceId ->
                openedSchedule = Triple(id, title, focusedPlaceId)
            },
        )

        composeRule.onNodeWithText("일정 생성이 완료되었습니다.").assertIsDisplayed()
        composeRule.onRoot().captureRoboImage()
        composeRule.onNodeWithText("확인하기").performClick()
        assertEquals(Triple("created-trip-42", "새로 생성된 서울 여행", null), openedSchedule)
        assertEquals(listOf<MapIntent>(MapIntent.AcknowledgeVideoCreation("completed-task")), intents)
    }

    private fun setMapContent(
        state: MapUiState,
        onIntent: (MapIntent) -> Unit = {},
        onOpenSchedule: (String, String, String?) -> Unit = { _, _, _ -> },
    ) {
        composeRule.setContent {
            CompositionLocalProvider(LocalInspectionMode provides true) {
                PreviewContextConfigurationEffect()
                LinkItTheme {
                    Box(Modifier.requiredSize(375.dp, 744.dp)) {
                        MapContent(
                            uiState = state,
                            onIntent = onIntent,
                            onOpenSchedule = onOpenSchedule,
                        )
                    }
                }
            }
        }
    }
}

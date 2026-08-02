package com.linkit.company.feature.map.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertValueEquals
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeWithVelocity
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
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w375dp-h744dp-mdpi")
class MapBottomSheetGestureTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun sheetTracksFingerAndReturnsToNearestAnchor() {
        setMapContent()
        val initialTop = sheetTop()
        val handleCenter = handleCenterInRoot()

        composeRule.onRoot().performTouchInput {
            down(handleCenter)
        }
        composeRule.onRoot().performTouchInput {
            moveTo(handleCenter + Offset(x = 0f, y = -80f), delayMillis = 200)
        }
        composeRule.waitForIdle()

        assertTrue(
            "The sheet should move continuously with the pointer while it is held",
            sheetTop() < initialTop - 30f,
        )

        composeRule.onRoot().performTouchInput {
            advanceEventTime(1_000)
            up()
        }
        composeRule.waitForIdle()

        sheet().assertValueEquals("Resting")
        assertEquals(initialTop.toDouble(), sheetTop().toDouble(), 1.0)
    }

    @Test
    fun slowReleasePastUpperMidpointSettlesExpanded() {
        setMapContent()

        slowDragBy(deltaY = -240f)

        sheet().assertValueEquals("Expanded")
    }

    @Test
    fun slowReleasePastLowerMidpointSettlesCollapsed() {
        setMapContent()

        slowDragBy(deltaY = 190f)

        sheet().assertValueEquals("Collapsed")
    }

    @Test
    fun fastShortSwipeUsesVelocityDirection() {
        setMapContent()

        fastSwipeBy(deltaY = -80f)

        sheet().assertValueEquals("Expanded")
    }

    @Test
    fun bothExtremeAnchorsCanReturnToResting() {
        setMapContent()

        fastSwipeBy(deltaY = -80f)
        sheet().assertValueEquals("Expanded")
        fastSwipeBy(deltaY = 80f)
        sheet().assertValueEquals("Resting")

        fastSwipeBy(deltaY = 80f)
        sheet().assertValueEquals("Collapsed")
        fastSwipeBy(deltaY = -80f)
        sheet().assertValueEquals("Resting")
    }

    @Test
    fun createControlVariantMatchesVisibleSheetHeightAtAnchors() {
        setMapContent()

        assertTrue(sheetSurfaceVisibleHeight() >= CreateControlThresholdPx)
        assertCreateControlIconOnly()

        fastSwipeBy(deltaY = 80f)
        sheet().assertValueEquals("Collapsed")
        assertTrue(sheetSurfaceVisibleHeight() < CreateControlThresholdPx)
        assertCreateControlLabelled()

        fastSwipeBy(deltaY = -80f)
        sheet().assertValueEquals("Resting")
        fastSwipeBy(deltaY = -80f)
        sheet().assertValueEquals("Expanded")
        assertTrue(sheetSurfaceVisibleHeight() >= CreateControlThresholdPx)
        assertCreateControlIconOnly()
    }

    @Test
    fun createControlVariantUpdatesWhileDraggingAcrossThresholdInBothDirections() {
        setMapContent()
        val initialSurfaceVisibleHeight = sheetSurfaceVisibleHeight()
        val handleCenter = handleCenterInRoot()
        val downwardDistance =
            initialSurfaceVisibleHeight - CreateControlThresholdPx + ThresholdCrossingMarginPx

        assertTrue(initialSurfaceVisibleHeight >= CreateControlThresholdPx)
        assertCreateControlIconOnly()

        composeRule.onRoot().performTouchInput {
            down(handleCenter)
        }
        composeRule.onRoot().performTouchInput {
            moveTo(
                position = handleCenter + Offset(x = 0f, y = downwardDistance),
                delayMillis = 300,
            )
        }
        composeRule.waitForIdle()

        assertTrue(sheetSurfaceVisibleHeight() < CreateControlThresholdPx)
        assertCreateControlLabelled()

        composeRule.onRoot().performTouchInput {
            moveTo(
                position = handleCenter + Offset(x = 0f, y = -ThresholdCrossingMarginPx),
                delayMillis = 300,
            )
        }
        composeRule.waitForIdle()

        assertTrue(sheetSurfaceVisibleHeight() >= CreateControlThresholdPx)
        assertCreateControlIconOnly()

        composeRule.onRoot().performTouchInput {
            advanceEventTime(1_000)
            up()
        }
        composeRule.waitForIdle()
        sheet().assertValueEquals("Resting")
    }

    @Test
    fun createMenuExpandsAsIconOnlyCloseAndRestoresSheetBasedMode() {
        setMapContent(
            stateReducer = { state, intent ->
                when (intent) {
                    MapIntent.ToggleCreateMenu ->
                        state.copy(isCreateMenuExpanded = !state.isCreateMenuExpanded)
                    else -> state
                }
            },
        )

        sheet().assertValueEquals("Resting")
        assertCreateControlIconOnly()

        composeRule.onNodeWithTag(CreateControlTag).performClick()
        composeRule.waitForIdle()

        sheet().assertValueEquals("Resting")
        assertCreateControlIconOnly(CreateMenuCloseDescription)
        composeRule
            .onNodeWithText(CreateFromVideoLabel)
            .assertIsDisplayed()
            .assertIsEnabled()
            .assertHasClickAction()
        assertComingSoonCreateOption(CreateFromStorageLabel)
        assertComingSoonCreateOption(CreateManuallyLabel)

        composeRule.onNodeWithTag(CreateControlTag).performClick()
        composeRule.waitForIdle()

        assertCreateOptionDoesNotExist(CreateFromVideoLabel)
        assertCreateOptionDoesNotExist(CreateFromStorageLabel)
        assertCreateOptionDoesNotExist(CreateManuallyLabel)
        sheet().assertValueEquals("Resting")
        assertCreateControlIconOnly()
    }

    @Test
    fun comingSoonCreateOptionsShowAndDismissModalDialog() {
        var backgroundSelectionCount = 0
        setMapContent(
            stateReducer = { state, intent ->
                if (intent is MapIntent.SelectSchedule) {
                    backgroundSelectionCount++
                }
                reduceCreateState(state, intent)
            },
        )

        composeRule.onNodeWithTag(CreateControlTag).performClick()
        composeRule.onNodeWithText(CreateFromStorageLabel).performTouchInput { click() }

        composeRule.onNodeWithText(ComingSoonTitle).assertIsDisplayed()
        composeRule.runOnIdle { assertEquals(0, backgroundSelectionCount) }
        composeRule.onNodeWithText(ComingSoonDescription).assertIsDisplayed()
        composeRule.onNodeWithText(DialogConfirmLabel).assertIsDisplayed().performClick()
        assertCreateOptionDoesNotExist(ComingSoonTitle)

        composeRule.onNodeWithText(CreateManuallyLabel).performClick()

        composeRule.onNodeWithText(ComingSoonTitle).assertIsDisplayed()
        composeRule
            .onNodeWithContentDescription(DialogCloseLabel)
            .assertIsDisplayed()
            .performClick()
        assertCreateOptionDoesNotExist(ComingSoonTitle)
    }

    @Test
    fun filterPillsOpenAnchoredPopupsAndApplySelections() {
        setMapContent(stateReducer = ::reduceFilterState)

        composeRule.onNodeWithText(RegionFilterLabel).performClick()
        composeRule.onNodeWithTag(RegionFilterPopupTag).assertIsDisplayed()
        composeRule.onNodeWithText("부산광역시").performClick()
        assertPopupDoesNotExist(RegionFilterPopupTag)

        composeRule.onNodeWithText(StyleFilterLabel).performClick()
        composeRule.onNodeWithTag(StyleFilterPopupTag).assertIsDisplayed()
        listOf(
            "전체 스타일",
            "맛집 중심",
            "쇼핑 중심",
            "명소 탐방 중심",
            "자연·풍경 위주",
            "문화·역사 탐방",
            "액티비티",
            "힐링",
        ).forEach { label ->
            assertTrue(composeRule.onAllNodesWithText(label).fetchSemanticsNodes().isNotEmpty())
        }
        composeRule.onNodeWithText("전체 스타일").performClick()
        assertPopupDoesNotExist(StyleFilterPopupTag)

        composeRule.onNodeWithText(DurationFilterLabel).performClick()
        composeRule.onNodeWithTag(DurationFilterPopupTag).assertIsDisplayed()
        composeRule.onNodeWithText("1박 2일").performClick()
        assertPopupDoesNotExist(DurationFilterPopupTag)
        composeRule.onNodeWithText("1박 2일").assertIsDisplayed()
    }

    @Test
    fun emptyRegionPopupShowsDisabledGeneratedRegionMessage() {
        setMapContent(
            initialState = MapUiState(loadState = MapLoadState.EMPTY),
            stateReducer = ::reduceFilterState,
        )

        composeRule.onNodeWithText(RegionFilterLabel).performClick()

        composeRule.onNodeWithText("전체국가").assertIsDisplayed().assertIsEnabled()
        composeRule.onNodeWithText("생성된 지역 없음").assertIsDisplayed().assertIsNotEnabled()
    }

    @Test
    fun expandedEmptyStateCreateActionEmitsToggleCreateMenuOnce() {
        var toggleCreateMenuCount = 0
        setMapContent(
            initialState = MapUiState(loadState = MapLoadState.EMPTY),
            stateReducer = { state, intent ->
                if (intent == MapIntent.ToggleCreateMenu) {
                    toggleCreateMenuCount++
                }
                state
            },
        )

        fastSwipeBy(deltaY = -80f)
        sheet().assertValueEquals("Expanded")
        listOf(
            RegionFilterLabel,
            StyleFilterLabel,
            DurationFilterLabel,
            EmptySummaryLabel,
            LatestSortLabel,
            EmptyStateTitle,
        ).forEach { label ->
            composeRule
                .onNodeWithText(label)
                .assertIsDisplayed()
        }
        composeRule
            .onNodeWithText(EmptyCreateScheduleLabel)
            .assertIsDisplayed()
            .assertIsEnabled()
            .assertHasClickAction()
            .performClick()

        composeRule.runOnIdle {
            assertEquals(1, toggleCreateMenuCount)
        }
    }

    @Test
    fun errorStateRetryEmitsRetryLoadOnce() {
        var retryLoadCount = 0
        val errorDescription = "네트워크 연결을 확인하고 다시 시도해 주세요."
        setMapContent(
            initialState = MapUiState(
                loadState = MapLoadState.ERROR,
                errorMessage = errorDescription,
            ),
            stateReducer = { state, intent ->
                if (intent == MapIntent.RetryLoad) {
                    retryLoadCount++
                }
                state
            },
        )

        fastSwipeBy(deltaY = -80f)
        sheet().assertValueEquals("Expanded")
        composeRule
            .onNodeWithText(ErrorStateTitle)
            .assertIsDisplayed()
        composeRule
            .onNodeWithText(errorDescription)
            .assertIsDisplayed()
        composeRule
            .onNodeWithText(RetryLabel)
            .assertIsDisplayed()
            .assertIsEnabled()
            .assertHasClickAction()
            .performClick()

        composeRule.runOnIdle {
            assertEquals(1, retryLoadCount)
        }
    }

    @Test
    fun createMenuEnterAndExitAnimationsHaveIntermediateFramesAndSettle() {
        setMapContent(
            stateReducer = { state, intent ->
                when (intent) {
                    MapIntent.ToggleCreateMenu ->
                        state.copy(isCreateMenuExpanded = !state.isCreateMenuExpanded)
                    else -> state
                }
            },
        )
        composeRule.mainClock.autoAdvance = false

        try {
            toggleCreateMenuWithSemantics()
            advanceAnimationToStart()
            // A zero-height AnimatedVisibility is not exposed in the semantics tree.
            assertMenuDoesNotExist()
            val enterStartHeight = 0f

            composeRule.mainClock.advanceTimeBy(AnimationMidpointMillis)
            composeRule.waitForIdle()
            val enterMidHeight = menuHeight()
            assertTrue(
                "Enter midpoint should have a visible partial height " +
                    "(start=$enterStartHeight, mid=$enterMidHeight)",
                enterMidHeight > enterStartHeight + AnimationHeightTolerancePx,
            )

            composeRule.mainClock.advanceTimeBy(AnimationCompletionMillis)
            composeRule.waitForIdle()
            val enterFinalHeight = menuHeight()
            assertTrue(
                "Enter animation should grow beyond its midpoint " +
                    "(mid=$enterMidHeight, final=$enterFinalHeight)",
                enterFinalHeight > enterMidHeight + AnimationHeightTolerancePx,
            )
            assertCreateControlIconOnly(CreateMenuCloseDescription)
            composeRule
                .onNodeWithText(CreateFromVideoLabel)
                .assertIsEnabled()
                .assertHasClickAction()
            assertComingSoonCreateOption(CreateFromStorageLabel)
            assertComingSoonCreateOption(CreateManuallyLabel)

            composeRule.mainClock.advanceTimeBy(AnimationSettleProbeMillis)
            composeRule.waitForIdle()
            assertEquals(
                enterFinalHeight.toDouble(),
                menuHeight().toDouble(),
                AnimationHeightTolerancePx.toDouble(),
            )

            toggleCreateMenuWithSemantics()
            advanceAnimationToStart()
            val exitStartHeight = menuHeight()
            assertEquals(
                enterFinalHeight.toDouble(),
                exitStartHeight.toDouble(),
                AnimationHeightTolerancePx.toDouble(),
            )

            composeRule.mainClock.advanceTimeBy(AnimationMidpointMillis)
            composeRule.waitForIdle()
            val exitMidHeight = menuHeight()
            assertTrue(
                "Exit midpoint should shrink but remain visible " +
                    "(start=$exitStartHeight, mid=$exitMidHeight)",
                exitMidHeight > AnimationHeightTolerancePx &&
                    exitMidHeight < exitStartHeight - AnimationHeightTolerancePx,
            )

            composeRule.mainClock.advanceTimeBy(AnimationCompletionMillis)
            composeRule.waitForIdle()
            assertMenuDoesNotExist()
            assertCreateOptionDoesNotExist(CreateFromVideoLabel)
            assertCreateOptionDoesNotExist(CreateFromStorageLabel)
            assertCreateOptionDoesNotExist(CreateManuallyLabel)
            assertCreateControlIconOnly()
        } finally {
            composeRule.mainClock.autoAdvance = true
            composeRule.waitForIdle()
        }
    }

    @Test
    fun reverseDragInterruptsSettlingAnimationWithoutPositionJump() {
        setMapContent()
        composeRule.mainClock.autoAdvance = false

        composeRule.onNodeWithTag(HandleTag).performTouchInput {
            swipeWithVelocity(
                start = center,
                end = center + Offset(x = 0f, y = -160f),
                endVelocity = 1_200f,
            )
        }
        composeRule.mainClock.advanceTimeBy(48)
        val topDuringSettle = sheetTop()
        val handleDuringSettle = handleCenterInRoot()

        composeRule.onRoot().performTouchInput {
            down(handleDuringSettle)
            moveTo(
                position = handleDuringSettle + Offset(x = 0f, y = 40f),
                delayMillis = 100,
            )
            moveTo(
                position = handleDuringSettle + Offset(x = 0f, y = 120f),
                delayMillis = 100,
            )
        }
        composeRule.mainClock.advanceTimeBy(32)
        val topAfterReverseDrag = sheetTop()
        assertTrue(
            "The interrupted sheet should immediately follow the reverse drag " +
                "(settling=$topDuringSettle, dragged=$topAfterReverseDrag)",
            topAfterReverseDrag > topDuringSettle + 30f,
        )

        composeRule.onRoot().performTouchInput {
            advanceEventTime(1_000)
            up()
        }
        composeRule.mainClock.autoAdvance = true
        composeRule.waitForIdle()

        sheet().assertValueEquals("Resting")
    }

    @Test
    fun scheduleRowShowsPreviewBeforeOpeningScheduleDetail() {
        var detailOpenCount = 0
        setMapContent(
            stateReducer = { state, intent ->
                when (intent) {
                    is MapIntent.SelectSchedule -> state.copy(
                        selectedScheduleId = intent.scheduleId,
                        selectedPlaceMarkerId = null,
                    )
                    else -> state
                }
            },
            onOpenSchedule = { detailOpenCount++ },
        )

        composeRule
            .onNodeWithTag("map-schedule-${MapTestFixtures.SeoulScheduleId}")
            .performClick()

        composeRule
            .onNodeWithTag("map-selected-schedule-${MapTestFixtures.SeoulScheduleId}")
            .assertIsDisplayed()
        sheet().assertValueEquals("Resting")
        composeRule.runOnIdle {
            assertEquals(0, detailOpenCount)
        }

        composeRule
            .onNodeWithTag("map-selected-schedule-${MapTestFixtures.SeoulScheduleId}")
            .performClick()
        composeRule.runOnIdle {
            assertEquals(1, detailOpenCount)
        }
    }

    private fun setMapContent(
        initialState: MapUiState = MapTestFixtures.contentState(),
        stateReducer: (MapUiState, MapIntent) -> MapUiState = { state, _ -> state },
        onOpenSchedule: () -> Unit = {},
    ) {
        composeRule.setContent {
            var state by remember { mutableStateOf(initialState) }
            CompositionLocalProvider(LocalInspectionMode provides true) {
                PreviewContextConfigurationEffect()
                LinkItTheme {
                    Box(Modifier.requiredSize(375.dp, 744.dp)) {
                        MapContent(
                            uiState = state,
                            onIntent = { intent ->
                                state = stateReducer(state, intent)
                            },
                            onOpenSchedule = { _, _, _ -> onOpenSchedule() },
                        )
                    }
                }
            }
        }
        composeRule.waitForIdle()
        sheet().assertValueEquals("Resting")
    }

    private fun slowDragBy(deltaY: Float) {
        val handleCenter = handleCenterInRoot()
        composeRule.onRoot().performTouchInput {
            down(handleCenter)
            moveTo(
                position = handleCenter + Offset(x = 0f, y = deltaY),
                delayMillis = 500,
            )
            advanceEventTime(1_000)
            up()
        }
        composeRule.waitForIdle()
    }

    private fun fastSwipeBy(deltaY: Float) {
        composeRule.onNodeWithTag(HandleTag).performTouchInput {
            swipeWithVelocity(
                start = center,
                end = center + Offset(x = 0f, y = deltaY),
                endVelocity = 1_200f,
            )
        }
        composeRule.waitForIdle()
    }

    private fun sheet() = composeRule.onNodeWithTag(SheetTag)

    private fun sheetTop(): Float = sheet().fetchSemanticsNode().boundsInRoot.top

    private fun sheetSurfaceVisibleHeight(): Float =
        composeRule.onRoot().fetchSemanticsNode().boundsInRoot.bottom -
            sheetTop() -
            MapLocationPillHeightPx

    private fun handleCenterInRoot(): Offset =
        composeRule.onNodeWithTag(HandleTag).fetchSemanticsNode().boundsInRoot.center

    private fun toggleCreateMenuWithSemantics() {
        composeRule
            .onNodeWithTag(CreateControlTag)
            .performSemanticsAction(SemanticsActions.OnClick)
    }

    private fun advanceAnimationToStart() {
        composeRule.mainClock.advanceTimeBy(AnimationStartFramesMillis)
        composeRule.waitForIdle()
    }

    private fun menuHeight(): Float =
        composeRule.onNodeWithTag(CreateMenuTag).fetchSemanticsNode().boundsInRoot.height

    private fun assertMenuDoesNotExist() {
        assertEquals(
            0,
            composeRule.onAllNodesWithTag(CreateMenuTag).fetchSemanticsNodes().size,
        )
    }

    private fun assertCreateControlLabelled() {
        composeRule
            .onNodeWithTag(CreateControlTag)
            .assertValueEquals("Labelled")
            .assertContentDescriptionEquals(CreateMenuOpenDescription)
        composeRule
            .onNodeWithText(CreateScheduleLabel, useUnmergedTree = true)
            .assertIsDisplayed()
    }

    private fun assertCreateControlIconOnly(
        expectedContentDescription: String = CreateMenuOpenDescription,
    ) {
        composeRule
            .onNodeWithTag(CreateControlTag)
            .assertValueEquals("IconOnly")
            .assertContentDescriptionEquals(expectedContentDescription)
        val hiddenLabel = if (expectedContentDescription == CreateMenuCloseDescription) {
            CloseLabel
        } else {
            CreateScheduleLabel
        }
        assertCreateOptionDoesNotExist(hiddenLabel)
    }

    private fun assertComingSoonCreateOption(label: String) {
        composeRule
            .onNodeWithText(label)
            .assertIsDisplayed()
            .assertIsEnabled()
            .assertHasClickAction()
    }

    private fun assertCreateOptionDoesNotExist(label: String) {
        assertEquals(
            0,
            composeRule
                .onAllNodesWithText(label, useUnmergedTree = true)
                .fetchSemanticsNodes()
                .size,
        )
    }

    private fun assertPopupDoesNotExist(tag: String) {
        assertEquals(0, composeRule.onAllNodesWithTag(tag).fetchSemanticsNodes().size)
    }

    private fun reduceFilterState(state: MapUiState, intent: MapIntent): MapUiState = when (intent) {
        is MapIntent.ToggleFilter -> state.copy(
            expandedFilter = intent.filter.takeUnless { it == state.expandedFilter },
        )
        is MapIntent.SelectRegion -> state.copy(
            selectedRegion = intent.region,
            expandedFilter = null,
        )
        is MapIntent.SelectStyle -> state.copy(
            selectedStyle = intent.style,
            expandedFilter = null,
        )
        is MapIntent.SelectDuration -> state.copy(
            durationFilter = intent.duration,
            expandedFilter = null,
        )
        else -> state
    }

    private fun reduceCreateState(state: MapUiState, intent: MapIntent): MapUiState = when (intent) {
        MapIntent.ToggleCreateMenu -> state.copy(
            isCreateMenuExpanded = !state.isCreateMenuExpanded,
        )
        MapIntent.ShowComingSoonDialog -> state.copy(
            isComingSoonDialogVisible = true,
        )
        MapIntent.DismissComingSoonDialog -> state.copy(
            isComingSoonDialogVisible = false,
        )
        else -> state
    }

    private companion object {
        const val SheetTag = "map-bottom-sheet"
        const val HandleTag = "map-bottom-sheet-handle"
        const val CreateControlTag = "map-create-schedule-control"
        const val CreateMenuTag = "map-create-schedule-menu"
        const val CreateScheduleLabel = "일정 생성"
        const val CloseLabel = "닫기"
        const val CreateMenuOpenDescription = "일정 생성 메뉴 열기"
        const val CreateMenuCloseDescription = "일정 생성 메뉴 닫기"
        const val CreateFromVideoLabel = "영상 링크로 만들기"
        const val CreateFromStorageLabel = "보관함에서 가져오기"
        const val CreateManuallyLabel = "직접 만들기"
        const val ComingSoonTitle = "해당 기능은\n곧 출시 예정이에요!"
        const val ComingSoonDescription = "조금만 기다려 주세요"
        const val DialogConfirmLabel = "확인"
        const val DialogCloseLabel = "닫기"
        const val EmptyCreateScheduleLabel = "일정 생성하기"
        const val RetryLabel = "다시 시도"
        const val RegionFilterLabel = "국가"
        const val StyleFilterLabel = "여행 스타일"
        const val DurationFilterLabel = "기간"
        const val RegionFilterPopupTag = "map-filter-region-popup"
        const val StyleFilterPopupTag = "map-filter-style-popup"
        const val DurationFilterPopupTag = "map-filter-duration-popup"
        const val EmptySummaryLabel = "총 0개 일정"
        const val LatestSortLabel = "최신순"
        const val EmptyStateTitle = "아직 등록된 일정이 없습니다."
        const val ErrorStateTitle = "일정을 불러오지 못했어요"

        // Figma's 380.dp ruler includes the 76.dp bottom navigation outside MapContent.
        // The mdpi MapContent test therefore uses a 304px visible-surface threshold.
        const val MapLocationPillHeightPx = 57f
        const val CreateControlThresholdPx = 304f
        const val ThresholdCrossingMarginPx = 16f

        const val AnimationStartFramesMillis = 32L
        const val AnimationMidpointMillis = 96L
        const val AnimationCompletionMillis = 160L
        const val AnimationSettleProbeMillis = 240L
        const val AnimationHeightTolerancePx = 1f
    }
}

package com.linkit.company.feature.map.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.assertValueEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeWithVelocity
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
@Config(sdk = [35], qualifiers = "w375dp-h744dp-mdpi")
class MapScreenshotTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun defaultMap() = capture(
        state = MapTestFixtures.contentState(),
        createControlExpectation = CreateControlExpectation.IconOnly,
    )

    @Test
    fun defaultMapExpanded() = capture(
        state = MapTestFixtures.contentState(),
        sheetGesture = SheetGesture.EXPAND,
        createControlExpectation = CreateControlExpectation.IconOnly,
    )

    @Test
    fun defaultMapCollapsed() = capture(
        state = MapTestFixtures.contentState(),
        sheetGesture = SheetGesture.COLLAPSE,
        createControlExpectation = CreateControlExpectation.Labelled,
    )

    @Test
    fun scheduleSelected() = capture(
        state = MapTestFixtures.contentState(
            selectedScheduleId = MapTestFixtures.SeoulScheduleId,
        ),
        createControlExpectation = CreateControlExpectation.Absent,
    )

    @Test
    fun createMenuExpanded() = capture(
        state = MapTestFixtures.contentState().copy(isCreateMenuExpanded = true),
        createControlExpectation = CreateControlExpectation.IconOnlyClose,
    )

    @Test
    fun comingSoonDialog() = capture(
        state = MapTestFixtures.contentState().copy(
            isCreateMenuExpanded = true,
            isComingSoonDialogVisible = true,
        ),
        createControlExpectation = CreateControlExpectation.IconOnlyClose,
    )

    @Test
    fun regionFilterExpanded() = capture(
        state = MapTestFixtures.contentState().copy(expandedFilter = MapFilterType.REGION),
        createControlExpectation = CreateControlExpectation.IconOnly,
    )

    @Test
    fun styleFilterExpanded() = capture(
        state = MapTestFixtures.contentState().copy(expandedFilter = MapFilterType.STYLE),
        createControlExpectation = CreateControlExpectation.IconOnly,
    )

    @Test
    fun durationFilterExpanded() = capture(
        state = MapTestFixtures.contentState().copy(expandedFilter = MapFilterType.DURATION),
        createControlExpectation = CreateControlExpectation.IconOnly,
    )

    @Test
    fun placeSelected() {
        val debugSchedule = MapDebugMockData.schedules.first().toMapScheduleUiModel()
        val selectedPlace = debugSchedule.places[2].copy(
            name = "루브르 박물관",
            categoryLabel = "음식점",
            description = "세계 각지의 유물이 모여있는 박물관 입니다. 모나리자를 위해 드농 윙에 집중하세요.",
            address = "뤼 드 리볼리, 75001 파리",
        )
        val schedule = debugSchedule.copy(
            title = "도쿄 신주쿠 여행",
            places = debugSchedule.places.toMutableList().apply { this[2] = selectedPlace },
        )

        capture(
            MapUiState(
                loadState = MapLoadState.CONTENT,
                schedules = listOf(schedule),
                selectedScheduleId = schedule.id,
                selectedPlaceMarkerId = selectedPlace.markerId,
                mapCenterLocationLabel = "일본, 도쿄",
            ),
        )
    }

    @Test
    fun placeCardActionsFollowSelectionSpec() {
        val intents = mutableListOf<MapIntent>()
        var scheduleRequest: Triple<String, String, String?>? = null
        var detailRequest: MapPlaceUiModel? = null
        val state = MapTestFixtures.contentState(
            selectedScheduleId = MapTestFixtures.SeoulScheduleId,
            selectedPlaceMarkerId = MapTestFixtures.MarketMarkerId,
        )

        setPlaceContent(
            state = state,
            onIntent = intents::add,
            onOpenSchedule = { id, title, placeId ->
                scheduleRequest = Triple(id, title, placeId)
            },
            onOpenPlaceDetail = { detailRequest = it },
        )

        composeRule.onNodeWithTag("map-place-card").assertIsDisplayed()
        composeRule.onNodeWithText("2일차 2번째 여행").assertIsDisplayed()
        composeRule.onNodeWithTag("map-selected-schedule-marker").assertIsDisplayed()
        composeRule.onNodeWithTag("map-place-previous").assertIsEnabled().performClick()
        composeRule.onNodeWithTag("map-place-next").assertIsNotEnabled()
        composeRule.onNodeWithTag("map-place-close").performClick()
        composeRule.onNodeWithTag("map-place-view-schedule").performClick()
        composeRule.onNodeWithTag("map-place-open-detail").performClick()

        composeRule.runOnIdle {
            assertEquals(
                listOf(MapIntent.ShowPreviousPlace, MapIntent.ClosePlace),
                intents,
            )
            assertEquals(
                Triple(
                    MapTestFixtures.SeoulScheduleId,
                    state.selectedSchedule?.title,
                    state.selectedPlace?.placeId,
                ),
                scheduleRequest,
            )
            assertEquals(state.selectedPlace, detailRequest)
        }
    }

    @Test
    fun placeCardDisablesPreviousAndEnablesNextAtFirstPlace() {
        setPlaceContent(
            state = MapTestFixtures.contentState(
                selectedScheduleId = MapTestFixtures.SeoulScheduleId,
                selectedPlaceMarkerId = MapTestFixtures.PalaceMarkerId,
            ),
        )

        composeRule.onNodeWithTag("map-place-previous").assertIsNotEnabled()
        composeRule.onNodeWithTag("map-place-next").assertIsEnabled()
    }

    @Test
    fun loadingMap() = capture(
        state = MapUiState(
            loadState = MapLoadState.LOADING,
            mapCenterLocationLabel = DefaultMapCenterLabel,
        ),
        createControlExpectation = CreateControlExpectation.IconOnly,
    )

    @Test
    fun emptyMap() = capture(
        state = MapUiState(
            loadState = MapLoadState.EMPTY,
            mapCenterLocationLabel = DefaultMapCenterLabel,
        ),
        createControlExpectation = CreateControlExpectation.IconOnly,
    )

    @Test
    fun emptyMapExpanded() = capture(
        state = MapUiState(
            loadState = MapLoadState.EMPTY,
            mapCenterLocationLabel = DefaultMapCenterLabel,
        ),
        sheetGesture = SheetGesture.EXPAND,
        createControlExpectation = CreateControlExpectation.IconOnly,
    )

    @Test
    fun errorMap() = capture(
        state = MapUiState(
            loadState = MapLoadState.ERROR,
            errorMessage = "네트워크 연결을 확인하고 다시 시도해 주세요.",
            mapCenterLocationLabel = DefaultMapCenterLabel,
        ),
        createControlExpectation = CreateControlExpectation.IconOnly,
    )

    @Test
    fun errorMapExpanded() = capture(
        state = MapUiState(
            loadState = MapLoadState.ERROR,
            errorMessage = "네트워크 연결을 확인하고 다시 시도해 주세요.",
            mapCenterLocationLabel = DefaultMapCenterLabel,
        ),
        sheetGesture = SheetGesture.EXPAND,
        createControlExpectation = CreateControlExpectation.IconOnly,
    )

    @Test
    fun topActionsMatchFigma() {
        composeRule.setContent {
            CompositionLocalProvider(LocalInspectionMode provides true) {
                PreviewContextConfigurationEffect()
                LinkItTheme {
                    MapContent(uiState = MapTestFixtures.contentState(), onIntent = {})
                }
            }
        }

        assertEquals(1, composeRule.onAllNodesWithContentDescription("마이페이지").fetchSemanticsNodes().size)
        assertEquals(1, composeRule.onAllNodesWithContentDescription("지도 종류 변경").fetchSemanticsNodes().size)
        assertEquals(0, composeRule.onAllNodesWithContentDescription("현재 위치로 이동").fetchSemanticsNodes().size)
    }

    @Test
    fun mapCenterLocationShowsCountryAndRegion() {
        composeRule.setContent {
            CompositionLocalProvider(LocalInspectionMode provides true) {
                PreviewContextConfigurationEffect()
                LinkItTheme {
                    MapContent(uiState = MapTestFixtures.contentState(), onIntent = {})
                }
            }
        }

        composeRule
            .onNodeWithTag("map-center-location-label")
            .assertTextEquals("대한민국, 서울특별시")
    }

    private fun capture(
        state: MapUiState,
        sheetGesture: SheetGesture? = null,
        createControlExpectation: CreateControlExpectation? = null,
    ) {
        composeRule.setContent {
            CompositionLocalProvider(LocalInspectionMode provides true) {
                PreviewContextConfigurationEffect()
                LinkItTheme {
                    Box(Modifier.requiredSize(375.dp, 744.dp)) {
                        MapContent(uiState = state, onIntent = {})
                    }
                }
            }
        }

        sheetGesture?.let(::settleSheet)
        assertCreateControl(createControlExpectation)
        composeRule.onRoot().captureRoboImage()
    }

    private fun setPlaceContent(
        state: MapUiState,
        onIntent: (MapIntent) -> Unit = {},
        onOpenSchedule: (String, String, String?) -> Unit = { _, _, _ -> },
        onOpenPlaceDetail: (MapPlaceUiModel) -> Unit = {},
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
                            onOpenPlaceDetail = onOpenPlaceDetail,
                        )
                    }
                }
            }
        }
    }

    private fun assertCreateControl(expectation: CreateControlExpectation?) {
        when (expectation) {
            CreateControlExpectation.Labelled ->
                composeRule
                    .onNodeWithTag(CreateControlTag)
                    .assertValueEquals("Labelled")
                    .assertContentDescriptionEquals(CreateMenuOpenDescription)
            CreateControlExpectation.IconOnly ->
                composeRule
                    .onNodeWithTag(CreateControlTag)
                    .assertValueEquals("IconOnly")
                    .assertContentDescriptionEquals(CreateMenuOpenDescription)
            CreateControlExpectation.IconOnlyClose ->
                composeRule
                    .onNodeWithTag(CreateControlTag)
                    .assertValueEquals("IconOnly")
                    .assertContentDescriptionEquals(CreateMenuCloseDescription)
            CreateControlExpectation.Absent ->
                assertEquals(
                    0,
                    composeRule
                        .onAllNodesWithTag(CreateControlTag)
                        .fetchSemanticsNodes()
                        .size,
                )
            null -> Unit
        }
    }

    private fun settleSheet(gesture: SheetGesture) {
        composeRule.onNodeWithTag("map-bottom-sheet-handle").performTouchInput {
            val delta = when (gesture) {
                SheetGesture.EXPAND -> -80f
                SheetGesture.COLLAPSE -> 80f
            }
            swipeWithVelocity(
                start = center,
                end = center + Offset(x = 0f, y = delta),
                endVelocity = 1_200f,
            )
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("map-bottom-sheet").assertValueEquals(gesture.expectedState)
    }

    private enum class SheetGesture(val expectedState: String) {
        EXPAND("Expanded"),
        COLLAPSE("Collapsed"),
    }

    private enum class CreateControlExpectation {
        Labelled,
        IconOnly,
        IconOnlyClose,
        Absent,
    }

    private companion object {
        const val DefaultMapCenterLabel = "대한민국, 서울특별시"
        const val CreateControlTag = "map-create-schedule-control"
        const val CreateMenuOpenDescription = "일정 생성 메뉴 열기"
        const val CreateMenuCloseDescription = "일정 생성 메뉴 닫기"
    }
}

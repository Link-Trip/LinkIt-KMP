package com.linkit.company.feature.schedule

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.github.takahirom.roborazzi.captureRoboImage
import com.linkit.company.core.designsystem.theme.LinkItTheme
import com.linkit.company.domain.model.place.Place
import com.linkit.company.domain.model.place.PlaceCategory
import com.linkit.company.domain.model.tripplan.TripPlanDetail
import com.linkit.company.domain.model.tripplan.TripPlanItem
import com.linkit.company.domain.model.video.VideoAnalysis
import com.linkit.company.domain.model.video.VideoAnalysisStatus
import com.linkit.company.domain.model.video.VideoTimeline
import com.linkit.company.domain.usecase.TripPlanContent
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
class TripDetailApiContentTest {
    @get:Rule val composeRule = createComposeRule()

    @Test
    fun displaysActualPlacesAndChangesDayWithoutShowingMockPlaces() {
        composeRule.setContent {
            var day by remember { mutableStateOf(1) }
            LinkItTheme {
                ScheduleTripDetailContent(
                    uiState = ScheduleUiState(showTripMapPreview = false),
                    tripPlanId = "trip",
                    detailState = TripDetailUiState(false, content(), day),
                    onIntent = {},
                    onSelectDay = { day = it },
                )
            }
        }
        composeRule.onNodeWithText("서울 여행").assertIsDisplayed()
        composeRule.onNodeWithText("경복궁").assertIsDisplayed()
        composeRule.onNodeWithText("루브르 박물관").assertDoesNotExist()
        composeRule.onNodeWithTag("trip-detail-day-2").performClick()
        composeRule.onNodeWithText("남산").assertIsDisplayed()
        composeRule.onNodeWithText("경복궁").assertDoesNotExist()
        composeRule.onNodeWithText("자세히 보기 ›").performClick()
        composeRule.onNodeWithText("편한 신발을 준비하세요").assertIsDisplayed()
        composeRule.onRoot().captureRoboImage()
    }

    @Test
    fun summaryShowsServerSummaryCostsAndTimelineWithoutInventedMetadata() {
        composeRule.setContent {
            LinkItTheme {
                ScheduleTripDetailContent(
                    uiState = ScheduleUiState(showTripMapPreview = false, tripDetailTab = TripDetailTab.SUMMARY),
                    tripPlanId = "trip",
                    detailState = TripDetailUiState(false, content()),
                    onIntent = {},
                )
            }
        }
        composeRule.onNodeWithText("서울의 문화 명소를 둘러보세요").assertIsDisplayed()
        composeRule.onNodeWithText("10,000원 ~ 20,000원").assertIsDisplayed()
        composeRule.onNodeWithText("남산 도착").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Sarah Kim  ·  2.3M views").assertDoesNotExist()
        composeRule.onRoot().captureRoboImage()
    }

    @Test
    fun focusedPlaceIsBroughtIntoViewAndShowsItsTips() {
        val base = content()
        val focusedContent = base.copy(
            tripPlan = base.tripPlan.copy(
                items = (1..8).map { item("item-$it", "방문 장소 $it", 2) } +
                    item("focused", "선택한 장소", 2),
            ),
        )
        composeRule.setContent {
            LinkItTheme {
                ScheduleTripDetailContent(
                    uiState = ScheduleUiState(showTripMapPreview = false),
                    tripPlanId = "trip",
                    focusedPlaceId = "place-focused",
                    detailState = TripDetailUiState(false, focusedContent, selectedDay = 2),
                    onIntent = {},
                )
            }
        }
        composeRule.onNodeWithText("선택한 장소").assertIsDisplayed()
        composeRule.onNodeWithText("편한 신발을 준비하세요").assertIsDisplayed()
        composeRule.onRoot().captureRoboImage()
    }

    @Test
    fun loadFailureOffersRetry() {
        var retries = 0
        composeRule.setContent {
            LinkItTheme {
                ScheduleTripDetailContent(
                    uiState = ScheduleUiState(showTripMapPreview = false),
                    tripPlanId = "trip",
                    detailState = TripDetailUiState(isLoading = false, errorMessage = "연결 실패"),
                    onIntent = {},
                    onRetry = { retries++ },
                )
            }
        }
        composeRule.onNodeWithText("연결 실패").assertIsDisplayed()
        composeRule.onNodeWithText("다시 시도").performClick()
        assertEquals(1, retries)
    }

    private fun content() = TripPlanContent(
        tripPlan = TripPlanDetail(
            "trip", "서울 여행", "analysis",
            listOf(item("first", "경복궁", 1), item("second", "남산", 2)), "", "",
        ),
        summary = null,
        analysis = VideoAnalysis(
            "analysis", "", true, VideoAnalysisStatus.COMPLETED,
            "서울의 문화 명소를 둘러보세요", 10000, 20000, null, true,
            listOf(VideoTimeline(80, "1:20", "", "남산 도착")), emptyList(),
        ),
        metadata = null,
    )

    private fun item(id: String, name: String, day: Int) = TripPlanItem(
        id, "source-$id", day, 1, name, PlaceCategory.ATTRACTION, "서울의 명소", "편한 신발을 준비하세요",
        Place("place-$id", name, "google-$id", "서울", 37.5, 127.0),
    )
}

package com.linkit.company.feature.map.main

import android.os.Looper
import com.linkit.company.domain.model.auth.Auth
import com.linkit.company.domain.model.common.CursorPage
import com.linkit.company.domain.model.tripplan.TripPlanDetail
import com.linkit.company.domain.model.tripplan.TripPlanItemOrder
import com.linkit.company.domain.model.tripplan.TripPlanSummary
import com.linkit.company.domain.repository.AuthRepository
import com.linkit.company.domain.repository.TripPlanRepository
import com.linkit.company.domain.usecase.DeleteTripPlanUseCase
import com.linkit.company.domain.usecase.EnsureAuthenticatedUseCase
import com.linkit.company.domain.usecase.GetSavedTripPlansForMapUseCase
import com.linkit.company.domain.usecase.RenameTripPlanUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class MapViewModelTest {
    @Test
    fun placeSelectionKeepsFocusWhileMapMovesAndCloseReturnsToSchedule() {
        val viewModel = createViewModel()
        val schedule = MapDebugMockData.schedules.first().toMapScheduleUiModel()
        val firstPlace = schedule.places.first()
        val secondPlace = schedule.places[1]

        viewModel.useDebugMapData(MapDebugMockData.schedules)
        viewModel.onIntent(MapIntent.SelectPlace(schedule.id, firstPlace.markerId))

        assertEquals(MapSelection.PLACE, viewModel.uiState.value.selection)
        assertEquals(firstPlace.markerId, viewModel.uiState.value.selectedPlaceMarkerId)

        viewModel.onIntent(
            MapIntent.CameraChanged(
                latitude = firstPlace.latitude + 0.01,
                longitude = firstPlace.longitude + 0.01,
                zoom = 14f,
            ),
        )

        assertEquals(firstPlace.markerId, viewModel.uiState.value.selectedPlaceMarkerId)

        viewModel.onIntent(MapIntent.ShowNextPlace)
        assertEquals(secondPlace.markerId, viewModel.uiState.value.selectedPlaceMarkerId)

        viewModel.onIntent(MapIntent.ClosePlace)
        assertEquals(MapSelection.SCHEDULE, viewModel.uiState.value.selection)
        assertEquals(schedule.id, viewModel.uiState.value.selectedScheduleId)
        assertNull(viewModel.uiState.value.selectedPlaceMarkerId)
    }

    @Test
    fun selectingAnotherPlaceReplacesFocusAndBoundaryNavigationDoesNothing() {
        val viewModel = createViewModel()
        val schedule = MapDebugMockData.schedules.first().toMapScheduleUiModel()
        val firstPlace = schedule.places.first()
        val lastPlace = schedule.places.last()

        viewModel.useDebugMapData(MapDebugMockData.schedules)
        viewModel.onIntent(MapIntent.SelectPlace(schedule.id, firstPlace.markerId))
        viewModel.onIntent(MapIntent.ShowPreviousPlace)
        assertEquals(firstPlace.markerId, viewModel.uiState.value.selectedPlaceMarkerId)

        viewModel.onIntent(MapIntent.SelectPlace(schedule.id, lastPlace.markerId))
        assertEquals(lastPlace.markerId, viewModel.uiState.value.selectedPlaceMarkerId)

        viewModel.onIntent(MapIntent.ShowNextPlace)
        assertEquals(lastPlace.markerId, viewModel.uiState.value.selectedPlaceMarkerId)
    }

    @Test
    fun scheduleMoreMenuTogglesBetweenSchedulesAndDismisses() {
        val viewModel = createViewModel()
        val schedules = MapDebugMockData.schedules
        val firstScheduleId = schedules.first().toMapScheduleUiModel().id
        val secondScheduleId = schedules[1].toMapScheduleUiModel().id

        viewModel.useDebugMapData(schedules)
        viewModel.onIntent(MapIntent.ToggleScheduleMenu(firstScheduleId))
        assertEquals(firstScheduleId, viewModel.uiState.value.expandedScheduleMenuId)

        viewModel.onIntent(MapIntent.ToggleScheduleMenu(secondScheduleId))
        assertEquals(secondScheduleId, viewModel.uiState.value.expandedScheduleMenuId)

        viewModel.onIntent(MapIntent.ToggleScheduleMenu(secondScheduleId))
        assertNull(viewModel.uiState.value.expandedScheduleMenuId)

        viewModel.onIntent(MapIntent.ToggleScheduleMenu(firstScheduleId))
        viewModel.onIntent(MapIntent.DismissScheduleMenu)
        assertNull(viewModel.uiState.value.expandedScheduleMenuId)
    }

    @Test
    fun renameDialogPrefillsLimitsAndRenamesSchedule() {
        val repository = RecordingTripPlanRepository()
        val viewModel = createViewModel(repository)
        val schedule = MapDebugMockData.schedules.first().toMapScheduleUiModel()

        viewModel.useDebugMapData(MapDebugMockData.schedules)
        viewModel.onIntent(MapIntent.ShowRenameScheduleDialog(schedule.id))
        assertEquals(MapScheduleDialog.RENAME, viewModel.uiState.value.scheduleDialog)
        assertEquals(schedule.title, viewModel.uiState.value.scheduleNameDraft)

        viewModel.onIntent(MapIntent.UpdateScheduleName("1234567890123456789012345"))
        assertEquals("12345678901234567890", viewModel.uiState.value.scheduleNameDraft)

        viewModel.onIntent(MapIntent.UpdateScheduleName("새 일정 이름"))
        viewModel.onIntent(MapIntent.ConfirmScheduleRename)
        shadowOf(Looper.getMainLooper()).idle()

        assertEquals(schedule.id to "새 일정 이름", repository.renamedSchedule)
        assertEquals(
            "새 일정 이름",
            viewModel.uiState.value.schedules.first { it.id == schedule.id }.title,
        )
        assertEquals(
            "일정 이름 수정이 완료되었습니다.",
            viewModel.uiState.value.scheduleActionFeedback?.message,
        )
        assertNull(viewModel.uiState.value.scheduleDialog)
    }

    @Test
    fun deleteDialogDeletesScheduleAndClearsSelection() {
        val repository = RecordingTripPlanRepository()
        val viewModel = createViewModel(repository)
        val schedule = MapDebugMockData.schedules.first().toMapScheduleUiModel()

        viewModel.useDebugMapData(MapDebugMockData.schedules)
        viewModel.onIntent(MapIntent.SelectSchedule(schedule.id))
        viewModel.onIntent(MapIntent.ShowDeleteScheduleDialog(schedule.id))
        assertEquals(MapScheduleDialog.DELETE, viewModel.uiState.value.scheduleDialog)

        viewModel.onIntent(MapIntent.ConfirmScheduleDelete)
        shadowOf(Looper.getMainLooper()).idle()

        assertEquals(schedule.id, repository.deletedScheduleId)
        assertEquals(false, viewModel.uiState.value.schedules.any { it.id == schedule.id })
        assertNull(viewModel.uiState.value.selectedScheduleId)
        assertEquals("일정이 삭제되었습니다.", viewModel.uiState.value.scheduleActionFeedback?.message)
    }

    private fun createViewModel(
        tripPlanRepository: TripPlanRepository = RecordingTripPlanRepository(),
    ): MapViewModel {
        val authRepository = EmptyAuthRepository()
        val ensureAuthenticated = EnsureAuthenticatedUseCase(authRepository)
        return MapViewModel(
            getSavedTripPlansForMap = GetSavedTripPlansForMapUseCase(
                ensureAuthenticated = ensureAuthenticated,
                tripPlanRepository = tripPlanRepository,
            ),
            ensureAuthenticated = ensureAuthenticated,
            renameTripPlan = RenameTripPlanUseCase(
                ensureAuthenticated = ensureAuthenticated,
                tripPlanRepository = tripPlanRepository,
            ),
            deleteTripPlan = DeleteTripPlanUseCase(
                ensureAuthenticated = ensureAuthenticated,
                tripPlanRepository = tripPlanRepository,
            ),
        )
    }
}

private class EmptyAuthRepository : AuthRepository {
    override suspend fun login(): Auth = Auth("member-id", "access-token")

    override suspend fun isLoggedIn(): Boolean = true

    override suspend fun logout() = Unit
}

private class RecordingTripPlanRepository : TripPlanRepository {
    var renamedSchedule: Pair<String, String>? = null
    var deletedScheduleId: String? = null

    override suspend fun getTripPlans(cursor: String?): CursorPage<TripPlanSummary> =
        CursorPage(items = emptyList(), nextCursor = null, hasNext = false)

    override suspend fun getTripPlan(tripPlanId: String): TripPlanDetail = error("Not used")

    override suspend fun updateTripPlan(
        tripPlanId: String,
        title: String?,
        items: List<TripPlanItemOrder>?,
    ): TripPlanDetail {
        val updatedTitle = requireNotNull(title)
        renamedSchedule = tripPlanId to updatedTitle
        return TripPlanDetail(
            id = tripPlanId,
            title = updatedTitle,
            videoAnalysisTaskId = "task-id",
            items = emptyList(),
            createdAt = "",
            updatedAt = "",
        )
    }

    override suspend fun deleteTripPlan(tripPlanId: String) {
        deletedScheduleId = tripPlanId
    }
}

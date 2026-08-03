package com.linkit.company.feature.map.main

import com.linkit.company.domain.model.auth.Auth
import com.linkit.company.domain.model.common.CursorPage
import com.linkit.company.domain.model.tripplan.TripPlanDetail
import com.linkit.company.domain.model.tripplan.TripPlanItemOrder
import com.linkit.company.domain.model.tripplan.TripPlanSummary
import com.linkit.company.domain.repository.AuthRepository
import com.linkit.company.domain.repository.TripPlanRepository
import com.linkit.company.domain.usecase.EnsureAuthenticatedUseCase
import com.linkit.company.domain.usecase.GetSavedTripPlansForMapUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
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

    private fun createViewModel(): MapViewModel {
        val authRepository = EmptyAuthRepository()
        val ensureAuthenticated = EnsureAuthenticatedUseCase(authRepository)
        return MapViewModel(
            getSavedTripPlansForMap = GetSavedTripPlansForMapUseCase(
                ensureAuthenticated = ensureAuthenticated,
                tripPlanRepository = EmptyTripPlanRepository,
            ),
            ensureAuthenticated = ensureAuthenticated,
        )
    }
}

private class EmptyAuthRepository : AuthRepository {
    override suspend fun login(): Auth = Auth("member-id", "access-token")

    override suspend fun isLoggedIn(): Boolean = true

    override suspend fun logout() = Unit
}

private object EmptyTripPlanRepository : TripPlanRepository {
    override suspend fun getTripPlans(cursor: String?): CursorPage<TripPlanSummary> =
        CursorPage(items = emptyList(), nextCursor = null, hasNext = false)

    override suspend fun getTripPlan(tripPlanId: String): TripPlanDetail = error("Not used")

    override suspend fun updateTripPlan(
        tripPlanId: String,
        title: String?,
        items: List<TripPlanItemOrder>?,
    ): TripPlanDetail = error("Not used")

    override suspend fun deleteTripPlan(tripPlanId: String) = Unit
}

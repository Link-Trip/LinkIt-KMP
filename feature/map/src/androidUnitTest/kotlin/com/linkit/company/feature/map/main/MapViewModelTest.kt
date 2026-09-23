package com.linkit.company.feature.map.main

import android.os.Looper
import com.linkit.company.domain.model.auth.Auth
import com.linkit.company.domain.model.common.CursorPage
import com.linkit.company.domain.model.tripplan.TripPlanDetail
import com.linkit.company.domain.model.tripplan.TripPlanItemOrder
import com.linkit.company.domain.model.onboarding.TutorialStep
import com.linkit.company.domain.model.tripplan.TripPlanSummary
import com.linkit.company.domain.repository.AuthRepository
import com.linkit.company.domain.repository.TripPlanRepository
import com.linkit.company.domain.usecase.CompleteOnboardingUseCase
import com.linkit.company.domain.usecase.DeleteTripPlanUseCase
import com.linkit.company.domain.usecase.EnsureAuthenticatedUseCase
import com.linkit.company.domain.usecase.GetSavedTripPlansForMapUseCase
import com.linkit.company.domain.usecase.RenameTripPlanUseCase
import com.linkit.company.feature.map.testing.FakeAppSettingsRepository
import com.linkit.company.feature.map.testing.FakeOnboardingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
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

    // ---- 튜토리얼 (T033) ----

    @Test
    fun tutorialStepComesFromRepositoryAndFabTapAdvancesToVideoLinkOption() {
        val onboarding = FakeOnboardingRepository(initialStep = TutorialStep.CREATE_BUTTON)
        val viewModel = createViewModel(onboarding = onboarding)
        idle()
        assertEquals(TutorialStep.CREATE_BUTTON, viewModel.uiState.value.tutorialStep)
        assertTrue(viewModel.uiState.value.isOnboardingMode)

        viewModel.onIntent(MapIntent.ToggleCreateMenu)
        idle()

        assertTrue(viewModel.uiState.value.isCreateMenuExpanded)
        assertEquals(TutorialStep.VIDEO_LINK_OPTION, viewModel.uiState.value.tutorialStep)
        assertEquals(listOf("setTutorialStep(VIDEO_LINK_OPTION)"), onboarding.events)
    }

    @Test
    fun selectingVideoLinkOptionAdvancesToCopyLinkAndClosesMenu() {
        val onboarding = FakeOnboardingRepository(initialStep = TutorialStep.VIDEO_LINK_OPTION)
        val viewModel = createViewModel(onboarding = onboarding)
        viewModel.onIntent(MapIntent.ToggleCreateMenu)
        idle()

        viewModel.onIntent(MapIntent.SelectCreateFromVideo)
        idle()

        assertFalse(viewModel.uiState.value.isCreateMenuExpanded)
        assertEquals(TutorialStep.COPY_LINK, viewModel.uiState.value.tutorialStep)
        assertEquals(listOf("setTutorialStep(COPY_LINK)"), onboarding.events)
    }

    @Test
    fun skipOnboardingRecordsCompletionAndLeavesTutorialMode() {
        val onboarding = FakeOnboardingRepository(initialStep = TutorialStep.CREATE_BUTTON)
        val viewModel = createViewModel(onboarding = onboarding)
        idle()

        viewModel.onIntent(MapIntent.SkipOnboarding)
        idle()

        assertEquals(listOf("setOnboardingCompleted(true)", "setTutorialStep(null)"), onboarding.events)
        assertTrue(onboarding.onboardingCompleted)
        assertNull(viewModel.uiState.value.tutorialStep)
        assertFalse(viewModel.uiState.value.isOnboardingMode)
    }

    @Test
    fun withoutTutorialStepMenuAndSkipBehaveAsBefore() {
        val onboarding = FakeOnboardingRepository(initialStep = null)
        val viewModel = createViewModel(onboarding = onboarding)
        idle()

        viewModel.onIntent(MapIntent.ToggleCreateMenu)
        viewModel.onIntent(MapIntent.SelectCreateFromVideo)
        viewModel.onIntent(MapIntent.SkipOnboarding)
        idle()

        assertEquals(emptyList<String>(), onboarding.events)
        assertNull(viewModel.uiState.value.tutorialStep)
    }

    // ---- 확인전/확인후 (T049) ----

    @Test
    fun uncheckedIdsMarkSchedulesAndOpeningDetailChecksThem() {
        val repository = RecordingTripPlanRepository()
        val viewModel = createViewModel(repository)
        val schedule = MapDebugMockData.schedules.first().toMapScheduleUiModel()
        viewModel.useDebugMapData(MapDebugMockData.schedules)

        repository.uncheckedIds.value = setOf(schedule.id)
        idle()
        assertTrue(viewModel.uiState.value.schedules.first { it.id == schedule.id }.isUnchecked)

        viewModel.onIntent(MapIntent.ScheduleOpened(schedule.id))
        idle()

        assertEquals(listOf(schedule.id), repository.checkedIds)
        assertFalse(viewModel.uiState.value.schedules.first { it.id == schedule.id }.isUnchecked)
    }

    @Test
    fun newViewModelKeepsHighlightFromPersistedUncheckedIds() {
        val repository = RecordingTripPlanRepository()
        val schedule = MapDebugMockData.schedules.first().toMapScheduleUiModel()
        repository.uncheckedIds.value = setOf(schedule.id)

        val viewModel = createViewModel(repository)
        viewModel.useDebugMapData(MapDebugMockData.schedules)
        idle()

        assertTrue(viewModel.uiState.value.schedules.first { it.id == schedule.id }.isUnchecked)
        assertEquals(setOf(schedule.id), viewModel.uiState.value.uncheckedScheduleIds)
    }

    @Test
    fun tutorialFinishingReloadsSchedules() {
        val onboarding = FakeOnboardingRepository(initialStep = TutorialStep.FREE)
        val repository = RecordingTripPlanRepository()
        val viewModel = createViewModel(repository, onboarding)
        idle()
        val loadsBefore = repository.listRequests

        onboarding.tutorialStep.value = null
        idle()

        assertTrue(repository.listRequests > loadsBefore)
    }

    private fun idle() = shadowOf(Looper.getMainLooper()).idle()

    private fun createViewModel(
        tripPlanRepository: RecordingTripPlanRepository = RecordingTripPlanRepository(),
        onboarding: FakeOnboardingRepository = FakeOnboardingRepository(),
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
            appSettingsRepository = FakeAppSettingsRepository(),
            onboardingRepository = onboarding,
            tripPlanRepository = tripPlanRepository,
            completeOnboarding = CompleteOnboardingUseCase(onboarding),
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
    var listRequests = 0
    val uncheckedIds = MutableStateFlow<Set<String>>(emptySet())
    val checkedIds = mutableListOf<String>()

    override suspend fun getTripPlans(cursor: String?): CursorPage<TripPlanSummary> {
        listRequests += 1
        return CursorPage(items = emptyList(), nextCursor = null, hasNext = false)
    }

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

    override fun observeUncheckedTripPlanIds(): Flow<Set<String>> = uncheckedIds

    override suspend fun markTripPlanUnchecked(tripPlanId: String) {
        uncheckedIds.value = uncheckedIds.value + tripPlanId
    }

    override suspend fun markTripPlanChecked(tripPlanId: String) {
        checkedIds += tripPlanId
        uncheckedIds.value = uncheckedIds.value - tripPlanId
    }

    override suspend fun clearUncheckedTripPlans() {
        uncheckedIds.value = emptySet()
    }
}

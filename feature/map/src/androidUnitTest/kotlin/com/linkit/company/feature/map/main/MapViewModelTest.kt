package com.linkit.company.feature.map.main

import android.os.Looper
import com.linkit.company.domain.model.auth.Auth
import com.linkit.company.domain.model.common.CursorPage
import com.linkit.company.domain.model.settings.MapDisplayType
import com.linkit.company.domain.model.tripplan.TripPlanDetail
import com.linkit.company.domain.model.tripplan.TripPlanItemOrder
import com.linkit.company.domain.model.tripplan.TripPlanSummary
import com.linkit.company.domain.repository.AuthRepository
import com.linkit.company.domain.repository.AppSettingsRepository
import com.linkit.company.domain.repository.TripPlanRepository
import com.linkit.company.domain.repository.VideoRepository
import com.linkit.company.domain.model.video.DiscoverChannel
import com.linkit.company.domain.model.video.DiscoverVideo
import com.linkit.company.domain.model.video.VideoAnalysis
import com.linkit.company.domain.model.video.VideoAnalysisStatus
import com.linkit.company.domain.model.video.VideoScheduleCreationState
import com.linkit.company.domain.model.video.YouTubeVideoMetadata
import com.linkit.company.domain.usecase.AcknowledgeVideoScheduleCreationUseCase
import com.linkit.company.domain.usecase.ObserveVideoScheduleCreationUseCase
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import com.linkit.company.domain.usecase.DeleteTripPlanUseCase
import com.linkit.company.domain.usecase.EnsureAuthenticatedUseCase
import com.linkit.company.domain.usecase.GetSavedTripPlansForMapUseCase
import com.linkit.company.domain.usecase.RenameTripPlanUseCase
import com.linkit.company.feature.map.testing.FakeAppSettingsRepository
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
    fun mapDisplayTypeStaysObservedOnceAcrossScreenResumesAndTogglePersistsIt() {
        val settings = FakeAppSettingsRepository(initial = MapDisplayType.SATELLITE)
        val viewModel = createViewModel(appSettingsRepository = settings)
        shadowOf(Looper.getMainLooper()).idle()
        assertEquals(MapDisplayType.SATELLITE, viewModel.uiState.value.mapType)

        repeat(2) {
            viewModel.onScreenResumed()
            shadowOf(Looper.getMainLooper()).idle()
            viewModel.onScreenPaused()
        }
        assertEquals(1, settings.mapDisplayType.subscriptionCount.value)

        settings.mapDisplayType.value = MapDisplayType.DEFAULT
        shadowOf(Looper.getMainLooper()).idle()
        assertEquals(MapDisplayType.DEFAULT, viewModel.uiState.value.mapType)

        viewModel.onScreenResumed()
        viewModel.onIntent(MapIntent.ToggleMapType)
        shadowOf(Looper.getMainLooper()).idle()
        assertEquals(MapDisplayType.SATELLITE, viewModel.uiState.value.mapType)
        assertEquals(listOf(MapDisplayType.SATELLITE), settings.savedMapDisplayTypes)
        assertEquals(1, settings.mapDisplayType.subscriptionCount.value)
        viewModel.onScreenPaused()
    }

    @Test
    fun returningToMapReloadsServerChangesWithoutMovingCameraOrLosingSelection() {
        val repository = RecordingTripPlanRepository().apply {
            savedSchedules = MapDebugMockData.schedules
        }
        val viewModel = createViewModel(repository)
        viewModel.onScreenResumed()
        shadowOf(Looper.getMainLooper()).idle()
        val selected = viewModel.uiState.value.schedules.first()
        val selectedPlace = selected.places.first()
        viewModel.onIntent(MapIntent.SelectPlace(selected.id, selectedPlace.markerId))
        viewModel.onIntent(MapIntent.CameraChanged(35.0, 130.0, 15f))
        viewModel.onScreenPaused()
        repository.savedSchedules = repository.savedSchedules.map {
            if (it.summary.id == selected.id) it.copy(summary = it.summary.copy(title = "서버에서 변경한 이름")) else it
        }
        viewModel.onScreenResumed()
        shadowOf(Looper.getMainLooper()).idle()

        assertEquals("서버에서 변경한 이름", viewModel.uiState.value.selectedSchedule?.title)
        assertEquals(selectedPlace.markerId, viewModel.uiState.value.selectedPlaceMarkerId)
        assertEquals(35.0, viewModel.uiState.value.cameraLatitude, 0.0)
        viewModel.onScreenPaused()
    }

    @Test
    fun completedAnalysisLinksToServerScheduleAndAcknowledgementClearsNotice() {
        val repository = RecordingTripPlanRepository().apply { savedSchedules = MapDebugMockData.schedules }
        val summary = repository.savedSchedules.first().summary
        val videos = EmptyVideoRepository().apply {
            pending.value = summary.videoAnalysisTaskId
            analysis = VideoAnalysis(
                id = summary.videoAnalysisTaskId, youtubeUrl = summary.youtubeUrl,
                isValid = true, status = VideoAnalysisStatus.COMPLETED, summary = "분석 요약",
                estimatedMinCost = 100, estimatedMaxCost = 200, costBasis = null,
                placeEnrichmentCompleted = true, timelines = emptyList(), itineraryItems = emptyList(),
            )
        }
        val viewModel = createViewModel(repository, videos)
        viewModel.onScreenResumed()
        shadowOf(Looper.getMainLooper()).idle()
        assertEquals(
            VideoScheduleCreationState.Completed(summary.videoAnalysisTaskId, summary.id, summary.title),
            viewModel.uiState.value.videoCreationState,
        )
        viewModel.onIntent(MapIntent.AcknowledgeVideoCreation(summary.videoAnalysisTaskId))
        shadowOf(Looper.getMainLooper()).idle()
        assertNull(videos.pending.value)
        assertEquals(VideoScheduleCreationState.Idle, viewModel.uiState.value.videoCreationState)
        viewModel.onScreenPaused()
    }

    @Test
    fun staleRefreshDoesNotDismissRenameDraftAndDeferredRefreshKeepsSavedTitle() {
        val repository = RecordingTripPlanRepository().apply { savedSchedules = MapDebugMockData.schedules }
        val viewModel = createViewModel(repository)
        viewModel.onScreenResumed()
        shadowOf(Looper.getMainLooper()).idle()
        val schedule = viewModel.uiState.value.schedules.first()
        val delayedRefresh = CompletableDeferred<Unit>()
        repository.nextListGate = delayedRefresh
        viewModel.onScreenPaused()
        viewModel.onScreenResumed()
        shadowOf(Looper.getMainLooper()).idle()

        viewModel.onIntent(MapIntent.ShowRenameScheduleDialog(schedule.id))
        viewModel.onIntent(MapIntent.UpdateScheduleName("저장할 이름"))
        viewModel.onIntent(MapIntent.RetryLoad)
        delayedRefresh.complete(Unit)
        shadowOf(Looper.getMainLooper()).idle()

        assertEquals(MapScheduleDialog.RENAME, viewModel.uiState.value.scheduleDialog)
        assertEquals("저장할 이름", viewModel.uiState.value.scheduleNameDraft)
        viewModel.onIntent(MapIntent.ConfirmScheduleRename)
        shadowOf(Looper.getMainLooper()).idle()

        assertEquals("저장할 이름", viewModel.uiState.value.schedules.first { it.id == schedule.id }.title)
        assertNull(viewModel.uiState.value.scheduleDialog)
        viewModel.onScreenPaused()
    }

    @Test
    fun lateRefreshCannotRestoreADeletedSchedule() {
        val repository = RecordingTripPlanRepository().apply { savedSchedules = MapDebugMockData.schedules }
        val viewModel = createViewModel(repository)
        viewModel.onScreenResumed()
        shadowOf(Looper.getMainLooper()).idle()
        val schedule = viewModel.uiState.value.schedules.first()
        val delayedRefresh = CompletableDeferred<Unit>()
        repository.nextListGate = delayedRefresh
        viewModel.onScreenPaused()
        viewModel.onScreenResumed()
        shadowOf(Looper.getMainLooper()).idle()

        viewModel.onIntent(MapIntent.ShowDeleteScheduleDialog(schedule.id))
        viewModel.onIntent(MapIntent.ConfirmScheduleDelete)
        shadowOf(Looper.getMainLooper()).idle()
        delayedRefresh.complete(Unit)
        shadowOf(Looper.getMainLooper()).idle()

        assertEquals(schedule.id, repository.deletedScheduleId)
        assertEquals(false, viewModel.uiState.value.schedules.any { it.id == schedule.id })
        assertEquals("일정이 삭제되었습니다.", viewModel.uiState.value.scheduleActionFeedback?.message)
        viewModel.onScreenPaused()
    }

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
        videoRepository: VideoRepository = EmptyVideoRepository(),
        appSettingsRepository: AppSettingsRepository = FakeAppSettingsRepository(),
    ): MapViewModel {
        val authRepository = EmptyAuthRepository()
        val ensureAuthenticated = EnsureAuthenticatedUseCase(authRepository)
        return MapViewModel(
            getSavedTripPlansForMap = GetSavedTripPlansForMapUseCase(
                ensureAuthenticated = ensureAuthenticated,
                tripPlanRepository = tripPlanRepository,
                videoRepository = videoRepository,
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
            observeVideoScheduleCreation = ObserveVideoScheduleCreationUseCase(
                ensureAuthenticated, videoRepository, tripPlanRepository,
            ),
            acknowledgeVideoScheduleCreation = AcknowledgeVideoScheduleCreationUseCase(videoRepository),
            appSettingsRepository = appSettingsRepository,
        )
    }
}

private class EmptyAuthRepository : AuthRepository {
    override suspend fun login(): Auth = Auth("member-id", "access-token")

    override suspend fun isLoggedIn(): Boolean = true

    override suspend fun logout() = Unit
}

private class RecordingTripPlanRepository : TripPlanRepository {
    var savedSchedules = emptyList<com.linkit.company.domain.model.map.TripPlanMapData>()
    var nextListGate: CompletableDeferred<Unit>? = null
    var renamedSchedule: Pair<String, String>? = null
    var deletedScheduleId: String? = null

    override suspend fun getTripPlans(cursor: String?): CursorPage<TripPlanSummary> {
        val snapshot = savedSchedules.map { it.summary }
        val gate = nextListGate
        nextListGate = null
        // 이미 전송된 응답이 취소 이후에도 늦게 도착하는 상황을 재현한다.
        if (gate != null) withContext(NonCancellable) { gate.await() }
        return CursorPage(items = snapshot, nextCursor = null, hasNext = false)
    }

    override suspend fun getTripPlan(tripPlanId: String): TripPlanDetail {
        val saved = savedSchedules.first { it.summary.id == tripPlanId }
        return TripPlanDetail(tripPlanId, saved.summary.title, saved.summary.videoAnalysisTaskId,
            saved.places.map { it.item }, saved.summary.createdAt, saved.summary.updatedAt)
    }

    override suspend fun updateTripPlan(
        tripPlanId: String,
        title: String?,
        items: List<TripPlanItemOrder>?,
    ): TripPlanDetail {
        val updatedTitle = requireNotNull(title)
        renamedSchedule = tripPlanId to updatedTitle
        savedSchedules = savedSchedules.map {
            if (it.summary.id == tripPlanId) it.copy(summary = it.summary.copy(title = updatedTitle)) else it
        }
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
        savedSchedules = savedSchedules.filterNot { it.summary.id == tripPlanId }
    }
}

private class EmptyVideoRepository : VideoRepository {
    val pending = MutableStateFlow<String?>(null)
    var analysis: VideoAnalysis? = null
    override fun observePendingVideoAnalysisTaskId() = pending
    override suspend fun savePendingVideoAnalysisTaskId(taskId: String, excludedTripPlanIds: Set<String>) { pending.value = taskId }
    override suspend fun getPendingVideoAnalysisExcludedTripPlanIds(): Set<String> = emptySet()
    override suspend fun clearPendingVideoAnalysisTaskId(expectedTaskId: String) {
        if (pending.value == expectedTaskId) pending.value = null
    }
    override suspend fun analyzeVideo(youtubeUrl: String): VideoAnalysis = error("Not used")
    override suspend fun getVideoAnalysis(videoAnalysisTaskId: String): VideoAnalysis = analysis ?: error("No analysis")
    override suspend fun getYouTubeVideoMetadata(youtubeUrl: String): YouTubeVideoMetadata = error("No metadata")
    override suspend fun getDiscoverChannels(): List<DiscoverChannel> = error("Not used")
    override suspend fun getDiscoverVideosByTheme(theme: String, cursor: String?): CursorPage<DiscoverVideo> = error("Not used")
    override suspend fun getDiscoverVideosByCountry(country: String): List<DiscoverVideo> = error("Not used")
    override suspend fun getDiscoverVideosByRegion(region: String): List<DiscoverVideo> = error("Not used")
}

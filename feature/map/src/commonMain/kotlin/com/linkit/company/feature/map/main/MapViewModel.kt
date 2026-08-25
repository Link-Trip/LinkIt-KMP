package com.linkit.company.feature.map.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.linkit.company.core.common.architecture.MviContainer
import com.linkit.company.core.common.architecture.MviContext
import com.linkit.company.domain.exception.LinkTripApiException
import com.linkit.company.domain.exception.LinkTripErrorCode
import com.linkit.company.domain.model.map.TripPlanMapData
import com.linkit.company.domain.usecase.DeleteTripPlanUseCase
import com.linkit.company.domain.usecase.EnsureAuthenticatedUseCase
import com.linkit.company.domain.usecase.GetSavedTripPlansForMapUseCase
import com.linkit.company.domain.usecase.RenameTripPlanUseCase
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.abs

@ContributesIntoMap(AppScope::class)
@ViewModelKey(MapViewModel::class)
@Inject
class MapViewModel(
    private val getSavedTripPlansForMap: GetSavedTripPlansForMapUseCase,
    private val ensureAuthenticated: EnsureAuthenticatedUseCase,
    private val renameTripPlan: RenameTripPlanUseCase,
    private val deleteTripPlan: DeleteTripPlanUseCase,
) : ViewModel() {
    private val container = MviContainer<MapIntent, MapSideEffect, MapUiState>(
        initialState = MapUiState(),
        onIntent = { handleIntent(it) },
    )
    private var loadJob: Job? = null
    private var debugMapData: List<TripPlanMapData>? = null
    private var scheduleActionFeedbackId: Int = 0

    val uiState = container.uiState

    init {
        loadSchedules()
    }

    fun onIntent(intent: MapIntent) = container.intent(intent)

    internal fun useDebugMapData(mapData: List<TripPlanMapData>) {
        debugMapData = mapData
        loadJob?.cancel()
        showSchedules(mapData)
    }

    private fun MviContext<MapUiState, MapSideEffect>.handleIntent(intent: MapIntent) {
        when (intent) {
            MapIntent.RetryLoad -> loadSchedules()
            is MapIntent.SelectSchedule -> selectSchedule(intent.scheduleId)
            is MapIntent.SelectPlace -> selectPlace(intent.scheduleId, intent.markerId)
            MapIntent.ClearSelection -> reduce {
                copy(
                    selectedScheduleId = null,
                    selectedPlaceMarkerId = null,
                    expandedScheduleMenuId = null,
                )
            }
            MapIntent.ClosePlace -> reduce { copy(selectedPlaceMarkerId = null) }
            MapIntent.ShowPreviousPlace -> selectAdjacentPlace(offset = -1)
            MapIntent.ShowNextPlace -> selectAdjacentPlace(offset = 1)
            MapIntent.ToggleCreateMenu -> reduce {
                copy(
                    isCreateMenuExpanded = !isCreateMenuExpanded,
                    expandedScheduleMenuId = null,
                )
            }
            is MapIntent.ToggleScheduleMenu -> reduce {
                if (schedules.any { it.id == intent.scheduleId }) {
                    copy(
                        expandedScheduleMenuId = intent.scheduleId
                            .takeUnless { it == expandedScheduleMenuId },
                        isCreateMenuExpanded = false,
                        expandedFilter = null,
                    )
                } else {
                    this
                }
            }
            MapIntent.DismissScheduleMenu -> reduce { copy(expandedScheduleMenuId = null) }
            is MapIntent.ShowRenameScheduleDialog -> showScheduleDialog(
                scheduleId = intent.scheduleId,
                dialog = MapScheduleDialog.RENAME,
            )
            is MapIntent.ShowDeleteScheduleDialog -> showScheduleDialog(
                scheduleId = intent.scheduleId,
                dialog = MapScheduleDialog.DELETE,
            )
            is MapIntent.UpdateScheduleName -> reduce {
                if (scheduleDialog == MapScheduleDialog.RENAME && !isScheduleActionInProgress) {
                    copy(scheduleNameDraft = intent.value.take(MaxScheduleNameLength))
                } else {
                    this
                }
            }
            MapIntent.ConfirmScheduleRename -> confirmScheduleRename()
            MapIntent.ConfirmScheduleDelete -> confirmScheduleDelete()
            MapIntent.DismissScheduleDialog -> dismissScheduleDialog()
            MapIntent.DismissScheduleActionFeedback -> reduce {
                copy(scheduleActionFeedback = null)
            }
            MapIntent.ShowComingSoonDialog -> reduce {
                copy(
                    isComingSoonDialogVisible = true,
                    expandedScheduleMenuId = null,
                )
            }
            MapIntent.DismissComingSoonDialog -> reduce {
                copy(isComingSoonDialogVisible = false)
            }
            MapIntent.ToggleMapType -> reduce {
                copy(mapType = if (mapType == MapType.DEFAULT) MapType.SATELLITE else MapType.DEFAULT)
            }
            is MapIntent.ToggleFilter -> reduce {
                copy(
                    expandedFilter = intent.filter.takeUnless { it == expandedFilter },
                    expandedScheduleMenuId = null,
                )
            }
            is MapIntent.SelectRegion -> reduce {
                copy(selectedRegion = intent.region, expandedFilter = null)
            }
            is MapIntent.SelectStyle -> reduce {
                copy(selectedStyle = intent.style, expandedFilter = null)
            }
            is MapIntent.SelectDuration -> reduce {
                copy(durationFilter = intent.duration, expandedFilter = null)
            }
            MapIntent.RequestCurrentLocation -> reduce {
                copy(
                    locationRequestToken = locationRequestToken.nextRequestToken(),
                    locationMessage = null,
                )
            }
            is MapIntent.CurrentLocationResolved -> {
                if (intent.latitude.isValidLatitude() && intent.longitude.isValidLongitude()) {
                    reduce {
                        copy(
                            currentLocationLatitude = intent.latitude,
                            currentLocationLongitude = intent.longitude,
                            focusCurrentLocationRequest = focusCurrentLocationRequest.nextRequestToken(),
                            locationMessage = null,
                        )
                    }
                }
            }
            is MapIntent.CurrentLocationUnavailable -> reduce {
                copy(locationMessage = intent.message)
            }
            is MapIntent.CameraChanged -> {
                if (intent.latitude.isValidLatitude() && intent.longitude.isValidLongitude()) {
                    reduce {
                        copy(
                            cameraLatitude = intent.latitude,
                            cameraLongitude = intent.longitude,
                            cameraZoom = intent.zoom.coerceIn(MinZoom, MaxZoom),
                        )
                    }
                }
            }
            is MapIntent.MapCenterLocationResolved -> reduce {
                if (matchesMapCenter(intent.latitude, intent.longitude)) {
                    copy(mapCenterLocationLabel = intent.label)
                } else {
                    this
                }
            }
        }
    }

    private fun MviContext<MapUiState, MapSideEffect>.showScheduleDialog(
        scheduleId: String,
        dialog: MapScheduleDialog,
    ) {
        val schedule = currentState.schedules.firstOrNull { it.id == scheduleId } ?: return
        reduce {
            copy(
                expandedScheduleMenuId = null,
                scheduleDialog = dialog,
                scheduleDialogScheduleId = scheduleId,
                scheduleNameDraft = schedule.title,
                isScheduleActionInProgress = false,
                scheduleActionFeedback = null,
            )
        }
    }

    private fun MviContext<MapUiState, MapSideEffect>.dismissScheduleDialog() {
        if (currentState.isScheduleActionInProgress) return
        reduce {
            copy(
                scheduleDialog = null,
                scheduleDialogScheduleId = null,
                scheduleNameDraft = "",
            )
        }
    }

    private fun MviContext<MapUiState, MapSideEffect>.confirmScheduleRename() {
        val scheduleId = currentState.scheduleDialogScheduleId ?: return
        val title = currentState.scheduleNameDraft
        if (
            currentState.scheduleDialog != MapScheduleDialog.RENAME ||
            currentState.isScheduleActionInProgress ||
            title.isBlank()
        ) {
            return
        }

        reduce { copy(isScheduleActionInProgress = true) }
        viewModelScope.launch {
            try {
                val updated = runWithAuthRetry { renameTripPlan(scheduleId, title) }
                container.mviContext.reduce {
                    copy(
                        schedules = schedules.map { schedule ->
                            if (schedule.id == scheduleId) schedule.copy(title = updated.title) else schedule
                        },
                        scheduleDialog = null,
                        scheduleDialogScheduleId = null,
                        scheduleNameDraft = "",
                        isScheduleActionInProgress = false,
                        scheduleActionFeedback = nextScheduleActionFeedback(
                            message = "일정 이름 수정이 완료되었습니다.",
                            type = MapScheduleActionFeedbackType.SUCCESS,
                        ),
                    )
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Throwable) {
                showScheduleActionError(error, fallback = "일정 이름을 수정하지 못했어요.")
            }
        }
    }

    private fun MviContext<MapUiState, MapSideEffect>.confirmScheduleDelete() {
        val scheduleId = currentState.scheduleDialogScheduleId ?: return
        if (
            currentState.scheduleDialog != MapScheduleDialog.DELETE ||
            currentState.isScheduleActionInProgress
        ) {
            return
        }

        reduce { copy(isScheduleActionInProgress = true) }
        viewModelScope.launch {
            try {
                runWithAuthRetry { deleteTripPlan(scheduleId) }
                container.mviContext.reduce {
                    val remainingSchedules = schedules.filterNot { it.id == scheduleId }
                    val deletedSelectedSchedule = selectedScheduleId == scheduleId
                    copy(
                        loadState = if (remainingSchedules.isEmpty()) {
                            MapLoadState.EMPTY
                        } else {
                            MapLoadState.CONTENT
                        },
                        schedules = remainingSchedules,
                        selectedScheduleId = selectedScheduleId.takeUnless { deletedSelectedSchedule },
                        selectedPlaceMarkerId = selectedPlaceMarkerId.takeUnless { deletedSelectedSchedule },
                        scheduleDialog = null,
                        scheduleDialogScheduleId = null,
                        scheduleNameDraft = "",
                        isScheduleActionInProgress = false,
                        scheduleActionFeedback = nextScheduleActionFeedback(
                            message = "일정이 삭제되었습니다.",
                            type = MapScheduleActionFeedbackType.SUCCESS,
                        ),
                    )
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Throwable) {
                showScheduleActionError(error, fallback = "일정을 삭제하지 못했어요.")
            }
        }
    }

    private fun showScheduleActionError(error: Throwable, fallback: String) {
        val message = (error as? LinkTripApiException)
            ?.message
            ?.takeIf(String::isNotBlank)
            ?: fallback
        container.mviContext.reduce {
            copy(
                scheduleDialog = null,
                scheduleDialogScheduleId = null,
                scheduleNameDraft = "",
                isScheduleActionInProgress = false,
                scheduleActionFeedback = nextScheduleActionFeedback(
                    message = message,
                    type = MapScheduleActionFeedbackType.ERROR,
                ),
            )
        }
    }

    private fun nextScheduleActionFeedback(
        message: String,
        type: MapScheduleActionFeedbackType,
    ): MapScheduleActionFeedback {
        scheduleActionFeedbackId = scheduleActionFeedbackId.nextRequestToken()
        return MapScheduleActionFeedback(
            id = scheduleActionFeedbackId,
            message = message,
            type = type,
        )
    }

    private fun MviContext<MapUiState, MapSideEffect>.selectSchedule(scheduleId: String) {
        if (currentState.schedules.none { it.id == scheduleId }) return
        reduce {
            copy(
                selectedScheduleId = scheduleId,
                selectedPlaceMarkerId = null,
                isCreateMenuExpanded = false,
                expandedScheduleMenuId = null,
                expandedFilter = null,
            )
        }
    }

    private fun MviContext<MapUiState, MapSideEffect>.selectPlace(
        scheduleId: String,
        markerId: String,
    ) {
        val schedule = currentState.schedules.firstOrNull { it.id == scheduleId } ?: return
        if (schedule.places.none { it.markerId == markerId }) return
        reduce {
            copy(
                selectedScheduleId = scheduleId,
                selectedPlaceMarkerId = markerId,
                isCreateMenuExpanded = false,
                expandedScheduleMenuId = null,
                expandedFilter = null,
            )
        }
    }

    private fun MviContext<MapUiState, MapSideEffect>.selectAdjacentPlace(offset: Int) {
        val schedule = currentState.selectedSchedule ?: return
        val selectedIndex = schedule.places.indexOfFirst {
            it.markerId == currentState.selectedPlaceMarkerId
        }
        if (selectedIndex < 0) return
        val target = schedule.places.getOrNull(selectedIndex + offset) ?: return
        reduce { copy(selectedPlaceMarkerId = target.markerId) }
    }

    private fun loadSchedules() {
        if (loadJob?.isActive == true) return
        debugMapData?.let { mapData ->
            showSchedules(mapData)
            return
        }
        loadJob = viewModelScope.launch {
            container.mviContext.reduce {
                copy(loadState = MapLoadState.LOADING, errorMessage = null)
            }
            val result = runCatching { loadWithAuthRetry() }
            if (result.exceptionOrNull() is CancellationException || debugMapData != null) {
                return@launch
            }
            result
                .onSuccess(::showSchedules)
                .onFailure(::showLoadError)
        }
    }

    private suspend fun loadWithAuthRetry(): List<TripPlanMapData> = runWithAuthRetry {
        getSavedTripPlansForMap()
    }

    private suspend fun <T> runWithAuthRetry(action: suspend () -> T): T = try {
        action()
    } catch (error: LinkTripApiException) {
        val isUnauthorized = error.httpStatus == UnauthorizedStatus ||
            error.errorCode == LinkTripErrorCode.UNAUTHORIZED_AUTHENTICATION_FAILED
        if (!isUnauthorized) throw error

        ensureAuthenticated(forceRefresh = true)
        action()
    }

    private fun showSchedules(mapData: List<TripPlanMapData>) {
        val schedules = mapData.map(TripPlanMapData::toMapScheduleUiModel)
        val firstCenter = schedules.firstOrNull {
            it.centerLatitude != null && it.centerLongitude != null
        }
        container.mviContext.reduce {
            val nextLatitude = firstCenter?.centerLatitude ?: cameraLatitude
            val nextLongitude = firstCenter?.centerLongitude ?: cameraLongitude
            copy(
                loadState = if (schedules.isEmpty()) MapLoadState.EMPTY else MapLoadState.CONTENT,
                schedules = schedules,
                errorMessage = null,
                selectedScheduleId = selectedScheduleId?.takeIf { selectedId ->
                    schedules.any { it.id == selectedId }
                },
                selectedPlaceMarkerId = null,
                expandedScheduleMenuId = null,
                scheduleDialog = null,
                scheduleDialogScheduleId = null,
                scheduleNameDraft = "",
                isScheduleActionInProgress = false,
                cameraLatitude = nextLatitude,
                cameraLongitude = nextLongitude,
            )
        }
    }

    private fun showLoadError(error: Throwable) {
        val message = when (error) {
            is LinkTripApiException -> error.message.takeIf(String::isNotBlank)
            else -> null
        } ?: "저장한 일정을 불러오지 못했어요. 네트워크 연결을 확인해 주세요."

        container.mviContext.reduce {
            copy(
                loadState = MapLoadState.ERROR,
                schedules = emptyList(),
                selectedScheduleId = null,
                selectedPlaceMarkerId = null,
                errorMessage = message,
            )
        }
    }

    override fun onCleared() {
        loadJob?.cancel()
        container.close()
        super.onCleared()
    }

    private companion object {
        const val UnauthorizedStatus = 401
        const val MaxScheduleNameLength = 20
        const val MinZoom = 3f
        const val MaxZoom = 21f
    }
}

private const val MapCenterCoordinateTolerance = 0.000_001

private fun MapUiState.matchesMapCenter(latitude: Double, longitude: Double): Boolean =
    abs(cameraLatitude - latitude) <= MapCenterCoordinateTolerance &&
        abs(cameraLongitude - longitude) <= MapCenterCoordinateTolerance

private fun Int.nextRequestToken(): Int = if (this == Int.MAX_VALUE) 1 else this + 1

private fun Double.isValidLatitude(): Boolean = isFinite() && this in -90.0..90.0

private fun Double.isValidLongitude(): Boolean = isFinite() && this in -180.0..180.0

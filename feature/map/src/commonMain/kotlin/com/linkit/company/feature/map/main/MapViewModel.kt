package com.linkit.company.feature.map.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.linkit.company.core.common.architecture.MviContainer
import com.linkit.company.core.common.architecture.MviContext
import com.linkit.company.domain.exception.LinkTripApiException
import com.linkit.company.domain.exception.LinkTripErrorCode
import com.linkit.company.domain.model.map.TripPlanMapData
import com.linkit.company.domain.usecase.EnsureAuthenticatedUseCase
import com.linkit.company.domain.usecase.GetSavedTripPlansForMapUseCase
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@ContributesIntoMap(AppScope::class)
@ViewModelKey(MapViewModel::class)
@Inject
class MapViewModel(
    private val getSavedTripPlansForMap: GetSavedTripPlansForMapUseCase,
    private val ensureAuthenticated: EnsureAuthenticatedUseCase,
) : ViewModel() {
    private val container = MviContainer<MapIntent, MapSideEffect, MapUiState>(
        initialState = MapUiState(),
        onIntent = { handleIntent(it) },
    )
    private var loadJob: Job? = null

    val uiState = container.uiState

    init {
        loadSchedules()
    }

    fun onIntent(intent: MapIntent) = container.intent(intent)

    private fun MviContext<MapUiState, MapSideEffect>.handleIntent(intent: MapIntent) {
        when (intent) {
            MapIntent.RetryLoad -> loadSchedules()
            is MapIntent.SelectSchedule -> selectSchedule(intent.scheduleId)
            is MapIntent.SelectPlace -> selectPlace(intent.scheduleId, intent.markerId)
            MapIntent.ClearSelection -> reduce {
                copy(selectedScheduleId = null, selectedPlaceMarkerId = null)
            }
            MapIntent.ClosePlace -> reduce { copy(selectedPlaceMarkerId = null) }
            MapIntent.ShowPreviousPlace -> selectAdjacentPlace(offset = -1)
            MapIntent.ShowNextPlace -> selectAdjacentPlace(offset = 1)
            MapIntent.ToggleCreateMenu -> reduce {
                copy(isCreateMenuExpanded = !isCreateMenuExpanded)
            }
            MapIntent.ToggleMapType -> reduce {
                copy(mapType = if (mapType == MapType.DEFAULT) MapType.SATELLITE else MapType.DEFAULT)
            }
            is MapIntent.ToggleFilter -> reduce {
                copy(expandedFilter = intent.filter.takeUnless { it == expandedFilter })
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
        }
    }

    private fun MviContext<MapUiState, MapSideEffect>.selectSchedule(scheduleId: String) {
        if (currentState.schedules.none { it.id == scheduleId }) return
        reduce {
            copy(
                selectedScheduleId = scheduleId,
                selectedPlaceMarkerId = null,
                isCreateMenuExpanded = false,
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
        loadJob = viewModelScope.launch {
            container.mviContext.reduce {
                copy(loadState = MapLoadState.LOADING, errorMessage = null)
            }
            runCatching { loadWithAuthRetry() }
                .onSuccess { mapData -> showSchedules(mapData) }
                .onFailure { error -> showLoadError(error) }
        }
    }

    private suspend fun loadWithAuthRetry(): List<TripPlanMapData> = try {
        getSavedTripPlansForMap()
    } catch (error: LinkTripApiException) {
        val isUnauthorized = error.httpStatus == UnauthorizedStatus ||
            error.errorCode == LinkTripErrorCode.UNAUTHORIZED_AUTHENTICATION_FAILED
        if (!isUnauthorized) throw error

        ensureAuthenticated(forceRefresh = true)
        getSavedTripPlansForMap()
    }

    private fun showSchedules(mapData: List<TripPlanMapData>) {
        val schedules = mapData.map(TripPlanMapData::toMapScheduleUiModel)
        val firstCenter = schedules.firstOrNull {
            it.centerLatitude != null && it.centerLongitude != null
        }
        container.mviContext.reduce {
            copy(
                loadState = if (schedules.isEmpty()) MapLoadState.EMPTY else MapLoadState.CONTENT,
                schedules = schedules,
                errorMessage = null,
                selectedScheduleId = selectedScheduleId?.takeIf { selectedId ->
                    schedules.any { it.id == selectedId }
                },
                selectedPlaceMarkerId = null,
                cameraLatitude = firstCenter?.centerLatitude ?: cameraLatitude,
                cameraLongitude = firstCenter?.centerLongitude ?: cameraLongitude,
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
        const val MinZoom = 3f
        const val MaxZoom = 21f
    }
}

private fun Int.nextRequestToken(): Int = if (this == Int.MAX_VALUE) 1 else this + 1

private fun Double.isValidLatitude(): Boolean = isFinite() && this in -90.0..90.0

private fun Double.isValidLongitude(): Boolean = isFinite() && this in -180.0..180.0

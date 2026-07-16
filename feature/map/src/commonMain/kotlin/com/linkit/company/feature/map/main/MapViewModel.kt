package com.linkit.company.feature.map.main

import androidx.lifecycle.ViewModel
import com.linkit.company.core.common.architecture.MviContainer
import com.linkit.company.core.common.architecture.MviContext
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey

@ContributesIntoMap(AppScope::class)
@ViewModelKey(MapViewModel::class)
@Inject
class MapViewModel : ViewModel() {
    private val container = MviContainer<MapIntent, MapSideEffect, MapUiState>(
        initialState = MapUiState(),
        onIntent = { handleIntent(it) },
    )

    val uiState = container.uiState

    fun onIntent(intent: MapIntent) = container.intent(intent)

    private fun MviContext<MapUiState, MapSideEffect>.handleIntent(intent: MapIntent) {
        when (intent) {
            MapIntent.SelectSchedule -> reduce { copy(selection = MapSelection.SCHEDULE) }
            MapIntent.SelectPlace -> reduce { copy(selection = MapSelection.PLACE) }
            MapIntent.ClearSelection -> reduce { copy(selection = MapSelection.NONE) }
            MapIntent.ClosePlace -> reduce { copy(selection = MapSelection.SCHEDULE) }
            MapIntent.ShowPreviousPlace -> reduce {
                copy(selectedPlaceIndex = (selectedPlaceIndex - 1).coerceAtLeast(0))
            }
            MapIntent.ShowNextPlace -> reduce {
                copy(selectedPlaceIndex = (selectedPlaceIndex + 1).coerceAtMost(2))
            }
            MapIntent.ToggleCreateMenu -> reduce {
                copy(isCreateMenuExpanded = !isCreateMenuExpanded)
            }
            MapIntent.ToggleMapType -> reduce {
                copy(mapType = if (mapType == MapType.DEFAULT) MapType.SATELLITE else MapType.DEFAULT)
            }
        }
    }

    override fun onCleared() {
        container.close()
        super.onCleared()
    }
}

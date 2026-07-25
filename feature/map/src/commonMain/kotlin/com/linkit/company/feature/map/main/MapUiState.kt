package com.linkit.company.feature.map.main

import com.linkit.company.core.common.architecture.contract.UiState

enum class MapSelection {
    NONE,
    SCHEDULE,
    PLACE,
}

enum class MapType {
    DEFAULT,
    SATELLITE,
}

enum class MapLoadState {
    LOADING,
    CONTENT,
    EMPTY,
    ERROR,
}

enum class MapFilterType {
    REGION,
    STYLE,
    DURATION,
}

enum class MapDurationFilter(val label: String) {
    ALL("전체"),
    SHORT("3일 이하"),
    MEDIUM("4~6일"),
    LONG("7일 이상"),
    ;

    fun accepts(days: Int): Boolean = when (this) {
        ALL -> true
        SHORT -> days <= 3
        MEDIUM -> days in 4..6
        LONG -> days >= 7
    }
}

data class MapScheduleUiModel(
    val id: String,
    val title: String,
    val youtubeUrl: String,
    val itemCount: Int,
    val nights: Int,
    val days: Int,
    val hashtags: List<String>,
    val regionLabel: String,
    val centerLatitude: Double?,
    val centerLongitude: Double?,
    val places: List<MapPlaceUiModel>,
)

data class MapPlaceUiModel(
    val markerId: String,
    val scheduleId: String,
    val placeId: String,
    val itemId: String,
    val day: Int,
    val itemOrder: Int,
    val name: String,
    val categoryLabel: String,
    val description: String,
    val tips: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
)

data class MapUiState(
    val loadState: MapLoadState = MapLoadState.LOADING,
    val schedules: List<MapScheduleUiModel> = emptyList(),
    val errorMessage: String? = null,
    val selectedScheduleId: String? = null,
    val selectedPlaceMarkerId: String? = null,
    val isCreateMenuExpanded: Boolean = false,
    val mapType: MapType = MapType.DEFAULT,
    val expandedFilter: MapFilterType? = null,
    val selectedRegion: String? = null,
    val selectedStyle: String? = null,
    val durationFilter: MapDurationFilter = MapDurationFilter.ALL,
    val cameraLatitude: Double = DefaultLatitude,
    val cameraLongitude: Double = DefaultLongitude,
    val cameraZoom: Float = DefaultZoom,
    val locationRequestToken: Int = 0,
    val currentLocationLatitude: Double? = null,
    val currentLocationLongitude: Double? = null,
    val focusCurrentLocationRequest: Int = 0,
    val locationMessage: String? = null,
) : UiState {
    val selection: MapSelection
        get() = when {
            selectedPlaceMarkerId != null -> MapSelection.PLACE
            selectedScheduleId != null -> MapSelection.SCHEDULE
            else -> MapSelection.NONE
        }

    val selectedSchedule: MapScheduleUiModel?
        get() = schedules.firstOrNull { it.id == selectedScheduleId }

    val selectedPlace: MapPlaceUiModel?
        get() = selectedSchedule
            ?.places
            ?.firstOrNull { it.markerId == selectedPlaceMarkerId }

    val selectedPlaceIndex: Int
        get() = selectedSchedule
            ?.places
            ?.indexOfFirst { it.markerId == selectedPlaceMarkerId }
            ?.coerceAtLeast(0)
            ?: 0

    val filteredSchedules: List<MapScheduleUiModel>
        get() = schedules.filter { schedule ->
            (selectedRegion == null || schedule.regionLabel == selectedRegion) &&
                (selectedStyle == null || selectedStyle in schedule.hashtags) &&
                durationFilter.accepts(schedule.days)
        }

    val availableRegions: List<String>
        get() = schedules.map(MapScheduleUiModel::regionLabel).filter(String::isNotBlank).distinct()

    val availableStyles: List<String>
        get() = schedules.flatMap(MapScheduleUiModel::hashtags).filter(String::isNotBlank).distinct()

    companion object {
        const val DefaultLatitude = 37.5665
        const val DefaultLongitude = 126.9780
        const val DefaultZoom = 12f
    }
}

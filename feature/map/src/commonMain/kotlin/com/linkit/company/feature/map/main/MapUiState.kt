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

enum class MapTravelStyleFilter(val label: String) {
    FOOD("맛집 중심"),
    SHOPPING("쇼핑 중심"),
    LANDMARK("명소 탐방 중심"),
    NATURE("자연·풍경 위주"),
    CULTURE("문화·역사 탐방"),
    ACTIVITY("액티비티"),
    HEALING("힐링"),
    ;

    fun accepts(hashtags: List<String>): Boolean = hashtags.any { hashtag ->
        hashtag.removePrefix("#").trim() == label
    }
}

enum class MapDurationFilter(val label: String) {
    ALL("전체 기간"),
    DAY_TRIP("당일치기"),
    ONE_NIGHT_TWO_DAYS("1박 2일"),
    TWO_NIGHTS_THREE_DAYS("2박 3일"),
    THREE_NIGHTS_FOUR_DAYS("3박 4일"),
    FOUR_NIGHTS_FIVE_DAYS("4박 5일"),
    FIVE_NIGHTS_OR_MORE("5일 이상~"),
    ;

    fun accepts(days: Int): Boolean = when (this) {
        ALL -> true
        DAY_TRIP -> days == 1
        ONE_NIGHT_TWO_DAYS -> days == 2
        TWO_NIGHTS_THREE_DAYS -> days == 3
        THREE_NIGHTS_FOUR_DAYS -> days == 4
        FOUR_NIGHTS_FIVE_DAYS -> days == 5
        FIVE_NIGHTS_OR_MORE -> days >= 6
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
    val regions: List<String> = listOf(regionLabel),
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
    val isComingSoonDialogVisible: Boolean = false,
    val mapType: MapType = MapType.DEFAULT,
    val expandedFilter: MapFilterType? = null,
    val selectedRegion: String? = null,
    val selectedStyle: MapTravelStyleFilter? = null,
    val durationFilter: MapDurationFilter = MapDurationFilter.ALL,
    val cameraLatitude: Double = DefaultLatitude,
    val cameraLongitude: Double = DefaultLongitude,
    val cameraZoom: Float = DefaultZoom,
    val mapCenterLocationLabel: String? = null,
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
            (selectedRegion == null || selectedRegion in schedule.regions) &&
                (selectedStyle == null || selectedStyle.accepts(schedule.hashtags)) &&
                durationFilter.accepts(schedule.days)
        }

    val availableRegions: List<String>
        get() = schedules
            .flatMap(MapScheduleUiModel::regions)
            .filter(String::isNotBlank)
            .distinct()
            .sortedDescending()

    companion object {
        const val DefaultLatitude = 37.5665
        const val DefaultLongitude = 126.9780
        const val DefaultZoom = 12f
    }
}

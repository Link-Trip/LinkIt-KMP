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

data class MapUiState(
    val selection: MapSelection = MapSelection.NONE,
    val selectedPlaceIndex: Int = 0,
    val isCreateMenuExpanded: Boolean = false,
    val mapType: MapType = MapType.DEFAULT,
) : UiState

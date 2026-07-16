package com.linkit.company.feature.schedule

import com.linkit.company.core.common.architecture.contract.UiState

enum class TripDetailTab {
    ITINERARY,
    SUMMARY,
}

data class ScheduleUiState(
    val videoLink: String = "",
    val copiedRecommendedIndex: Int? = null,
    val showInvalidLinkMessage: Boolean = false,
    val tripDetailTab: TripDetailTab = TripDetailTab.ITINERARY,
    val showTripMapPreview: Boolean = true,
) : UiState {
    val canCreate: Boolean get() = videoLink.isNotBlank()
}

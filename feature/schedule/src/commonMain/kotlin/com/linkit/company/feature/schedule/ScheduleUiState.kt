package com.linkit.company.feature.schedule

import com.linkit.company.core.common.architecture.contract.UiState

enum class TripDetailTab {
    ITINERARY,
    SUMMARY,
}

enum class VideoLinkError {
    WRONG_FORMAT,
    INVALID_LINK,
}

data class ExistingScheduleUiModel(
    val tripPlanId: String,
    val title: String,
)

data class ScheduleUiState(
    val videoLink: String = "",
    val copiedRecommendedIndex: Int? = null,
    val videoLinkError: VideoLinkError? = null,
    val isSubmittingVideoLink: Boolean = false,
    val existingSchedule: ExistingScheduleUiModel? = null,
    val tripDetailTab: TripDetailTab = TripDetailTab.ITINERARY,
    val showTripMapPreview: Boolean = true,
) : UiState {
    val canCreate: Boolean
        get() = videoLink.isNotBlank() && videoLinkError == null && !isSubmittingVideoLink
}

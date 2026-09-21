package com.linkit.company.feature.schedule

import com.linkit.company.core.common.architecture.contract.UiState
import com.linkit.company.domain.model.video.DiscoverVideo

enum class TripDetailTab {
    ITINERARY,
    SUMMARY,
}

enum class VideoLinkError {
    WRONG_FORMAT,
    INVALID_LINK,
    ALREADY_IN_PROGRESS,
}

enum class TripDetailDialog {
    RENAME,
    DELETE,
}

data class ExistingScheduleUiModel(
    val tripPlanId: String,
    val title: String,
)

data class ScheduleUiState(
    val recommendedVideos: List<DiscoverVideo> = emptyList(),
    val isLoadingRecommendedVideos: Boolean = true,
    val recommendedVideosError: String? = null,
    val areRecommendedVideosExpanded: Boolean = false,
    val videoLink: String = "",
    val videoLinkError: VideoLinkError? = null,
    val isSubmittingVideoLink: Boolean = false,
    val existingSchedule: ExistingScheduleUiModel? = null,
    val tripDetailTab: TripDetailTab = TripDetailTab.ITINERARY,
    val showTripMapPreview: Boolean = true,
    val tripDetailMenuExpanded: Boolean = false,
    val tripDetailDialog: TripDetailDialog? = null,
    val tripDetailActionTripPlanId: String? = null,
    val tripDetailNameDraft: String = "",
    val renamedTripPlanId: String? = null,
    val renamedTripPlanTitle: String? = null,
    val isTripDetailActionInProgress: Boolean = false,
    val tripDetailActionError: String? = null,
) : UiState {
    val canCreate: Boolean
        get() = videoLink.isNotBlank() && videoLinkError == null && !isSubmittingVideoLink
}

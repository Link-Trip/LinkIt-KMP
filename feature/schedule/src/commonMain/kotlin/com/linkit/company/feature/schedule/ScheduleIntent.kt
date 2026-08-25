package com.linkit.company.feature.schedule

import com.linkit.company.core.common.architecture.contract.Intent

sealed interface ScheduleIntent : Intent {
    data class UpdateVideoLink(val link: String) : ScheduleIntent
    data object SubmitVideoLink : ScheduleIntent
    data object CreateDuplicateVideoSchedule : ScheduleIntent
    data object OpenExistingSchedule : ScheduleIntent
    data object DismissExistingSchedule : ScheduleIntent
    data class SelectTripDetailTab(val tab: TripDetailTab) : ScheduleIntent
    data object ToggleTripDetailMenu : ScheduleIntent
    data object DismissTripDetailMenu : ScheduleIntent
    data class ShowTripDetailRenameDialog(
        val tripPlanId: String,
        val currentTitle: String,
    ) : ScheduleIntent
    data class ShowTripDetailDeleteDialog(val tripPlanId: String) : ScheduleIntent
    data class UpdateTripDetailName(val value: String) : ScheduleIntent
    data object ConfirmTripDetailRename : ScheduleIntent
    data object ConfirmTripDetailDelete : ScheduleIntent
    data object DismissTripDetailDialog : ScheduleIntent
}

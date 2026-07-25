package com.linkit.company.feature.schedule

import com.linkit.company.core.common.architecture.contract.Intent

sealed interface ScheduleIntent : Intent {
    data class UpdateVideoLink(val link: String) : ScheduleIntent
    data object SubmitVideoLink : ScheduleIntent
    data object CreateDuplicateVideoSchedule : ScheduleIntent
    data object OpenExistingSchedule : ScheduleIntent
    data object DismissExistingSchedule : ScheduleIntent
    data class SelectTripDetailTab(val tab: TripDetailTab) : ScheduleIntent
}

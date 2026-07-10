package com.linkit.company.feature.schedule

import com.linkit.company.core.common.architecture.contract.Intent

sealed interface ScheduleIntent : Intent {
    data class UpdateVideoLink(val link: String) : ScheduleIntent
    data class CopyRecommendedLink(val index: Int) : ScheduleIntent
    data object SubmitVideoLink : ScheduleIntent
    data object DismissInvalidLink : ScheduleIntent
}

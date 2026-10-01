package com.linkit.company.domain.model.video

sealed interface VideoScheduleCreationState {
    data object Idle : VideoScheduleCreationState

    data class InProgress(val taskId: String) : VideoScheduleCreationState

    data class Completed(
        val taskId: String,
        val tripPlanId: String,
        val title: String,
    ) : VideoScheduleCreationState

    data class Failed(val taskId: String, val message: String) : VideoScheduleCreationState

    /** 일시 오류나 폴링 한도 도달. 저장 ID를 보존하므로 재구독하면 다시 확인한다. */
    data class Error(val taskId: String?, val message: String) : VideoScheduleCreationState
}

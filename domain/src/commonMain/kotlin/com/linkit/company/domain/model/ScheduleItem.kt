package com.linkit.company.domain.model

/** 영상 분석으로 추출된 일차별 일정 아이템 */
data class ScheduleItem(
    val id: String,
    val day: Int,
    val order: Int,
    val category: PlaceCategory,
    val name: String,
    val description: String,
    val tips: String,
    /** placeStatus가 FOUND가 아니면 null일 수 있다 */
    val place: Place?,
    val placeStatus: PlaceStatus,
)

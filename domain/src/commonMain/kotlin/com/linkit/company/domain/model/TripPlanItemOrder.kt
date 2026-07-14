package com.linkit.company.domain.model

/** 여행 계획 수정 시 아이템의 일차(day)·순서(itemOrder) 재배치 명령 */
data class TripPlanItemOrder(
    val tripPlanItemId: String,
    val day: Int,
    val itemOrder: Int,
)

package com.linkit.company.domain.model

data class TripPlanItem(
    val id: String,
    val travelItineraryItemId: String,
    val day: Int,
    val itemOrder: Int,
    val name: String,
    val category: PlaceCategory,
    val description: String,
    val tips: String,
    /** 장소 정보가 연결되지 않은 아이템(이동 구간 등)은 null */
    val place: Place?,
)

package com.linkit.company.data.dto.tripplan

import kotlinx.serialization.Serializable

@Serializable
data class TripPlanItemDetailResponse(
    val id: String,
    val travelItineraryItemId: String,
    val day: Int,
    val itemOrder: Int,
    val name: String,
    val category: String,
    val description: String? = null,
    val tips: String? = null,
    val place: PlaceResponse? = null,
)

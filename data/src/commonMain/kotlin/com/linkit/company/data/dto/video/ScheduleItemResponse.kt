package com.linkit.company.data.dto.video

import com.linkit.company.data.dto.tripplan.PlaceResponse
import kotlinx.serialization.Serializable

@Serializable
data class ScheduleItemResponse(
    val id: String,
    val day: Int,
    val order: Int,
    val category: String,
    val name: String,
    val description: String? = null,
    val tips: String? = null,
    val place: PlaceResponse? = null,
    val placeStatus: String,
)

package com.linkit.company.data.dto.tripplan

import kotlinx.serialization.Serializable

@Serializable
data class TripPlanDetailResponse(
    val id: String,
    val title: String,
    val videoAnalysisTaskId: String,
    val items: List<TripPlanItemDetailResponse> = emptyList(),
    val createdAt: String,
    val updatedAt: String,
)

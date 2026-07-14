package com.linkit.company.data.dto.tripplan

import kotlinx.serialization.Serializable

@Serializable
data class TripPlanSummaryResponse(
    val id: String,
    val title: String,
    val videoAnalysisTaskId: String,
    val youtubeUrl: String,
    val itemCount: Int,
    val nights: Int,
    val days: Int,
    val hashtags: List<String> = emptyList(),
    val createdAt: String,
    val updatedAt: String,
)

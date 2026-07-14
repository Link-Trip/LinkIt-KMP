package com.linkit.company.data.dto.video

import kotlinx.serialization.Serializable

@Serializable
data class VideoAnalyzeResponse(
    val id: String,
    val youtubeUrl: String,
    val valid: Boolean,
    val status: String,
    val summary: String? = null,
    val estimatedMinCost: Int? = null,
    val estimatedMaxCost: Int? = null,
    val costBasis: String? = null,
    val placeEnrichmentCompleted: Boolean = false,
    val timelines: List<TimelineResponse> = emptyList(),
    val itineraryItems: List<ScheduleItemResponse> = emptyList(),
)

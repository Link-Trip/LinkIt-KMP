package com.linkit.company.domain.model

data class TripPlanSummary(
    val id: String,
    val title: String,
    val videoAnalysisTaskId: String,
    val youtubeUrl: String,
    val itemCount: Int,
    val nights: Int,
    val days: Int,
    val hashtags: List<String>,
    val createdAt: String,
    val updatedAt: String,
)

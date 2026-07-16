package com.linkit.company.domain.model.tripplan

data class TripPlanDetail(
    val id: String,
    val title: String,
    val videoAnalysisTaskId: String,
    val items: List<TripPlanItem>,
    val createdAt: String,
    val updatedAt: String,
)

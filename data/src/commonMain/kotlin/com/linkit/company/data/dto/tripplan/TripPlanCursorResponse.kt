package com.linkit.company.data.dto.tripplan

import kotlinx.serialization.Serializable

@Serializable
data class TripPlanCursorResponse(
    val tripPlans: List<TripPlanSummaryResponse> = emptyList(),
    val nextCursor: String? = null,
    val hasNext: Boolean = false,
)

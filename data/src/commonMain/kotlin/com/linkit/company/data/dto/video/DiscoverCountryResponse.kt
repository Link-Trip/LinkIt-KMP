package com.linkit.company.data.dto.video

import kotlinx.serialization.Serializable

@Serializable
data class DiscoverCountryResponse(
    val country: String,
    val tripPlanCount: Long,
)

package com.linkit.company.data.dto.video

import kotlinx.serialization.Serializable

@Serializable
data class DiscoverCountryResponses(
    val countries: List<DiscoverCountryResponse> = emptyList(),
)

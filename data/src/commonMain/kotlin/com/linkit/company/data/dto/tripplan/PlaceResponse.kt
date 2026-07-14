package com.linkit.company.data.dto.tripplan

import kotlinx.serialization.Serializable

@Serializable
data class PlaceResponse(
    val id: String,
    val name: String,
    val googlePlaceId: String,
    val address: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
)

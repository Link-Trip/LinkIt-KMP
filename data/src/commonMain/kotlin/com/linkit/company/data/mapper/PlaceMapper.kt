package com.linkit.company.data.mapper

import com.linkit.company.data.dto.tripplan.PlaceResponse
import com.linkit.company.domain.model.Place

internal fun PlaceResponse.toDomain(): Place {
    return Place(
        id = id,
        name = name,
        googlePlaceId = googlePlaceId,
        address = address.orEmpty(),
        latitude = latitude,
        longitude = longitude,
    )
}

package com.linkit.company.data.dto.tripplan

import kotlinx.serialization.Serializable

/**
 * null 필드는 "변경하지 않음" 의미로 전송에서 생략된다 (defaultJson의 explicitNulls=false).
 */
@Serializable
internal data class UpdateTripPlanRequest(
    val title: String? = null,
    val items: List<UpdateTripPlanItemRequest>? = null,
)

@Serializable
internal data class UpdateTripPlanItemRequest(
    val tripPlanItemId: String,
    val day: Int,
    val itemOrder: Int,
)

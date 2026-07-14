package com.linkit.company.data.mapper

import com.linkit.company.data.dto.tripplan.TripPlanCursorResponse
import com.linkit.company.data.dto.tripplan.TripPlanDetailResponse
import com.linkit.company.data.dto.tripplan.TripPlanItemDetailResponse
import com.linkit.company.data.dto.tripplan.TripPlanSummaryResponse
import com.linkit.company.domain.model.CursorPage
import com.linkit.company.domain.model.PlaceCategory
import com.linkit.company.domain.model.TripPlanDetail
import com.linkit.company.domain.model.TripPlanItem
import com.linkit.company.domain.model.TripPlanSummary

internal fun TripPlanCursorResponse.toDomain(): CursorPage<TripPlanSummary> {
    return CursorPage(
        items = tripPlans.map { it.toDomain() },
        nextCursor = nextCursor,
        hasNext = hasNext,
    )
}

internal fun TripPlanSummaryResponse.toDomain(): TripPlanSummary {
    return TripPlanSummary(
        id = id,
        title = title,
        videoAnalysisTaskId = videoAnalysisTaskId,
        youtubeUrl = youtubeUrl,
        itemCount = itemCount,
        nights = nights,
        days = days,
        hashtags = hashtags,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}

internal fun TripPlanDetailResponse.toDomain(): TripPlanDetail {
    return TripPlanDetail(
        id = id,
        title = title,
        videoAnalysisTaskId = videoAnalysisTaskId,
        items = items.map { it.toDomain() },
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}

internal fun TripPlanItemDetailResponse.toDomain(): TripPlanItem {
    return TripPlanItem(
        id = id,
        travelItineraryItemId = travelItineraryItemId,
        day = day,
        itemOrder = itemOrder,
        name = name,
        category = PlaceCategory.from(category),
        description = description.orEmpty(),
        tips = tips.orEmpty(),
        place = place?.toDomain(),
    )
}

package com.linkit.company.data.datasource.tripplan

import com.linkit.company.data.dto.tripplan.TripPlanCursorResponse
import com.linkit.company.data.dto.tripplan.TripPlanDetailResponse

interface TripPlanRemoteDataSource {

    suspend fun getTripPlans(cursor: String?): TripPlanCursorResponse

    suspend fun getTripPlan(tripPlanId: String): TripPlanDetailResponse

    suspend fun updateTripPlan(
        tripPlanId: String,
        title: String?,
        items: List<TripPlanItemOrderParam>?,
    ): TripPlanDetailResponse

    suspend fun deleteTripPlan(tripPlanId: String)
}

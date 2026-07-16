package com.linkit.company.data.datasource.tripplan

import com.linkit.company.data.DataScope
import com.linkit.company.data.api.TripPlanApi
import com.linkit.company.data.dto.tripplan.TripPlanCursorResponse
import com.linkit.company.data.dto.tripplan.TripPlanDetailResponse
import com.linkit.company.data.dto.tripplan.UpdateTripPlanItemRequest
import com.linkit.company.data.dto.tripplan.UpdateTripPlanRequest
import de.jensklingenberg.ktorfit.Ktorfit
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject

@Inject
@ContributesBinding(DataScope::class)
class TripPlanRemoteDataSourceImpl(
    ktorfit: Ktorfit,
) : TripPlanRemoteDataSource {

    private val api = ktorfit.create<TripPlanApi>()

    override suspend fun getTripPlans(cursor: String?): TripPlanCursorResponse {
        val response = api.getTripPlans(cursor)
        return checkNotNull(response.data) { "trip-plans 응답에 data가 없습니다" }
    }

    override suspend fun getTripPlan(tripPlanId: String): TripPlanDetailResponse {
        val response = api.getTripPlan(tripPlanId)
        return checkNotNull(response.data) { "trip-plans/$tripPlanId 응답에 data가 없습니다" }
    }

    override suspend fun updateTripPlan(
        tripPlanId: String,
        title: String?,
        items: List<TripPlanItemOrderParam>?,
    ): TripPlanDetailResponse {
        val request = UpdateTripPlanRequest(
            title = title,
            items = items?.map {
                UpdateTripPlanItemRequest(
                    tripPlanItemId = it.tripPlanItemId,
                    day = it.day,
                    itemOrder = it.itemOrder,
                )
            },
        )
        val response = api.updateTripPlan(tripPlanId, request)
        return checkNotNull(response.data) { "trip-plans/$tripPlanId 수정 응답에 data가 없습니다" }
    }

    override suspend fun deleteTripPlan(tripPlanId: String) {
        api.deleteTripPlan(tripPlanId)
    }
}

package com.linkit.company.data.repository

import com.linkit.company.data.DataScope
import com.linkit.company.data.datasource.tripplan.TripPlanItemOrderParam
import com.linkit.company.data.datasource.tripplan.TripPlanRemoteDataSource
import com.linkit.company.data.mapper.toDomain
import com.linkit.company.domain.model.common.CursorPage
import com.linkit.company.domain.model.tripplan.TripPlanDetail
import com.linkit.company.domain.model.tripplan.TripPlanItemOrder
import com.linkit.company.domain.model.tripplan.TripPlanSummary
import com.linkit.company.domain.repository.TripPlanRepository
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject

@Inject
@ContributesBinding(DataScope::class)
class TripPlanRepositoryImpl(
    private val tripPlanRemoteDataSource: TripPlanRemoteDataSource,
) : TripPlanRepository {

    override suspend fun getTripPlans(cursor: String?): CursorPage<TripPlanSummary> {
        return tripPlanRemoteDataSource.getTripPlans(cursor).toDomain()
    }

    override suspend fun getTripPlan(tripPlanId: String): TripPlanDetail {
        return tripPlanRemoteDataSource.getTripPlan(tripPlanId).toDomain()
    }

    override suspend fun updateTripPlan(
        tripPlanId: String,
        title: String?,
        items: List<TripPlanItemOrder>?,
    ): TripPlanDetail {
        return tripPlanRemoteDataSource.updateTripPlan(
            tripPlanId = tripPlanId,
            title = title,
            items = items?.map { it.toParam() },
        ).toDomain()
    }

    override suspend fun deleteTripPlan(tripPlanId: String) {
        tripPlanRemoteDataSource.deleteTripPlan(tripPlanId)
    }

    private fun TripPlanItemOrder.toParam(): TripPlanItemOrderParam {
        return TripPlanItemOrderParam(
            tripPlanItemId = tripPlanItemId,
            day = day,
            itemOrder = itemOrder,
        )
    }
}

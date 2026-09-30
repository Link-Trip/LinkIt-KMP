package com.linkit.company.data.repository

import com.linkit.company.data.DataScope
import com.linkit.company.data.datasource.tripplan.TripPlanItemOrderParam
import com.linkit.company.data.datasource.tripplan.TripPlanLocalDataSource
import com.linkit.company.data.datasource.tripplan.TripPlanRemoteDataSource
import com.linkit.company.data.mapper.toDomain
import com.linkit.company.domain.model.common.CursorPage
import com.linkit.company.domain.model.tripplan.TripPlanDetail
import com.linkit.company.domain.model.tripplan.TripPlanItemOrder
import com.linkit.company.domain.model.tripplan.TripPlanSummary
import com.linkit.company.domain.repository.TripPlanRepository
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow

@Inject
@ContributesBinding(DataScope::class)
class TripPlanRepositoryImpl(
    private val tripPlanRemoteDataSource: TripPlanRemoteDataSource,
    private val tripPlanLocalDataSource: TripPlanLocalDataSource,
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
        tripPlanLocalDataSource.removeUncheckedId(tripPlanId)
    }

    override fun observeUncheckedTripPlanIds(): Flow<Set<String>> {
        return tripPlanLocalDataSource.observeUncheckedIds()
    }

    override suspend fun markTripPlanUnchecked(tripPlanId: String) {
        tripPlanLocalDataSource.addUncheckedId(tripPlanId)
    }

    override suspend fun markTripPlanChecked(tripPlanId: String) {
        tripPlanLocalDataSource.removeUncheckedId(tripPlanId)
    }

    override suspend fun clearUncheckedTripPlans() {
        tripPlanLocalDataSource.clearUnchecked()
    }

    private fun TripPlanItemOrder.toParam(): TripPlanItemOrderParam {
        return TripPlanItemOrderParam(
            tripPlanItemId = tripPlanItemId,
            day = day,
            itemOrder = itemOrder,
        )
    }
}

package com.linkit.company.domain.repository

import com.linkit.company.domain.model.CursorPage
import com.linkit.company.domain.model.TripPlanDetail
import com.linkit.company.domain.model.TripPlanItemOrder
import com.linkit.company.domain.model.TripPlanSummary

interface TripPlanRepository {

    suspend fun getTripPlans(cursor: String?): CursorPage<TripPlanSummary>

    suspend fun getTripPlan(tripPlanId: String): TripPlanDetail

    /**
     * 여행 계획을 수정한다. null인 항목은 변경하지 않는다.
     *
     * @param title 변경할 제목 (null이면 유지)
     * @param items 재배치할 아이템 목록 — 포함된 아이템만 수정된다 (null이면 유지)
     * @return 수정된 상세 정보
     */
    suspend fun updateTripPlan(
        tripPlanId: String,
        title: String?,
        items: List<TripPlanItemOrder>?,
    ): TripPlanDetail

    suspend fun deleteTripPlan(tripPlanId: String)
}

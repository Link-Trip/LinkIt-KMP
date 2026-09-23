package com.linkit.company.domain.repository

import com.linkit.company.domain.model.common.CursorPage
import com.linkit.company.domain.model.tripplan.TripPlanDetail
import com.linkit.company.domain.model.tripplan.TripPlanItemOrder
import com.linkit.company.domain.model.tripplan.TripPlanSummary
import kotlinx.coroutines.flow.Flow

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

    // ---- 확인전/확인후 (기기 로컬 상태, research R2) ----

    /** 아직 상세를 열지 않은 `확인전` 일정 id 집합. */
    fun observeUncheckedTripPlanIds(): Flow<Set<String>>

    /** 새로 생성된 일정을 `확인전`으로 표시한다. */
    suspend fun markTripPlanUnchecked(tripPlanId: String)

    /** 상세를 열어 `확인후`로 바꾼다. 이후 다시 강조하지 않는다. */
    suspend fun markTripPlanChecked(tripPlanId: String)

    /** 앱 초기화 시 집합을 비운다. */
    suspend fun clearUncheckedTripPlans()
}

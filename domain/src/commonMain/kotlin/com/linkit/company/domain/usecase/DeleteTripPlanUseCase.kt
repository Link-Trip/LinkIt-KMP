package com.linkit.company.domain.usecase

import com.linkit.company.domain.repository.TripPlanRepository
import dev.zacsweers.metro.Inject

/** 인증 상태를 확인한 뒤 일정을 삭제한다. */
@Inject
class DeleteTripPlanUseCase(
    private val ensureAuthenticated: EnsureAuthenticatedUseCase,
    private val tripPlanRepository: TripPlanRepository,
) {
    suspend operator fun invoke(tripPlanId: String) {
        ensureAuthenticated()
        tripPlanRepository.deleteTripPlan(tripPlanId)
    }
}

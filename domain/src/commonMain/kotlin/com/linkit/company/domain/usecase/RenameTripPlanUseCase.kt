package com.linkit.company.domain.usecase

import com.linkit.company.domain.model.tripplan.TripPlanDetail
import com.linkit.company.domain.repository.TripPlanRepository
import dev.zacsweers.metro.Inject

/** 일정 이름을 검증한 뒤 수정한다. */
@Inject
class RenameTripPlanUseCase(
    private val ensureAuthenticated: EnsureAuthenticatedUseCase,
    private val tripPlanRepository: TripPlanRepository,
) {
    suspend operator fun invoke(tripPlanId: String, title: String): TripPlanDetail {
        require(title.isNotBlank()) { "일정 이름을 입력해 주세요." }
        require(title.length <= MaxTitleLength) { "일정 이름은 20자까지 입력할 수 있어요." }

        ensureAuthenticated()
        return tripPlanRepository.updateTripPlan(
            tripPlanId = tripPlanId,
            title = title,
            items = null,
        )
    }

    private companion object {
        const val MaxTitleLength = 20
    }
}

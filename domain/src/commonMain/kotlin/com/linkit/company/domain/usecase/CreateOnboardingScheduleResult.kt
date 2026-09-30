package com.linkit.company.domain.usecase

/** [CreateOnboardingScheduleUseCase] 결과. */
sealed interface CreateOnboardingScheduleResult {
    /** 영상 링크 형식이 아니다 → `유효한 영상 링크가 아닙니다` */
    data object InvalidFormat : CreateOnboardingScheduleResult

    /** 형식은 맞지만 추천 영상이 아니다 → `온보딩에서는 추천 영상 링크만 사용할 수 있어요` */
    data object NotRecommended : CreateOnboardingScheduleResult

    /** 사전 분석이 끝나지 않았거나(202) 자동 생성된 일정을 찾지 못했다 → `미리 준비된 일정을 가져오지 못했어요` */
    data object NotReady : CreateOnboardingScheduleResult

    data class Created(
        val tripPlanId: String,
        val title: String,
    ) : CreateOnboardingScheduleResult
}

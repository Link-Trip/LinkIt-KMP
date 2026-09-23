package com.linkit.company.domain.usecase

import com.linkit.company.domain.model.onboarding.OnboardingCompletion
import com.linkit.company.domain.repository.OnboardingRepository
import dev.zacsweers.metro.Inject

/**
 * 온보딩을 완료로 기록하고 튜토리얼 단계를 끝낸다.
 *
 * `바로 시작하기`, 튜토리얼 `건너뛰기`, 분석 완료 화면 `생성된 일정 확인하기` 세 곳이 같은 행위를 쓴다.
 * 완료 기록이 먼저이고 단계 해제가 뒤다. 단계 해제로 화면이 일반 모드로 바뀐 뒤 완료 기록이 실패하면
 * 다음 실행에서 온보딩이 다시 시작되므로 순서를 고정한다.
 */
@Inject
class CompleteOnboardingUseCase(
    private val onboardingRepository: OnboardingRepository,
) {
    suspend operator fun invoke(reason: OnboardingCompletion) {
        onboardingRepository.setOnboardingCompleted(true)
        onboardingRepository.setTutorialStep(null)
    }
}

package com.linkit.company.feature.map.testing

import com.linkit.company.domain.model.onboarding.TutorialStep
import com.linkit.company.domain.repository.OnboardingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/** 메모리 기반 온보딩 저장소. 호출 순서를 기록한다. */
class FakeOnboardingRepository(
    initialStep: TutorialStep? = null,
) : OnboardingRepository {
    val events = mutableListOf<String>()
    var onboardingCompleted = false
    var termsAgreedAt: Long? = null
    val tutorialStep = MutableStateFlow(initialStep)

    override suspend fun isOnboardingCompleted(): Boolean = onboardingCompleted

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        events += "setOnboardingCompleted($completed)"
        onboardingCompleted = completed
    }

    override suspend fun isTermsAgreed(): Boolean = termsAgreedAt != null

    override suspend fun setTermsAgreed(agreedAtEpochMillis: Long) {
        events += "setTermsAgreed"
        termsAgreedAt = agreedAtEpochMillis
    }

    override fun observeTutorialStep(): Flow<TutorialStep?> = tutorialStep

    override suspend fun setTutorialStep(step: TutorialStep?) {
        events += "setTutorialStep($step)"
        tutorialStep.value = step
    }

    override suspend fun clearAll() {
        events += "clearAll"
        onboardingCompleted = false
        termsAgreedAt = null
        tutorialStep.value = null
    }
}

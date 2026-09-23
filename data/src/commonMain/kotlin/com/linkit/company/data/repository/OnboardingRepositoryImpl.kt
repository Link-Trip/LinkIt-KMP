package com.linkit.company.data.repository

import com.linkit.company.data.DataScope
import com.linkit.company.data.datasource.onboarding.OnboardingLocalDataSource
import com.linkit.company.domain.model.onboarding.TutorialStep
import com.linkit.company.domain.repository.OnboardingRepository
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Inject
@ContributesBinding(DataScope::class)
class OnboardingRepositoryImpl(
    private val onboardingLocalDataSource: OnboardingLocalDataSource,
) : OnboardingRepository {

    override suspend fun isOnboardingCompleted(): Boolean {
        return onboardingLocalDataSource.isOnboardingCompleted()
    }

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        onboardingLocalDataSource.saveOnboardingCompleted(completed)
    }

    override suspend fun isTermsAgreed(): Boolean {
        return onboardingLocalDataSource.getTermsAgreedAt() != null
    }

    override suspend fun setTermsAgreed(agreedAtEpochMillis: Long) {
        onboardingLocalDataSource.saveTermsAgreedAt(agreedAtEpochMillis)
    }

    override fun observeTutorialStep(): Flow<TutorialStep?> {
        return onboardingLocalDataSource.observeTutorialStep().map { stored ->
            TutorialStep.entries.firstOrNull { it.name == stored }
        }
    }

    override suspend fun setTutorialStep(step: TutorialStep?) {
        onboardingLocalDataSource.saveTutorialStep(step?.name)
    }

    override suspend fun clearAll() {
        onboardingLocalDataSource.clearAll()
    }
}

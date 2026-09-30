package com.linkit.company.domain.usecase

import com.linkit.company.domain.fake.InMemoryOnboardingRepository
import com.linkit.company.domain.model.onboarding.OnboardingCompletion
import com.linkit.company.domain.model.onboarding.TutorialStep
import com.linkit.company.domain.runImmediateSuspend
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CompleteOnboardingUseCaseTest {

    @Test
    fun recordsCompletionBeforeClearingTutorialStep() = runImmediateSuspend {
        val repository = InMemoryOnboardingRepository()
        repository.tutorialStep.value = TutorialStep.FREE

        CompleteOnboardingUseCase(repository)(OnboardingCompletion.TUTORIAL_FINISHED)

        assertEquals(listOf("setOnboardingCompleted(true)", "setTutorialStep(null)"), repository.events)
        assertTrue(repository.onboardingCompleted)
        assertNull(repository.tutorialStep.value)
    }

    @Test
    fun everyReasonCompletesOnboardingTheSameWay() = runImmediateSuspend {
        OnboardingCompletion.entries.forEach { reason ->
            val repository = InMemoryOnboardingRepository()
            repository.tutorialStep.value = TutorialStep.CREATE_BUTTON

            CompleteOnboardingUseCase(repository)(reason)

            assertTrue(repository.onboardingCompleted, "reason=$reason")
            assertNull(repository.tutorialStep.value, "reason=$reason")
        }
    }
}

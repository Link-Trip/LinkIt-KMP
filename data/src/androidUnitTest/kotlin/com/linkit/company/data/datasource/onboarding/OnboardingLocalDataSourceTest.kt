package com.linkit.company.data.datasource.onboarding

import com.linkit.company.data.core.createLinkItDataStore
import com.linkit.company.data.datasource.tripplan.TripPlanLocalDataSourceImpl
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** 실제 DataStore 파일(임시 경로)로 저장·읽기·초기화를 검증한다. 파일당 인스턴스 하나 규칙 때문에 테스트마다 새 파일을 쓴다. */
class OnboardingLocalDataSourceTest {

    @Test
    fun defaultsAreNotCompletedNotAgreedAndNoStep() = runBlocking {
        val source = OnboardingLocalDataSourceImpl(temporaryDataStore())

        assertFalse(source.isOnboardingCompleted())
        assertNull(source.getTermsAgreedAt())
        assertNull(source.observeTutorialStep().first())
    }

    @Test
    fun persistsCompletionAndTermsAgreedAt() = runBlocking {
        val source = OnboardingLocalDataSourceImpl(temporaryDataStore())

        source.saveOnboardingCompleted(true)
        source.saveTermsAgreedAt(1_700_000_000_000L)

        assertTrue(source.isOnboardingCompleted())
        assertEquals(1_700_000_000_000L, source.getTermsAgreedAt())
    }

    @Test
    fun tutorialStepFlowEmitsTransitions() = runBlocking {
        val source = OnboardingLocalDataSourceImpl(temporaryDataStore())

        source.saveTutorialStep("CREATE_BUTTON")
        assertEquals("CREATE_BUTTON", source.observeTutorialStep().first())

        source.saveTutorialStep("COPY_LINK")
        assertEquals("COPY_LINK", source.observeTutorialStep().first())

        source.saveTutorialStep(null)
        assertNull(source.observeTutorialStep().first())
    }

    @Test
    fun clearAllRemovesKeysAndResetsStep() = runBlocking {
        val source = OnboardingLocalDataSourceImpl(temporaryDataStore())
        source.saveOnboardingCompleted(true)
        source.saveTermsAgreedAt(1L)
        source.saveTutorialStep("FREE")

        source.clearAll()

        assertFalse(source.isOnboardingCompleted())
        assertNull(source.getTermsAgreedAt())
        assertNull(source.observeTutorialStep().first())
    }

    @Test
    fun clearAllLeavesOtherKeysUntouched() = runBlocking {
        val dataStore = temporaryDataStore()
        val onboarding = OnboardingLocalDataSourceImpl(dataStore)
        val tripPlans = TripPlanLocalDataSourceImpl(dataStore)
        tripPlans.addUncheckedId("plan-1")
        onboarding.saveOnboardingCompleted(true)

        onboarding.clearAll()

        assertEquals(setOf("plan-1"), tripPlans.observeUncheckedIds().first())
    }
}

internal fun temporaryDataStore() = createLinkItDataStore {
    val file = File.createTempFile("linkit-test-", ".preferences_pb")
    file.delete()
    file.absolutePath
}

package com.linkit.company.domain.usecase

import com.linkit.company.domain.exception.LinkTripApiException
import com.linkit.company.domain.exception.LinkTripErrorCode
import com.linkit.company.domain.fake.InMemoryAppSettingsRepository
import com.linkit.company.domain.fake.InMemoryOnboardingRepository
import com.linkit.company.domain.fake.ScriptedTripPlanRepository
import com.linkit.company.domain.fake.RecordingAuthRepository
import com.linkit.company.domain.fake.ScriptedMemberRepository
import com.linkit.company.domain.model.settings.MapDisplayType
import com.linkit.company.domain.runImmediateSuspend
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ResetAppUseCaseTest {

    @Test
    fun withdrawsThenClearsLocalSettingsThenLogsOut() = runImmediateSuspend {
        val auth = RecordingAuthRepository()
        val member = ScriptedMemberRepository(withdrawResults = listOf(3))
        val settings = InMemoryAppSettingsRepository(initial = MapDisplayType.SATELLITE)
        val onboarding = InMemoryOnboardingRepository(completed = true).apply { termsAgreedAt = 1L }
        val tripPlans = ScriptedTripPlanRepository().apply { uncheckedIds.value = setOf("plan-1") }

        createUseCase(auth, member, settings, onboarding, tripPlans)()

        assertEquals(listOf("withdraw"), member.events)
        assertEquals(1, settings.clearAllCount)
        assertEquals(MapDisplayType.DEFAULT, settings.mapDisplayType.value)
        assertEquals(listOf("clearAll"), onboarding.events)
        assertEquals(false, onboarding.onboardingCompleted)
        assertEquals(null, onboarding.termsAgreedAt)
        assertEquals(listOf("clearUnchecked"), tripPlans.events)
        assertEquals(emptySet(), tripPlans.uncheckedIds.value)
        assertEquals(listOf("logout"), auth.events)
    }

    @Test
    fun clearsAppSettingsThenOnboardingThenUncheckedBeforeLogout() = runImmediateSuspend {
        val auth = RecordingAuthRepository()
        val settings = InMemoryAppSettingsRepository()
        val onboarding = InMemoryOnboardingRepository()
        val tripPlans = ScriptedTripPlanRepository()
        val order = mutableListOf<String>()
        settings.onClearAll = { order += "appSettings" }
        val orderedOnboarding = object : com.linkit.company.domain.repository.OnboardingRepository by onboarding {
            override suspend fun clearAll() {
                order += "onboarding"
                onboarding.clearAll()
            }
        }
        val orderedTripPlans = object : com.linkit.company.domain.repository.TripPlanRepository by tripPlans {
            override suspend fun clearUncheckedTripPlans() {
                order += "tripPlans"
                tripPlans.clearUncheckedTripPlans()
            }
        }

        ResetAppUseCase(
            ensureAuthenticated = EnsureAuthenticatedUseCase(auth),
            memberRepository = ScriptedMemberRepository(),
            appSettingsRepository = settings,
            onboardingRepository = orderedOnboarding,
            tripPlanRepository = orderedTripPlans,
            authRepository = auth,
        )()

        assertEquals(listOf("appSettings", "onboarding", "tripPlans"), order)
        assertEquals(listOf("logout"), auth.events)
    }

    @Test
    fun logsInFirstWhenNoTokenIsStored() = runImmediateSuspend {
        val auth = RecordingAuthRepository(loggedIn = false)
        val member = ScriptedMemberRepository()
        val settings = InMemoryAppSettingsRepository()

        createUseCase(auth, member, settings)()

        assertEquals(listOf("login", "logout"), auth.events)
        assertEquals(listOf("withdraw"), member.events)
    }

    @Test
    fun treatsMissingMemberAsSuccess() = runImmediateSuspend {
        val auth = RecordingAuthRepository()
        val member = ScriptedMemberRepository(
            withdrawResults = listOf(apiException(LinkTripErrorCode.NOT_FOUND_MEMBER, 404)),
        )
        val settings = InMemoryAppSettingsRepository()

        createUseCase(auth, member, settings)()

        assertEquals(1, settings.clearAllCount)
        assertEquals(listOf("logout"), auth.events)
    }

    @Test
    fun retriesOnceWithRefreshedTokenAfterUnauthorized() = runImmediateSuspend {
        val auth = RecordingAuthRepository()
        val member = ScriptedMemberRepository(
            withdrawResults = listOf(apiException(LinkTripErrorCode.UNAUTHORIZED_TOKEN_EXPIRED, 401), 2),
        )
        val settings = InMemoryAppSettingsRepository()

        createUseCase(auth, member, settings)()

        assertEquals(listOf("withdraw", "withdraw"), member.events)
        // forceRefresh(logout → login) 뒤 최종 logout
        assertEquals(listOf("logout", "login", "logout"), auth.events)
        assertEquals(1, settings.clearAllCount)
    }

    @Test
    fun leavesLocalStateUntouchedWhenRemoteFails() = runImmediateSuspend {
        val auth = RecordingAuthRepository()
        val member = ScriptedMemberRepository(
            withdrawResults = listOf(apiException(LinkTripErrorCode.DUPLICATE_REQUEST, 409)),
        )
        val settings = InMemoryAppSettingsRepository(initial = MapDisplayType.SATELLITE)
        val onboarding = InMemoryOnboardingRepository(completed = true)
        val tripPlans = ScriptedTripPlanRepository().apply { uncheckedIds.value = setOf("plan-1") }

        val error = assertFailsWith<LinkTripApiException> {
            createUseCase(auth, member, settings, onboarding, tripPlans)()
        }

        assertEquals(LinkTripErrorCode.DUPLICATE_REQUEST, error.errorCode)
        assertEquals(0, settings.clearAllCount)
        assertEquals(MapDisplayType.SATELLITE, settings.mapDisplayType.value)
        assertEquals(emptyList(), onboarding.events)
        assertEquals(true, onboarding.onboardingCompleted)
        assertEquals(emptyList(), tripPlans.events)
        assertEquals(setOf("plan-1"), tripPlans.uncheckedIds.value)
        assertEquals(emptyList(), auth.events)
    }

    private fun createUseCase(
        auth: RecordingAuthRepository,
        member: ScriptedMemberRepository,
        settings: InMemoryAppSettingsRepository,
        onboarding: InMemoryOnboardingRepository = InMemoryOnboardingRepository(),
        tripPlans: ScriptedTripPlanRepository = ScriptedTripPlanRepository(),
    ) = ResetAppUseCase(
        ensureAuthenticated = EnsureAuthenticatedUseCase(auth),
        memberRepository = member,
        appSettingsRepository = settings,
        onboardingRepository = onboarding,
        tripPlanRepository = tripPlans,
        authRepository = auth,
    )

    private fun apiException(code: LinkTripErrorCode, status: Int) =
        LinkTripApiException(errorCode = code, httpStatus = status, message = code.name)
}

package com.linkit.company.domain.usecase

import com.linkit.company.domain.exception.LinkTripApiException
import com.linkit.company.domain.exception.LinkTripErrorCode
import com.linkit.company.domain.fake.InMemoryAppSettingsRepository
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

        createUseCase(auth, member, settings)()

        assertEquals(listOf("withdraw"), member.events)
        assertEquals(1, settings.clearAllCount)
        assertEquals(MapDisplayType.DEFAULT, settings.mapDisplayType.value)
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

        val error = assertFailsWith<LinkTripApiException> { createUseCase(auth, member, settings)() }

        assertEquals(LinkTripErrorCode.DUPLICATE_REQUEST, error.errorCode)
        assertEquals(0, settings.clearAllCount)
        assertEquals(MapDisplayType.SATELLITE, settings.mapDisplayType.value)
        assertEquals(emptyList(), auth.events)
    }

    private fun createUseCase(
        auth: RecordingAuthRepository,
        member: ScriptedMemberRepository,
        settings: InMemoryAppSettingsRepository,
    ) = ResetAppUseCase(
        ensureAuthenticated = EnsureAuthenticatedUseCase(auth),
        memberRepository = member,
        appSettingsRepository = settings,
        authRepository = auth,
    )

    private fun apiException(code: LinkTripErrorCode, status: Int) =
        LinkTripApiException(errorCode = code, httpStatus = status, message = code.name)
}

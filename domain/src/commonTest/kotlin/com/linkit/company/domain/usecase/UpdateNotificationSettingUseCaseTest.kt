package com.linkit.company.domain.usecase

import com.linkit.company.domain.exception.LinkTripApiException
import com.linkit.company.domain.exception.LinkTripErrorCode
import com.linkit.company.domain.fake.InMemoryAppSettingsRepository
import com.linkit.company.domain.fake.RecordingAuthRepository
import com.linkit.company.domain.fake.ScriptedMemberRepository
import com.linkit.company.domain.runImmediateSuspend
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class UpdateNotificationSettingUseCaseTest {

    @Test
    fun updatesServerThenSavesResponseLocally() = runImmediateSuspend {
        val auth = RecordingAuthRepository(loggedIn = false)
        val member = ScriptedMemberRepository()
        val settings = InMemoryAppSettingsRepository()

        createUseCase(auth, member, settings)(enabled = false)

        assertEquals(listOf("login"), auth.events)
        assertEquals(listOf(false), member.notificationCalls)
        assertEquals(listOf(false), settings.savedNotificationEnabled)
        assertEquals(false, settings.notificationEnabled.value)
    }

    @Test
    fun retriesOnceAfterUnauthorized() = runImmediateSuspend {
        val auth = RecordingAuthRepository()
        val member = ScriptedMemberRepository(
            notificationResults = listOf(
                LinkTripApiException(LinkTripErrorCode.UNAUTHORIZED_TOKEN_EXPIRED, 401, "만료"),
                Unit,
            ),
        )
        val settings = InMemoryAppSettingsRepository()

        createUseCase(auth, member, settings)(enabled = false)

        assertEquals(listOf(false, false), member.notificationCalls)
        assertEquals(listOf("logout", "login"), auth.events)
        assertEquals(listOf(false), settings.savedNotificationEnabled)
    }

    @Test
    fun failurePropagatesAndLeavesLocalUntouched() = runImmediateSuspend {
        val member = ScriptedMemberRepository(
            notificationResults = listOf(
                LinkTripApiException(LinkTripErrorCode.NOT_FOUND_MEMBER, 404, "없음"),
            ),
        )
        val settings = InMemoryAppSettingsRepository()

        val error = assertFailsWith<LinkTripApiException> {
            createUseCase(RecordingAuthRepository(), member, settings)(enabled = false)
        }

        assertEquals(LinkTripErrorCode.NOT_FOUND_MEMBER, error.errorCode)
        assertEquals(emptyList(), settings.savedNotificationEnabled)
        assertEquals(true, settings.notificationEnabled.value)
    }

    @Test
    fun secondUnauthorizedFailurePropagates() = runImmediateSuspend {
        val member = ScriptedMemberRepository(
            notificationResults = listOf(
                LinkTripApiException(LinkTripErrorCode.UNAUTHORIZED_TOKEN_EXPIRED, 401, "만료"),
                LinkTripApiException(LinkTripErrorCode.UNAUTHORIZED_AUTHENTICATION_FAILED, 401, "실패"),
            ),
        )
        val settings = InMemoryAppSettingsRepository()

        assertFailsWith<LinkTripApiException> {
            createUseCase(RecordingAuthRepository(), member, settings)(enabled = true)
        }

        assertEquals(listOf(true, true), member.notificationCalls)
        assertEquals(emptyList(), settings.savedNotificationEnabled)
    }

    private fun createUseCase(
        auth: RecordingAuthRepository,
        member: ScriptedMemberRepository,
        settings: InMemoryAppSettingsRepository,
    ) = UpdateNotificationSettingUseCase(
        ensureAuthenticated = EnsureAuthenticatedUseCase(auth),
        memberRepository = member,
        appSettingsRepository = settings,
    )
}

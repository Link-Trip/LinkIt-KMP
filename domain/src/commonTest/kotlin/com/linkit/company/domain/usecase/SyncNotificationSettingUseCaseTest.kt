package com.linkit.company.domain.usecase

import com.linkit.company.domain.exception.LinkTripApiException
import com.linkit.company.domain.exception.LinkTripErrorCode
import com.linkit.company.domain.fake.RecordingAuthRepository
import com.linkit.company.domain.fake.ScriptedMemberRepository
import com.linkit.company.domain.runImmediateSuspend
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlinx.coroutines.CancellationException

class SyncNotificationSettingUseCaseTest {

    @Test
    fun sendsEnabledValueAfterEnsuringAuthentication() = runImmediateSuspend {
        val auth = RecordingAuthRepository(loggedIn = false)
        val member = ScriptedMemberRepository()

        SyncNotificationSettingUseCase(EnsureAuthenticatedUseCase(auth), member)(enabled = true)

        assertEquals(listOf("login"), auth.events)
        assertEquals(listOf(true), member.notificationCalls)
    }

    @Test
    fun swallowsApiFailures() = runImmediateSuspend {
        val member = ScriptedMemberRepository(
            notificationResults = listOf(
                LinkTripApiException(LinkTripErrorCode.NOT_FOUND_MEMBER, 404, "없음"),
            ),
        )

        SyncNotificationSettingUseCase(EnsureAuthenticatedUseCase(RecordingAuthRepository()), member)(false)

        assertEquals(listOf(false), member.notificationCalls)
    }

    @Test
    fun retriesOnceAfterUnauthorizedThenSwallowsSecondFailure() = runImmediateSuspend {
        val auth = RecordingAuthRepository()
        val member = ScriptedMemberRepository(
            notificationResults = listOf(
                LinkTripApiException(LinkTripErrorCode.UNAUTHORIZED_TOKEN_EXPIRED, 401, "만료"),
                LinkTripApiException(LinkTripErrorCode.UNAUTHORIZED_AUTHENTICATION_FAILED, 401, "실패"),
            ),
        )

        SyncNotificationSettingUseCase(EnsureAuthenticatedUseCase(auth), member)(true)

        assertEquals(listOf(true, true), member.notificationCalls)
        assertEquals(listOf("logout", "login"), auth.events)
    }

    @Test
    fun rethrowsCancellation() = runImmediateSuspend {
        val member = ScriptedMemberRepository(notificationResults = listOf(CancellationException("취소")))

        assertFailsWith<CancellationException> {
            SyncNotificationSettingUseCase(EnsureAuthenticatedUseCase(RecordingAuthRepository()), member)(true)
        }
        Unit
    }
}

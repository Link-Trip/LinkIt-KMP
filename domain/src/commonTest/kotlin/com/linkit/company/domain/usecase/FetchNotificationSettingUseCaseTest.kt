package com.linkit.company.domain.usecase

import com.linkit.company.domain.exception.LinkTripApiException
import com.linkit.company.domain.exception.LinkTripErrorCode
import com.linkit.company.domain.fake.InMemoryAppSettingsRepository
import com.linkit.company.domain.fake.RecordingAuthRepository
import com.linkit.company.domain.fake.ScriptedMemberRepository
import com.linkit.company.domain.model.member.NotificationSetting
import com.linkit.company.domain.runImmediateSuspend
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlinx.coroutines.CancellationException

class FetchNotificationSettingUseCaseTest {

    @Test
    fun savesServerValueLocallyAfterEnsuringAuthentication() = runImmediateSuspend {
        val auth = RecordingAuthRepository(loggedIn = false)
        val member = ScriptedMemberRepository(getNotificationResults = listOf(NotificationSetting(enabled = false)))
        val settings = InMemoryAppSettingsRepository()

        createUseCase(auth, member, settings)()

        assertEquals(listOf("login"), auth.events)
        assertEquals(listOf("getNotification"), member.events)
        assertEquals(listOf(false), settings.savedNotificationEnabled)
        assertEquals(false, settings.notificationEnabled.value)
    }

    @Test
    fun retriesOnceAfterUnauthorized() = runImmediateSuspend {
        val auth = RecordingAuthRepository()
        val member = ScriptedMemberRepository(
            getNotificationResults = listOf(
                LinkTripApiException(LinkTripErrorCode.UNAUTHORIZED_TOKEN_EXPIRED, 401, "만료"),
                NotificationSetting(enabled = false),
            ),
        )
        val settings = InMemoryAppSettingsRepository()

        createUseCase(auth, member, settings)()

        assertEquals(listOf("getNotification", "getNotification"), member.events)
        assertEquals(listOf("logout", "login"), auth.events)
        assertEquals(listOf(false), settings.savedNotificationEnabled)
    }

    @Test
    fun swallowsFailureAndLeavesLocalUntouched() = runImmediateSuspend {
        val member = ScriptedMemberRepository(
            getNotificationResults = listOf(
                LinkTripApiException(LinkTripErrorCode.NOT_FOUND_MEMBER, 404, "없음"),
            ),
        )
        val settings = InMemoryAppSettingsRepository()

        createUseCase(RecordingAuthRepository(), member, settings)()

        assertEquals(emptyList(), settings.savedNotificationEnabled)
        assertEquals(true, settings.notificationEnabled.value)
    }

    @Test
    fun swallowsSecondUnauthorizedFailure() = runImmediateSuspend {
        val member = ScriptedMemberRepository(
            getNotificationResults = listOf(
                LinkTripApiException(LinkTripErrorCode.UNAUTHORIZED_TOKEN_EXPIRED, 401, "만료"),
                LinkTripApiException(LinkTripErrorCode.UNAUTHORIZED_AUTHENTICATION_FAILED, 401, "실패"),
            ),
        )
        val settings = InMemoryAppSettingsRepository()

        createUseCase(RecordingAuthRepository(), member, settings)()

        assertEquals(listOf("getNotification", "getNotification"), member.events)
        assertEquals(emptyList(), settings.savedNotificationEnabled)
    }

    @Test
    fun rethrowsCancellation() = runImmediateSuspend {
        val member = ScriptedMemberRepository(getNotificationResults = listOf(CancellationException("취소")))
        val settings = InMemoryAppSettingsRepository()

        assertFailsWith<CancellationException> {
            createUseCase(RecordingAuthRepository(), member, settings)()
        }
        assertEquals(emptyList(), settings.savedNotificationEnabled)
    }

    private fun createUseCase(
        auth: RecordingAuthRepository,
        member: ScriptedMemberRepository,
        settings: InMemoryAppSettingsRepository,
    ) = FetchNotificationSettingUseCase(
        ensureAuthenticated = EnsureAuthenticatedUseCase(auth),
        memberRepository = member,
        appSettingsRepository = settings,
    )
}

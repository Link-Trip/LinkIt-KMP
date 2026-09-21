package com.linkit.company.domain.usecase

import com.linkit.company.domain.exception.LinkTripApiException
import com.linkit.company.domain.exception.LinkTripErrorCode
import com.linkit.company.domain.fake.RecordingAuthRepository
import com.linkit.company.domain.model.app.AppInfo
import com.linkit.company.domain.model.member.NotificationSetting
import com.linkit.company.domain.repository.AppInfoRepository
import com.linkit.company.domain.repository.MemberRepository
import com.linkit.company.domain.runImmediateSuspend
import kotlinx.coroutines.CancellationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class RegisterFcmTokenUseCaseTest {
    @Test
    fun registersRealTokenWithPlatformAfterLogin() = runImmediateSuspend {
        for (platform in listOf("ANDROID", "IOS")) {
            val auth = RecordingAuthRepository(loggedIn = false)
            val member = TokenMemberRepository()
            useCase(auth, member, platform)(" sdk-token ")
            assertEquals(listOf("login"), auth.events)
            assertEquals(listOf("sdk-token" to platform), member.calls)
        }
    }

    @Test
    fun rejectsMissingTokenOrInvalidPlatformBeforeAuthentication() = runImmediateSuspend {
        val auth = RecordingAuthRepository(loggedIn = false)
        val member = TokenMemberRepository()
        assertFailsWith<IllegalArgumentException> { useCase(auth, member)(" ") }
        assertFailsWith<IllegalArgumentException> { useCase(auth, member, "WEB")("token") }
        assertEquals(emptyList(), auth.events)
        assertEquals(emptyList(), member.calls)
    }

    @Test
    fun refreshesAuthenticationOnlyOnceOnExpiredToken() = runImmediateSuspend {
        val auth = RecordingAuthRepository()
        val unauthorized = LinkTripApiException(LinkTripErrorCode.UNAUTHORIZED_TOKEN_EXPIRED, 401, "만료")
        val member = TokenMemberRepository(mutableListOf(unauthorized, unauthorized))
        assertFailsWith<LinkTripApiException> { useCase(auth, member)("sdk-token") }
        assertEquals(listOf("logout", "login"), auth.events)
        assertEquals(2, member.calls.size)
    }

    @Test
    fun propagatesCancellationAndServerFailuresWithoutRetry() = runImmediateSuspend {
        for (error in listOf(
            CancellationException("취소"),
            LinkTripApiException(LinkTripErrorCode.NOT_FOUND_MEMBER, 404, "없음"),
        )) {
            val auth = RecordingAuthRepository()
            val member = TokenMemberRepository(mutableListOf(error))
            val thrown = runCatching { useCase(auth, member)("sdk-token") }.exceptionOrNull()
            assertEquals(error, thrown)
            assertEquals(1, member.calls.size)
            assertEquals(emptyList(), auth.events)
        }
    }

    private fun useCase(
        auth: RecordingAuthRepository,
        member: MemberRepository,
        platform: String = "ANDROID",
    ) = RegisterFcmTokenUseCase(
        EnsureAuthenticatedUseCase(auth), member,
        object : AppInfoRepository {
            override suspend fun getAppInfo() = AppInfo("1.0", platform, "", "")
        },
    )
}

private class TokenMemberRepository(
    private val failures: MutableList<Exception> = mutableListOf(),
) : MemberRepository {
    val calls = mutableListOf<Pair<String, String>>()
    override suspend fun registerFcmToken(fcmToken: String, platform: String) {
        calls += fcmToken to platform
        if (failures.isNotEmpty()) throw failures.removeAt(0)
    }
    override suspend fun updateNotificationSetting(enabled: Boolean): NotificationSetting = error("Not used")
    override suspend fun withdraw(): Int = error("Not used")
}

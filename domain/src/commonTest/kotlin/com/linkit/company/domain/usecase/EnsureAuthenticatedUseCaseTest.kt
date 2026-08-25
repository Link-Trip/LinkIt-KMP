package com.linkit.company.domain.usecase

import com.linkit.company.domain.model.auth.Auth
import com.linkit.company.domain.repository.AuthRepository
import com.linkit.company.domain.runImmediateSuspend
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EnsureAuthenticatedUseCaseTest {

    @Test
    fun doesNotLoginWhenAccessTokenAlreadyExists() = runImmediateSuspend {
        val repository = EnsureAuthRepositoryFake(isLoggedIn = true)

        EnsureAuthenticatedUseCase(repository)()

        assertEquals(1, repository.isLoggedInCallCount)
        assertEquals(0, repository.loginCallCount)
    }

    @Test
    fun logsInWhenAccessTokenDoesNotExist() = runImmediateSuspend {
        val repository = EnsureAuthRepositoryFake(isLoggedIn = false)

        EnsureAuthenticatedUseCase(repository)()

        assertEquals(1, repository.isLoggedInCallCount)
        assertEquals(1, repository.loginCallCount)
        assertTrue(repository.isLoggedIn())
    }

    @Test
    fun forceRefreshLogsOutBeforeLoggingInAgain() = runImmediateSuspend {
        val repository = EnsureAuthRepositoryFake(isLoggedIn = true)

        EnsureAuthenticatedUseCase(repository)(forceRefresh = true)

        assertEquals(0, repository.isLoggedInCallCount)
        assertEquals(1, repository.logoutCallCount)
        assertEquals(1, repository.loginCallCount)
        assertEquals(listOf("logout", "login"), repository.authenticationEvents)
        assertTrue(repository.isLoggedIn())
    }
}

private class EnsureAuthRepositoryFake(
    private var isLoggedIn: Boolean,
) : AuthRepository {
    var isLoggedInCallCount = 0
        private set
    var loginCallCount = 0
        private set
    var logoutCallCount = 0
        private set
    val authenticationEvents = mutableListOf<String>()

    override suspend fun login(): Auth {
        loginCallCount += 1
        authenticationEvents += "login"
        isLoggedIn = true
        return Auth(memberId = "member-id", accessToken = "access-token")
    }

    override suspend fun isLoggedIn(): Boolean {
        isLoggedInCallCount += 1
        return isLoggedIn
    }

    override suspend fun logout() {
        logoutCallCount += 1
        authenticationEvents += "logout"
        isLoggedIn = false
    }
}

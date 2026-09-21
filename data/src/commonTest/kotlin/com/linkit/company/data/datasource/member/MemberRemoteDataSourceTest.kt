package com.linkit.company.data.datasource.member

import com.linkit.company.data.datasource.errorBody
import com.linkit.company.data.datasource.mockKtorfit
import com.linkit.company.data.datasource.respondJson
import com.linkit.company.domain.exception.LinkTripApiException
import com.linkit.company.domain.exception.LinkTripErrorCode
import io.ktor.client.request.HttpRequestData
import io.ktor.content.TextContent
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlinx.coroutines.runBlocking

class MemberRemoteDataSourceTest {

    @Test
    fun withdrawReturnsDeletedTripPlanCount() = runBlocking<Unit> {
        var captured: HttpRequestData? = null
        val dataSource = MemberRemoteDataSourceImpl(
            mockKtorfit { request ->
                captured = request
                respondJson("""{"status":200,"message":"OK","data":{"deletedTripPlanCount":3}}""")
            },
        )

        val response = dataSource.withdraw()

        assertEquals(3, response.deletedTripPlanCount)
        val request = checkNotNull(captured)
        assertEquals(HttpMethod.Delete, request.method)
        assertEquals("/api/members/me", request.url.encodedPath)
        assertNotNull(request.headers["Idempotency-Key"])
    }

    @Test
    fun withdrawMissingMemberBecomesNotFoundMember() = runBlocking<Unit> {
        val dataSource = MemberRemoteDataSourceImpl(
            mockKtorfit { respondJson(errorBody("NOT_FOUND_MEMBER"), HttpStatusCode.NotFound) },
        )

        val error = assertFailsWith<LinkTripApiException> { dataSource.withdraw() }

        assertEquals(LinkTripErrorCode.NOT_FOUND_MEMBER, error.errorCode)
        assertEquals(404, error.httpStatus)
    }

    @Test
    fun updateNotificationSettingSendsEnabledBody() = runBlocking<Unit> {
        var captured: HttpRequestData? = null
        val dataSource = MemberRemoteDataSourceImpl(
            mockKtorfit { request ->
                captured = request
                respondJson("""{"status":200,"message":"OK","data":{"enabled":false}}""")
            },
        )

        val response = dataSource.updateNotificationSetting(enabled = false)

        assertEquals(false, response.enabled)
        val request = checkNotNull(captured)
        assertEquals(HttpMethod.Put, request.method)
        assertEquals("/api/members/me/notification", request.url.encodedPath)
        assertEquals("""{"enabled":false}""", (request.body as TextContent).text)
    }

    @Test
    fun expiredTokenBecomesUnauthorizedTokenExpired() = runBlocking<Unit> {
        val dataSource = MemberRemoteDataSourceImpl(
            mockKtorfit {
                respondJson(errorBody("UNAUTHORIZED_TOKEN_EXPIRED"), HttpStatusCode.Unauthorized)
            },
        )

        val error = assertFailsWith<LinkTripApiException> { dataSource.updateNotificationSetting(true) }

        assertEquals(LinkTripErrorCode.UNAUTHORIZED_TOKEN_EXPIRED, error.errorCode)
    }
}

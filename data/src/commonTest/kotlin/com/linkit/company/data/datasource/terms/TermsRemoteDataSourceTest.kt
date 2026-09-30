package com.linkit.company.data.datasource.terms

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
import kotlin.test.assertNull
import kotlinx.coroutines.runBlocking

class TermsRemoteDataSourceTest {

    @Test
    fun getTermsUnwrapsListAndSendsBearerOnly() = runBlocking<Unit> {
        var captured: HttpRequestData? = null
        val dataSource = TermsRemoteDataSourceImpl(
            mockKtorfit { request ->
                captured = request
                respondJson(
                    """
                    {"status":200,"message":"OK","data":{"terms":[
                      {"type":"SERVICE","title":"서비스 이용약관 동의","required":true,"version":1,
                       "detailUrl":"https://pingo.notion.site/terms","agreed":false},
                      {"type":"PRIVACY","title":"개인정보 수집·이용 동의","required":true,"version":2,
                       "detailUrl":"https://pingo.notion.site/privacy","agreed":true}
                    ]}}
                    """.trimIndent(),
                )
            },
        )

        val terms = dataSource.getTerms()

        assertEquals(2, terms.size)
        assertEquals("SERVICE", terms[0].type)
        assertEquals(false, terms[0].agreed)
        assertEquals(2, terms[1].version)
        val request = checkNotNull(captured)
        assertEquals(HttpMethod.Get, request.method)
        assertEquals("/api/terms", request.url.encodedPath)
        assertEquals("Bearer access-token", request.headers["Authorization"])
        assertNull(request.headers["Idempotency-Key"])
    }

    @Test
    fun getTermsWithoutDataReturnsEmptyList() = runBlocking<Unit> {
        val dataSource = TermsRemoteDataSourceImpl(
            mockKtorfit { respondJson("""{"status":200,"message":"OK"}""") },
        )

        assertEquals(emptyList(), dataSource.getTerms())
    }

    @Test
    fun agreeSendsTypesBodyWithIdempotencyKey() = runBlocking<Unit> {
        var captured: HttpRequestData? = null
        val dataSource = TermsRemoteDataSourceImpl(
            mockKtorfit { request ->
                captured = request
                respondJson("""{"status":200,"message":"OK"}""")
            },
        )

        dataSource.agree(listOf("SERVICE", "PRIVACY"))

        val request = checkNotNull(captured)
        assertEquals(HttpMethod.Post, request.method)
        assertEquals("/api/terms/agreement", request.url.encodedPath)
        assertEquals("""{"types":["SERVICE","PRIVACY"]}""", (request.body as TextContent).text)
        assertNotNull(request.headers["Idempotency-Key"])
    }

    @Test
    fun missingRequiredTermBecomesTermsRequiredError() = runBlocking<Unit> {
        val dataSource = TermsRemoteDataSourceImpl(
            mockKtorfit { respondJson(errorBody("BAD_REQUEST_TERMS_REQUIRED"), HttpStatusCode.BadRequest) },
        )

        val error = assertFailsWith<LinkTripApiException> { dataSource.agree(listOf("SERVICE")) }

        assertEquals(LinkTripErrorCode.BAD_REQUEST_TERMS_REQUIRED, error.errorCode)
        assertEquals(400, error.httpStatus)
    }

    @Test
    fun unauthorizedGetBecomesAuthenticationFailed() = runBlocking<Unit> {
        val dataSource = TermsRemoteDataSourceImpl(
            mockKtorfit {
                respondJson(errorBody("UNAUTHORIZED_AUTHENTICATION_FAILED"), HttpStatusCode.Unauthorized)
            },
        )

        val error = assertFailsWith<LinkTripApiException> { dataSource.getTerms() }

        assertEquals(LinkTripErrorCode.UNAUTHORIZED_AUTHENTICATION_FAILED, error.errorCode)
        assertEquals(401, error.httpStatus)
    }
}

package com.linkit.company.data.datasource.feedback

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
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class FeedbackRemoteDataSourceTest {

    @Test
    fun sendsAllSixFieldsAsJsonBody() = runBlocking<Unit> {
        var captured: HttpRequestData? = null
        val dataSource = FeedbackRemoteDataSourceImpl(
            mockKtorfit { request ->
                captured = request
                respondJson("""{"status":200,"message":"OK"}""")
            },
        )

        dataSource.createFeedback(
            type = "SUGGESTION",
            content = "지도에서 순서를 바꾸고 싶어요",
            appVersion = "1.0",
            platform = "ANDROID",
            osVersion = "15",
            deviceModel = "Pixel 8",
        )

        val request = checkNotNull(captured)
        assertEquals(HttpMethod.Post, request.method)
        assertEquals("/api/feedback", request.url.encodedPath)
        val body = Json.parseToJsonElement((request.body as TextContent).text).jsonObject
        assertEquals(6, body.size)
        assertEquals("SUGGESTION", body.getValue("type").jsonPrimitive.content)
        assertEquals("지도에서 순서를 바꾸고 싶어요", body.getValue("content").jsonPrimitive.content)
        assertEquals("1.0", body.getValue("appVersion").jsonPrimitive.content)
        assertEquals("ANDROID", body.getValue("platform").jsonPrimitive.content)
        assertEquals("15", body.getValue("osVersion").jsonPrimitive.content)
        assertEquals("Pixel 8", body.getValue("deviceModel").jsonPrimitive.content)
    }

    @Test
    fun dailyLimitBecomesFeedbackDailyLimitExceeded() = runBlocking<Unit> {
        val dataSource = FeedbackRemoteDataSourceImpl(
            mockKtorfit {
                respondJson(errorBody("FEEDBACK_DAILY_LIMIT_EXCEEDED"), HttpStatusCode.TooManyRequests)
            },
        )

        val error = assertFailsWith<LinkTripApiException> { send(dataSource) }

        assertEquals(LinkTripErrorCode.FEEDBACK_DAILY_LIMIT_EXCEEDED, error.errorCode)
        assertEquals(429, error.httpStatus)
    }

    @Test
    fun rateLimitStaysDistinctFromDailyLimit() = runBlocking<Unit> {
        val dataSource = FeedbackRemoteDataSourceImpl(
            mockKtorfit {
                respondJson(errorBody("TOO_MANY_REQUESTS"), HttpStatusCode.TooManyRequests)
            },
        )

        val error = assertFailsWith<LinkTripApiException> { send(dataSource) }

        assertEquals(LinkTripErrorCode.TOO_MANY_REQUESTS, error.errorCode)
    }

    @Test
    fun unsupportedTypeBecomesBadRequestFeedbackType() = runBlocking<Unit> {
        val dataSource = FeedbackRemoteDataSourceImpl(
            mockKtorfit {
                respondJson(errorBody("BAD_REQUEST_FEEDBACK_TYPE"), HttpStatusCode.BadRequest)
            },
        )

        val error = assertFailsWith<LinkTripApiException> { send(dataSource) }

        assertEquals(LinkTripErrorCode.BAD_REQUEST_FEEDBACK_TYPE, error.errorCode)
    }

    private suspend fun send(dataSource: FeedbackRemoteDataSource) {
        dataSource.createFeedback("ETC", "내용", "1.0", "IOS", "18", "iPhone")
    }
}

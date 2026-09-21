package com.linkit.company.data.datasource

import com.linkit.company.data.core.defaultJson
import com.linkit.company.data.core.defaultKtorConfig
import de.jensklingenberg.ktorfit.Ktorfit
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf

/** 실제 `defaultKtorConfig`(에러 변환·공통 헤더)를 그대로 태운 MockEngine Ktorfit. */
internal fun mockKtorfit(
    handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData,
): Ktorfit {
    val client = HttpClient(MockEngine(handler)) {
        defaultKtorConfig(defaultJson()) { "access-token" }
    }
    return Ktorfit.Builder()
        .httpClient(client)
        .baseUrl("https://linktrip.cloud/api/")
        .build()
}

internal fun MockRequestHandleScope.respondJson(
    body: String,
    status: HttpStatusCode = HttpStatusCode.OK,
): HttpResponseData = respond(
    content = body,
    status = status,
    headers = headersOf("Content-Type", ContentType.Application.Json.toString()),
)

internal fun errorBody(code: String, message: String = code): String =
    """{"code":"$code","message":"$message","timestamp":1785390616431}"""

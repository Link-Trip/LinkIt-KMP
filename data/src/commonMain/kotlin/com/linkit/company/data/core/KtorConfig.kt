package com.linkit.company.data.core

import com.linkit.company.data.dto.ErrorResponse
import com.linkit.company.domain.exception.LinkTripApiException
import com.linkit.company.domain.exception.LinkTripErrorCode
import io.ktor.client.HttpClientConfig
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.serialization.json.Json

fun HttpClientConfig<*>.defaultKtorConfig(
    ktorJsonSettings: Json,
    accessTokenProvider: suspend () -> String?,
) {
    install(ContentNegotiation) {
        json(ktorJsonSettings)
    }

    install(linkTripHeadersPlugin(accessTokenProvider))

    // 실패(4xx/5xx) 응답 {code, message}를 LinkTripApiException으로 전환한다
    HttpResponseValidator {
        validateResponse { response ->
            if (!response.status.isSuccess()) {
                val error = runCatching { response.body<ErrorResponse>() }.getOrNull()
                throw LinkTripApiException(
                    errorCode = LinkTripErrorCode.from(error?.code),
                    httpStatus = response.status.value,
                    message = error?.message ?: response.status.description,
                )
            }
        }
    }
}

/**
 * LinkTrip API 공통 헤더를 자동 첨부한다.
 * - non-GET 요청: `Idempotency-Key` (UUID v4) — 서버 필수 헤더
 * - 저장된 accessToken이 있으면: `Authorization: Bearer` (로그인 전에는 미첨부)
 *
 * 주의: HttpRequestRetry 도입 시 재시도마다 Idempotency-Key가 재생성되어
 * 멱등성이 무력화되므로, 그때는 attributes 기반으로 키를 고정해야 한다.
 */
@OptIn(ExperimentalUuidApi::class)
private fun linkTripHeadersPlugin(accessTokenProvider: suspend () -> String?) =
    createClientPlugin("LinkTripHeaders") {
        onRequest { request, _ ->
            if (!request.url.host.equals(LinkTripHost, ignoreCase = true)) {
                return@onRequest
            }
            if (request.method != HttpMethod.Get) {
                request.headers.append("Idempotency-Key", Uuid.random().toString())
            }
            accessTokenProvider()?.let { token ->
                request.headers.append(HttpHeaders.Authorization, "Bearer $token")
            }
        }
    }

private const val LinkTripHost = "linktrip.cloud"

fun defaultJson(): Json {
    return Json {
        encodeDefaults = true
        isLenient = true
        prettyPrint = false
        ignoreUnknownKeys = true
        // null 필드는 직렬화에서 생략한다.
        // (예: UpdateTripPlanRequest.title=null은 "변경하지 않음" 의미 — "title":null 전송 방지)
        explicitNulls = false
    }
}

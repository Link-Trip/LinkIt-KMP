package com.linkit.company.data.core

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respondOk
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.http.HttpHeaders
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlinx.coroutines.runBlocking

class KtorConfigTest {
    @Test
    fun linkTripHeadersAreOnlySentToLinkTripHost() = runBlocking {
        val requests = mutableListOf<Pair<String, Map<String, List<String>>>>()
        val client = HttpClient(
            MockEngine { request ->
                requests += request.url.host to request.headers.entries()
                    .associate { (name, values) -> name to values }
                respondOk()
            },
        ) {
            defaultKtorConfig(defaultJson()) { "access-token" }
        }

        client.post("https://linktrip.cloud/api/video/analyze")
        client.get("https://www.youtube.com/oembed")
        client.close()

        val linkTripHeaders = requests[0].second
        assertEquals("Bearer access-token", linkTripHeaders[HttpHeaders.Authorization]?.single())
        assertNotNull(linkTripHeaders["Idempotency-Key"]?.single())

        val youtubeHeaders = requests[1].second
        assertNull(youtubeHeaders[HttpHeaders.Authorization])
        assertNull(youtubeHeaders["Idempotency-Key"])
    }
}

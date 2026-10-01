package com.linkit.company.data.datasource.video

import com.linkit.company.data.core.defaultJson
import com.linkit.company.data.core.defaultKtorConfig
import com.linkit.company.data.datasource.errorBody
import com.linkit.company.data.datasource.respondJson
import com.linkit.company.data.dto.video.VideoAnalyzeResponse
import com.linkit.company.data.mapper.toDomain
import com.linkit.company.data.repository.VideoRepositoryImpl
import com.linkit.company.domain.exception.LinkTripApiException
import com.linkit.company.domain.exception.LinkTripErrorCode
import com.linkit.company.domain.model.video.DiscoverCountry
import de.jensklingenberg.ktorfit.Ktorfit
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class VideoApiContractTest {
    @Test
    fun countriesUseAuthenticatedGetAndPreserveLongCounts() = runBlocking<Unit> {
        var captured: HttpRequestData? = null
        val repository = repository { request ->
            captured = request
            respondJson("""{"status":200,"message":"OK","data":{"countries":[{"country":"일본","tripPlanCount":2147483648}]}}""")
        }

        assertEquals(listOf(DiscoverCountry("일본", 2_147_483_648L)), repository.getDiscoverCountries())
        val request = checkNotNull(captured)
        assertEquals(HttpMethod.Get, request.method)
        assertEquals("/api/video/discover/countries", request.url.encodedPath)
        assertEquals("Bearer access-token", request.headers["Authorization"])
        assertNull(request.headers["Idempotency-Key"])
        assertEquals(emptySet(), request.url.parameters.names())
    }

    @Test
    fun allVideosOmitCountryAndRegionAndMapOptionalFields() = runBlocking<Unit> {
        var captured: HttpRequestData? = null
        val repository = repository { request ->
            captured = request
            respondJson(
                """{"status":200,"message":"OK","data":{"videos":[{
                    "videoId":"video-id","videoUrl":"https://www.youtube.com/watch?v=video-id",
                    "title":"영상","thumbnailUrl":"https://i.ytimg.com/vi/video-id/hqdefault.jpg",
                    "channelId":"channel-id","channelTitle":"채널","viewCount":3000000000,
                    "likeCount":123,"duration":"PT35M29S","publishedAt":"2026-09-21T09:00:00Z",
                    "region":"동아시아","country":"일본","city":null,"theme":"맛집여행"
                }]}}""",
            )
        }

        val video = repository.getDiscoverVideos().single()

        assertEquals("video-id", video.videoId)
        assertEquals(3_000_000_000L, video.viewCount)
        assertEquals("맛집여행", video.theme)
        assertEquals("", video.city)
        val request = checkNotNull(captured)
        assertEquals(HttpMethod.Get, request.method)
        assertEquals("/api/video/discover/category", request.url.encodedPath)
        assertEquals(emptySet(), request.url.parameters.names())
        assertEquals("Bearer access-token", request.headers["Authorization"])
        assertNull(request.headers["Idempotency-Key"])
    }

    @Test
    fun emptyCountriesAreAnEmptyList() = runBlocking<Unit> {
        val repository = repository {
            respondJson("""{"status":200,"message":"OK","data":{"countries":[]}}""")
        }
        assertEquals(emptyList(), repository.getDiscoverCountries())
    }

    @Test
    fun countryAndCategoryErrorsUseSharedApiErrorMapping() = runBlocking<Unit> {
        val repository = repository { request ->
            if (request.url.encodedPath.endsWith("countries")) {
                respondJson(errorBody("UNAUTHORIZED_TOKEN_EXPIRED"), HttpStatusCode.Unauthorized)
            } else {
                respondJson(errorBody("BAD_REQUEST_DISCOVER_QUERY"), HttpStatusCode.BadRequest)
            }
        }

        val countriesError = assertFailsWith<LinkTripApiException> { repository.getDiscoverCountries() }
        assertEquals(LinkTripErrorCode.UNAUTHORIZED_TOKEN_EXPIRED, countriesError.errorCode)
        assertEquals(401, countriesError.httpStatus)
        val categoryError = assertFailsWith<LinkTripApiException> { repository.getDiscoverVideos() }
        assertEquals(LinkTripErrorCode.BAD_REQUEST_DISCOVER_QUERY, categoryError.errorCode)
        assertEquals(400, categoryError.httpStatus)
    }

    @Test
    fun analysisCostsAboveIntRangeSurviveSerializationAndDomainMapping() {
        val json = defaultJson()
        val response = json.decodeFromString<VideoAnalyzeResponse>(
            """{"id":"analysis","youtubeUrl":"https://youtu.be/video-id","valid":true,
                "status":"COMPLETED","estimatedMinCost":2147483648,"estimatedMaxCost":9000000000}""",
        )
        val roundTrip = json.decodeFromString<VideoAnalyzeResponse>(json.encodeToString(response))
        val analysis = roundTrip.toDomain()

        assertEquals(response, roundTrip)
        assertEquals(2_147_483_648L, analysis.estimatedMinCost)
        assertEquals(9_000_000_000L, analysis.estimatedMaxCost)
    }

    private fun repository(
        handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData,
    ): VideoRepositoryImpl {
        val client = HttpClient(MockEngine(handler)) {
            defaultKtorConfig(defaultJson()) { "access-token" }
        }
        val ktorfit = Ktorfit.Builder()
            .httpClient(client)
            .baseUrl("https://linktrip.cloud/api/")
            .build()
        return VideoRepositoryImpl(VideoRemoteDataSourceImpl(ktorfit, client), UnusedLocalDataSource)
    }
}

private object UnusedLocalDataSource : VideoAnalysisLocalDataSource {
    override fun observePendingTaskId(): Flow<String?> = error("Not used in this test")
    override suspend fun savePendingTaskId(taskId: String, excludedTripPlanIds: Set<String>) =
        error("Not used in this test")
    override suspend fun getExcludedTripPlanIds(): Set<String> = error("Not used in this test")
    override suspend fun clearPendingTaskId(expectedTaskId: String) = error("Not used in this test")
}

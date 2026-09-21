package com.linkit.company.feature.explore

import android.os.Looper
import com.linkit.company.domain.exception.LinkTripApiException
import com.linkit.company.domain.exception.LinkTripErrorCode
import com.linkit.company.domain.model.auth.Auth
import com.linkit.company.domain.model.common.CursorPage
import com.linkit.company.domain.model.video.DiscoverChannel
import com.linkit.company.domain.model.video.DiscoverCountry
import com.linkit.company.domain.model.video.DiscoverVideo
import com.linkit.company.domain.model.video.VideoAnalysis
import com.linkit.company.domain.model.video.YouTubeVideoMetadata
import com.linkit.company.domain.repository.AuthRepository
import com.linkit.company.domain.repository.VideoRepository
import com.linkit.company.domain.usecase.EnsureAuthenticatedUseCase
import com.linkit.company.domain.usecase.GetExploreCatalogUseCase
import com.linkit.company.domain.usecase.GetExploreVideosUseCase
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ExploreViewModelTest {
    @Test
    fun loadsRealCountriesChannelsAndVideosAndCreatorSelectionChangesRecentVideos() {
        val repository = ExploreVideoRepositoryFake()
        val viewModel = viewModel(repository)
        idle()

        assertEquals(repository.countries, viewModel.uiState.value.countries)
        assertEquals(repository.videos, viewModel.uiState.value.videos)
        assertFalse(viewModel.uiState.value.isLoading)
        viewModel.onIntent(ExploreIntent.SelectChannel("creator2"))
        assertEquals("recent-creator2", viewModel.uiState.value.selectedChannel?.recentVideos?.single()?.videoId)
    }

    @Test
    fun filterChangesClearTheOtherConditionAndIgnoreLatePreviousResponse() {
        val repository = ExploreVideoRepositoryFake()
        val viewModel = viewModel(repository)
        idle()
        val gate = CompletableDeferred<Unit>()
        repository.countryGate = gate
        viewModel.onIntent(ExploreIntent.SelectCountry("일본"))
        idle()
        assertTrue(viewModel.uiState.value.isLoading)

        viewModel.onIntent(ExploreIntent.SelectRegion("유럽"))
        idle()
        gate.complete(Unit)
        idle()

        assertNull(viewModel.uiState.value.selectedCountry)
        assertEquals("유럽", viewModel.uiState.value.selectedRegion)
        assertEquals(listOf("region-video"), viewModel.uiState.value.videos.map { it.videoId })
        assertTrue(repository.calls.contains("country:일본"))
        assertTrue(repository.calls.contains("region:유럽"))
    }

    @Test
    fun themePaginationDeduplicatesVideosAndStopsRepeatedCursor() {
        val repository = ExploreVideoRepositoryFake()
        val viewModel = viewModel(repository)
        idle()
        viewModel.onIntent(ExploreIntent.SelectTab(ExploreTab.THEME))
        viewModel.onIntent(ExploreIntent.SelectTheme(ExploreTheme.FOOD))
        idle()
        assertEquals(listOf("first"), viewModel.uiState.value.videos.map { it.videoId })
        assertTrue(viewModel.uiState.value.hasNext)

        viewModel.onIntent(ExploreIntent.LoadMore)
        idle()

        assertEquals(listOf("first", "second"), viewModel.uiState.value.videos.map { it.videoId })
        assertTrue(repository.calls.contains("theme:맛집여행:next"))
        assertFalse(viewModel.uiState.value.hasNext)
        val requestCount = repository.calls.size
        viewModel.onIntent(ExploreIntent.LoadMore)
        idle()
        assertEquals(requestCount, repository.calls.size)
    }

    @Test
    fun failedNextPageKeepsExistingVideosAndRetryUsesTheSameCursor() {
        val repository = ExploreVideoRepositoryFake()
        val viewModel = viewModel(repository)
        idle()
        viewModel.onIntent(ExploreIntent.SelectTab(ExploreTab.THEME))
        viewModel.onIntent(ExploreIntent.SelectTheme(ExploreTheme.HEALING))
        idle()
        repository.failNextTheme = true
        viewModel.onIntent(ExploreIntent.LoadMore)
        idle()

        assertNotNull(viewModel.uiState.value.errorMessage)
        assertEquals(listOf("first"), viewModel.uiState.value.videos.map { it.videoId })
        viewModel.onIntent(ExploreIntent.RetryVideos)
        idle()
        assertNull(viewModel.uiState.value.errorMessage)
        assertEquals(listOf("first", "second"), viewModel.uiState.value.videos.map { it.videoId })
        assertEquals(2, repository.calls.count { it == "theme:힐링여행:next" })
    }

    @Test
    fun initialErrorCanRetryToEmptyStateAndCatalogFailureDoesNotHideVideos() {
        val repository = ExploreVideoRepositoryFake().apply {
            allError = IllegalStateException("offline")
            allErrorsRemaining = 1
            catalogError = IllegalStateException("catalog offline")
        }
        val viewModel = viewModel(repository)
        idle()
        assertNotNull(viewModel.uiState.value.errorMessage)
        assertNotNull(viewModel.uiState.value.catalogErrorMessage)

        repository.videos = emptyList()
        repository.catalogError = null
        viewModel.onIntent(ExploreIntent.RetryVideos)
        viewModel.onIntent(ExploreIntent.RetryCatalog)
        idle()
        assertNull(viewModel.uiState.value.errorMessage)
        assertNull(viewModel.uiState.value.catalogErrorMessage)
        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(emptyList<DiscoverVideo>(), viewModel.uiState.value.videos)
    }

    @Test
    fun unknown401RefreshesAuthenticationOnceAndThenReturnsAllVideos() = runBlocking {
        val auth = ExploreAuthRepositoryFake()
        val repository = ExploreVideoRepositoryFake().apply {
            allError = LinkTripApiException(LinkTripErrorCode.UNKNOWN, 401, "expired")
            allErrorsRemaining = 1
        }
        val result = GetExploreVideosUseCase(EnsureAuthenticatedUseCase(auth), repository)()
        assertEquals(repository.videos, result.items)
        assertEquals(1, auth.loginCalls)
        assertEquals(1, auth.logoutCalls)
        assertEquals(2, repository.calls.count { it == "all" })
    }

    @Test
    fun repeated401DoesNotRefreshAgainAndConflictingFiltersAreRejectedBeforeNetwork() {
        val auth = ExploreAuthRepositoryFake()
        val repository = ExploreVideoRepositoryFake().apply {
            allError = LinkTripApiException(LinkTripErrorCode.UNKNOWN, 401, "expired")
            allErrorsRemaining = 2
        }
        val useCase = GetExploreVideosUseCase(EnsureAuthenticatedUseCase(auth), repository)
        assertThrows(LinkTripApiException::class.java) { runBlocking { useCase() } }
        assertEquals(1, auth.loginCalls)
        val calls = repository.calls.toList()
        assertThrows(IllegalArgumentException::class.java) { runBlocking { useCase(country = "일본", region = "유럽") } }
        assertEquals(calls, repository.calls)
    }

    @Test
    fun safeLinksOpenAndInvalidUrlsOrPlatformFailureReturnFeedback() {
        val opened = mutableListOf<String>()
        assertTrue(openExploreUrl("https://youtu.be/video") { opened += it })
        assertFalse(openExploreUrl("https://youtube.com.evil.example/video") { opened += it })
        assertFalse(openExploreUrl("javascript:alert(1)") { opened += it })
        assertFalse(openExploreUrl("https://youtu.be/video") { error("No browser") })
        assertEquals(listOf("https://youtu.be/video"), opened)
        assertEquals("1:02:03", formatExploreDuration("PT1H2M3S"))
        assertEquals("12:34", formatExploreDuration("PT12M34S"))
    }

    private fun viewModel(repository: VideoRepository): ExploreViewModel {
        val auth = EnsureAuthenticatedUseCase(ExploreAuthRepositoryFake())
        return ExploreViewModel(GetExploreVideosUseCase(auth, repository), GetExploreCatalogUseCase(auth, repository))
    }

    private fun idle() = shadowOf(Looper.getMainLooper()).idle()
}

private class ExploreAuthRepositoryFake : AuthRepository {
    var loginCalls = 0
    var logoutCalls = 0
    override suspend fun isLoggedIn() = true
    override suspend fun login(): Auth {
        loginCalls += 1
        return Auth("member", "token")
    }
    override suspend fun logout() { logoutCalls += 1 }
}

private class ExploreVideoRepositoryFake : VideoRepository {
    val calls = mutableListOf<String>()
    val countries = exploreFixtureState().countries
    var videos = listOf(exploreVideo("all-video"))
    var allError: Exception? = null
    var allErrorsRemaining = 0
    var catalogError: Exception? = null
    var countryGate: CompletableDeferred<Unit>? = null
    var failNextTheme = false

    override suspend fun getDiscoverCountries(): List<DiscoverCountry> {
        catalogError?.let { throw it }
        return countries
    }
    override suspend fun getDiscoverChannels(): List<DiscoverChannel> = exploreFixtureState().channels
    override suspend fun getDiscoverVideos(): List<DiscoverVideo> {
        calls += "all"
        if (allErrorsRemaining-- > 0) allError?.let { throw it }
        return videos
    }
    override suspend fun getDiscoverVideosByCountry(country: String): List<DiscoverVideo> {
        calls += "country:$country"
        countryGate?.let { gate -> withContext(NonCancellable) { gate.await() } }
        return listOf(exploreVideo("country-video"))
    }
    override suspend fun getDiscoverVideosByRegion(region: String): List<DiscoverVideo> {
        calls += "region:$region"
        return listOf(exploreVideo("region-video"))
    }
    override suspend fun getDiscoverVideosByTheme(theme: String, cursor: String?): CursorPage<DiscoverVideo> {
        calls += "theme:$theme:$cursor"
        if (failNextTheme) {
            failNextTheme = false
            error("offline")
        }
        return CursorPage(
            if (cursor == null) listOf(exploreVideo("first")) else listOf(exploreVideo("first"), exploreVideo("second")),
            nextCursor = "next",
            hasNext = true,
        )
    }
    override fun observePendingVideoAnalysisTaskId() = flowOf<String?>(null)
    override suspend fun savePendingVideoAnalysisTaskId(taskId: String, excludedTripPlanIds: Set<String>) = Unit
    override suspend fun getPendingVideoAnalysisExcludedTripPlanIds(): Set<String> = emptySet()
    override suspend fun clearPendingVideoAnalysisTaskId(expectedTaskId: String) = Unit
    override suspend fun analyzeVideo(youtubeUrl: String): VideoAnalysis = error("Unused")
    override suspend fun getVideoAnalysis(videoAnalysisTaskId: String): VideoAnalysis = error("Unused")
    override suspend fun getYouTubeVideoMetadata(youtubeUrl: String): YouTubeVideoMetadata = error("Unused")
}

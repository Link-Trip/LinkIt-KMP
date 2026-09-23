package com.linkit.company.data.repository

import com.linkit.company.data.datasource.video.VideoRemoteDataSource
import com.linkit.company.data.dto.video.DiscoverChannelResponse
import com.linkit.company.data.dto.video.DiscoverVideoCursorResponse
import com.linkit.company.data.dto.video.DiscoverVideoResponse
import com.linkit.company.data.dto.video.VideoAnalyzeResponse
import com.linkit.company.data.dto.video.YouTubeOEmbedResponse
import kotlin.coroutines.Continuation
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.startCoroutine
import kotlin.test.Test
import kotlin.test.assertEquals

class VideoRepositoryImplTest {

    @Test
    fun onboardingVideosTakeFirstEightOfWholeDiscoverList() = runImmediate {
        val remote = FakeVideoRemoteDataSource(videos = (1..10).map(::video))

        val result = VideoRepositoryImpl(remote).getOnboardingVideos()

        assertEquals((1..8).map { "video-$it" }, result.map { it.videoId })
        assertEquals(listOf<Pair<String?, String?>>(null to null), remote.categoryRequests)
    }

    @Test
    fun onboardingVideosKeepShorterListsAsIs() = runImmediate {
        val remote = FakeVideoRemoteDataSource(videos = (1..3).map(::video))

        val result = VideoRepositoryImpl(remote).getOnboardingVideos()

        assertEquals(3, result.size)
    }

    private fun video(index: Int) = DiscoverVideoResponse(
        videoId = "video-$index",
        videoUrl = "https://youtu.be/video-$index",
        title = "title $index",
        thumbnailUrl = "https://img/$index.jpg",
        channelId = "channel",
        channelTitle = "channel",
        duration = "PT10M",
        publishedAt = "2026-09-21T00:00:00Z",
    )

    private fun runImmediate(block: suspend () -> Unit) {
        var outcome: Result<Unit>? = null
        block.startCoroutine(
            object : Continuation<Unit> {
                override val context = EmptyCoroutineContext
                override fun resumeWith(result: Result<Unit>) {
                    outcome = result
                }
            },
        )
        checkNotNull(outcome) { "coroutine did not complete immediately" }.getOrThrow()
    }
}

private class FakeVideoRemoteDataSource(
    private val videos: List<DiscoverVideoResponse>,
) : VideoRemoteDataSource {
    val categoryRequests = mutableListOf<Pair<String?, String?>>()

    override suspend fun analyzeVideo(youtubeUrl: String): VideoAnalyzeResponse = error("Not used")

    override suspend fun getVideoAnalysis(videoAnalysisTaskId: String): VideoAnalyzeResponse = error("Not used")

    override suspend fun getYouTubeVideoMetadata(youtubeUrl: String): YouTubeOEmbedResponse = error("Not used")

    override suspend fun getDiscoverVideosByTheme(theme: String, cursor: String?): DiscoverVideoCursorResponse =
        error("Not used")

    override suspend fun getDiscoverChannels(): List<DiscoverChannelResponse> = error("Not used")

    override suspend fun getDiscoverVideosByCategory(country: String?, region: String?): List<DiscoverVideoResponse> {
        categoryRequests += country to region
        return videos
    }
}

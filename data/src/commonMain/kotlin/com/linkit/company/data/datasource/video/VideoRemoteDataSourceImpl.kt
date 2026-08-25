package com.linkit.company.data.datasource.video

import com.linkit.company.data.DataScope
import com.linkit.company.data.api.VideoApi
import com.linkit.company.data.dto.video.DiscoverChannelResponse
import com.linkit.company.data.dto.video.DiscoverVideoCursorResponse
import com.linkit.company.data.dto.video.DiscoverVideoResponse
import com.linkit.company.data.dto.video.VideoAnalyzeRequest
import com.linkit.company.data.dto.video.VideoAnalyzeResponse
import com.linkit.company.data.dto.video.YouTubeOEmbedResponse
import de.jensklingenberg.ktorfit.Ktorfit
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

@Inject
@ContributesBinding(DataScope::class)
class VideoRemoteDataSourceImpl(
    ktorfit: Ktorfit,
    private val httpClient: HttpClient,
) : VideoRemoteDataSource {

    private val api = ktorfit.create<VideoApi>()

    override suspend fun analyzeVideo(youtubeUrl: String): VideoAnalyzeResponse {
        val response = api.analyzeVideo(VideoAnalyzeRequest(youtubeUrl = youtubeUrl))
        return checkNotNull(response.data) { "video/analyze 응답에 data가 없습니다" }
    }

    override suspend fun getVideoAnalysis(videoAnalysisTaskId: String): VideoAnalyzeResponse {
        val response = api.getVideoAnalysis(videoAnalysisTaskId)
        return checkNotNull(response.data) { "video/schedule/$videoAnalysisTaskId 응답에 data가 없습니다" }
    }

    override suspend fun getYouTubeVideoMetadata(youtubeUrl: String): YouTubeOEmbedResponse {
        return httpClient.get(YouTubeOEmbedUrl) {
            parameter("url", youtubeUrl)
            parameter("format", "json")
        }.body()
    }

    override suspend fun getDiscoverVideosByTheme(
        theme: String,
        cursor: String?,
    ): DiscoverVideoCursorResponse {
        val response = api.getDiscoverVideosByTheme(theme = theme, cursor = cursor)
        return checkNotNull(response.data) { "video/discover/theme 응답에 data가 없습니다" }
    }

    override suspend fun getDiscoverChannels(): List<DiscoverChannelResponse> {
        return api.getDiscoverChannels().data?.channels.orEmpty()
    }

    override suspend fun getDiscoverVideosByCategory(
        country: String?,
        region: String?,
    ): List<DiscoverVideoResponse> {
        return api.getDiscoverVideosByCategory(country = country, region = region).data?.videos.orEmpty()
    }
}

private const val YouTubeOEmbedUrl = "https://www.youtube.com/oembed"

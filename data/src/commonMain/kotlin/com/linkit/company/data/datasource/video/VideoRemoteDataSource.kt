package com.linkit.company.data.datasource.video

import com.linkit.company.data.dto.video.DiscoverChannelResponse
import com.linkit.company.data.dto.video.DiscoverVideoCursorResponse
import com.linkit.company.data.dto.video.DiscoverVideoResponse
import com.linkit.company.data.dto.video.VideoAnalyzeResponse

interface VideoRemoteDataSource {

    suspend fun analyzeVideo(youtubeUrl: String): VideoAnalyzeResponse

    suspend fun getVideoAnalysis(videoAnalysisTaskId: String): VideoAnalyzeResponse

    suspend fun getDiscoverVideosByTheme(theme: String, cursor: String?): DiscoverVideoCursorResponse

    suspend fun getDiscoverChannels(): List<DiscoverChannelResponse>

    suspend fun getDiscoverVideosByCategory(country: String?, region: String?): List<DiscoverVideoResponse>
}

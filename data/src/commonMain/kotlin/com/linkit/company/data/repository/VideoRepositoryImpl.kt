package com.linkit.company.data.repository

import com.linkit.company.data.DataScope
import com.linkit.company.data.datasource.video.VideoAnalysisLocalDataSource
import com.linkit.company.data.datasource.video.VideoRemoteDataSource
import com.linkit.company.data.mapper.toDomain
import com.linkit.company.domain.model.common.CursorPage
import com.linkit.company.domain.model.video.DiscoverChannel
import com.linkit.company.domain.model.video.DiscoverCountry
import com.linkit.company.domain.model.video.DiscoverVideo
import com.linkit.company.domain.model.video.VideoAnalysis
import com.linkit.company.domain.model.video.YouTubeVideoMetadata
import com.linkit.company.domain.repository.VideoRepository
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow

@Inject
@ContributesBinding(DataScope::class)
class VideoRepositoryImpl(
    private val videoRemoteDataSource: VideoRemoteDataSource,
    private val videoAnalysisLocalDataSource: VideoAnalysisLocalDataSource,
) : VideoRepository {

    override suspend fun analyzeVideo(youtubeUrl: String): VideoAnalysis {
        return videoRemoteDataSource.analyzeVideo(youtubeUrl).toDomain()
    }

    override suspend fun getVideoAnalysis(videoAnalysisTaskId: String): VideoAnalysis {
        return videoRemoteDataSource.getVideoAnalysis(videoAnalysisTaskId).toDomain()
    }

    override fun observePendingVideoAnalysisTaskId(): Flow<String?> =
        videoAnalysisLocalDataSource.observePendingTaskId()

    override suspend fun savePendingVideoAnalysisTaskId(taskId: String, excludedTripPlanIds: Set<String>) {
        videoAnalysisLocalDataSource.savePendingTaskId(taskId, excludedTripPlanIds)
    }

    override suspend fun getPendingVideoAnalysisExcludedTripPlanIds(): Set<String> =
        videoAnalysisLocalDataSource.getExcludedTripPlanIds()

    override suspend fun clearPendingVideoAnalysisTaskId(expectedTaskId: String) {
        videoAnalysisLocalDataSource.clearPendingTaskId(expectedTaskId)
    }

    override suspend fun getYouTubeVideoMetadata(youtubeUrl: String): YouTubeVideoMetadata {
        return videoRemoteDataSource.getYouTubeVideoMetadata(youtubeUrl).toDomain()
    }

    override suspend fun getDiscoverVideosByTheme(
        theme: String,
        cursor: String?,
    ): CursorPage<DiscoverVideo> {
        return videoRemoteDataSource.getDiscoverVideosByTheme(theme = theme, cursor = cursor).toDomain()
    }

    override suspend fun getDiscoverChannels(): List<DiscoverChannel> {
        return videoRemoteDataSource.getDiscoverChannels().map { it.toDomain() }
    }

    override suspend fun getDiscoverCountries(): List<DiscoverCountry> {
        return videoRemoteDataSource.getDiscoverCountries().map { it.toDomain() }
    }

    override suspend fun getDiscoverVideos(): List<DiscoverVideo> {
        return videoRemoteDataSource.getDiscoverVideosByCategory(country = null, region = null)
            .map { it.toDomain() }
    }

    override suspend fun getDiscoverVideosByCountry(country: String): List<DiscoverVideo> {
        return videoRemoteDataSource.getDiscoverVideosByCategory(country = country, region = null)
            .map { it.toDomain() }
    }

    override suspend fun getDiscoverVideosByRegion(region: String): List<DiscoverVideo> {
        return videoRemoteDataSource.getDiscoverVideosByCategory(country = null, region = region)
            .map { it.toDomain() }
    }
}

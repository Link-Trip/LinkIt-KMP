package com.linkit.company.domain.repository

import com.linkit.company.domain.model.common.CursorPage
import com.linkit.company.domain.model.video.DiscoverChannel
import com.linkit.company.domain.model.video.DiscoverVideo
import com.linkit.company.domain.model.video.VideoAnalysis
import com.linkit.company.domain.model.video.YouTubeVideoMetadata
import kotlinx.coroutines.flow.Flow

interface VideoRepository {

    /**
     * YouTube 영상 분석을 요청한다.
     *
     * 반환값의 [VideoAnalysis.isInProgress]가 true면 비동기 분석이 진행 중이므로
     * [getVideoAnalysis]로 결과를 폴링해야 한다. 이미 분석된 영상이면 결과가 인라인 반환된다.
     */
    suspend fun analyzeVideo(youtubeUrl: String): VideoAnalysis

    suspend fun getVideoAnalysis(videoAnalysisTaskId: String): VideoAnalysis

    /** 사용자에게 완료/실패를 알릴 때까지 분석 작업 ID를 유지한다. */
    fun observePendingVideoAnalysisTaskId(): Flow<String?>

    suspend fun savePendingVideoAnalysisTaskId(taskId: String, excludedTripPlanIds: Set<String> = emptySet())

    suspend fun getPendingVideoAnalysisExcludedTripPlanIds(): Set<String>

    /** 이전 작업의 늦은 확인 이벤트가 새 작업을 지우지 않도록 ID가 일치할 때만 지운다. */
    suspend fun clearPendingVideoAnalysisTaskId(expectedTaskId: String)

    suspend fun getYouTubeVideoMetadata(youtubeUrl: String): YouTubeVideoMetadata

    suspend fun getDiscoverVideosByTheme(theme: String, cursor: String?): CursorPage<DiscoverVideo>

    suspend fun getDiscoverChannels(): List<DiscoverChannel>

    // country와 region은 서버에서 동시 전달 시 400 에러 — 메서드를 분리해 시그니처로 차단한다
    suspend fun getDiscoverVideosByCountry(country: String): List<DiscoverVideo>

    suspend fun getDiscoverVideosByRegion(region: String): List<DiscoverVideo>
}

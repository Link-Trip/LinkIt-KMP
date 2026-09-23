package com.linkit.company.domain.repository

import com.linkit.company.domain.model.common.CursorPage
import com.linkit.company.domain.model.video.DiscoverChannel
import com.linkit.company.domain.model.video.DiscoverVideo
import com.linkit.company.domain.model.video.VideoAnalysis
import com.linkit.company.domain.model.video.YouTubeVideoMetadata

interface VideoRepository {

    /**
     * YouTube 영상 분석을 요청한다.
     *
     * 반환값의 [VideoAnalysis.isInProgress]가 true면 비동기 분석이 진행 중이므로
     * [getVideoAnalysis]로 결과를 폴링해야 한다. 이미 분석된 영상이면 결과가 인라인 반환된다.
     */
    suspend fun analyzeVideo(youtubeUrl: String): VideoAnalysis

    suspend fun getVideoAnalysis(videoAnalysisTaskId: String): VideoAnalysis

    suspend fun getYouTubeVideoMetadata(youtubeUrl: String): YouTubeVideoMetadata

    suspend fun getDiscoverVideosByTheme(theme: String, cursor: String?): CursorPage<DiscoverVideo>

    suspend fun getDiscoverChannels(): List<DiscoverChannel>

    // country와 region은 서버에서 동시 전달 시 400 에러 — 메서드를 분리해 시그니처로 차단한다
    suspend fun getDiscoverVideosByCountry(country: String): List<DiscoverVideo>

    suspend fun getDiscoverVideosByRegion(region: String): List<DiscoverVideo>

    /** 온보딩 추천 영상: 전체 탐색 영상 목록(파라미터 없음) 중 앞에서 8개. 사전 분석은 운영이 보장한다 (research R3). */
    suspend fun getOnboardingVideos(): List<DiscoverVideo>
}

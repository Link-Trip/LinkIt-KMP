package com.linkit.company.domain.model.video

data class VideoAnalysis(
    val id: String,
    val youtubeUrl: String,
    val isValid: Boolean,
    val status: VideoAnalysisStatus,
    val summary: String,
    val estimatedMinCost: Int?,
    val estimatedMaxCost: Int?,
    val costBasis: CostBasis?,
    val placeEnrichmentCompleted: Boolean,
    val timelines: List<VideoTimeline>,
    val itineraryItems: List<ScheduleItem>,
) {
    /** 분석 진행 중 여부 — true면 결과 조회를 폴링해야 한다 (HTTP 202 대응) */
    val isInProgress: Boolean
        get() = status == VideoAnalysisStatus.PENDING
}

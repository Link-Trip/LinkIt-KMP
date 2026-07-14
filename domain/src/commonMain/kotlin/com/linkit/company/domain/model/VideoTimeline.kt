package com.linkit.company.domain.model

/** 영상 주요 장면의 타임스탬프 + YouTube 딥링크 */
data class VideoTimeline(
    val timestampSeconds: Int,
    val timestamp: String,
    val timestampUrl: String,
    val description: String,
)

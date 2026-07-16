package com.linkit.company.domain.model.video

/** 채널의 최신 영상 요약 */
data class RecentVideo(
    val videoId: String,
    val title: String,
    val thumbnailUrl: String,
    val publishedAt: String,
    val videoUrl: String,
)

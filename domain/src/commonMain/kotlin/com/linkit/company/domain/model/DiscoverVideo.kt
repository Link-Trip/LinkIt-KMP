package com.linkit.company.domain.model

/** 탐색 화면에 노출되는 큐레이션 여행 영상 */
data class DiscoverVideo(
    val videoId: String,
    val videoUrl: String,
    val title: String,
    val description: String,
    val thumbnailUrl: String,
    val channelId: String,
    val channelTitle: String,
    val viewCount: Long,
    val likeCount: Long,
    val duration: String,
    val publishedAt: String,
    val region: String,
    val country: String,
    val city: String,
    val theme: String,
)

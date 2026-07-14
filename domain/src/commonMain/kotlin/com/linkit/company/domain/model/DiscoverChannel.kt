package com.linkit.company.domain.model

/** 큐레이션된 여행 유튜브 채널 */
data class DiscoverChannel(
    val channelId: String,
    val title: String,
    val description: String,
    val thumbnailUrl: String,
    val subscriberCount: Long,
    val videoCount: Long,
    val recentVideos: List<RecentVideo>,
)

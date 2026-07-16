package com.linkit.company.data.dto.video

import kotlinx.serialization.Serializable

@Serializable
data class DiscoverChannelResponse(
    val channelId: String,
    val title: String,
    val description: String? = null,
    val thumbnailUrl: String,
    val subscriberCount: Long = 0,
    val videoCount: Long = 0,
    val recentVideos: List<RecentVideoResponse> = emptyList(),
)

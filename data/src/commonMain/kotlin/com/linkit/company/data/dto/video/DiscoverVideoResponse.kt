package com.linkit.company.data.dto.video

import kotlinx.serialization.Serializable

@Serializable
data class DiscoverVideoResponse(
    val videoId: String,
    val videoUrl: String,
    val title: String,
    val description: String? = null,
    val thumbnailUrl: String,
    val channelId: String,
    val channelTitle: String,
    val viewCount: Long = 0,
    val likeCount: Long = 0,
    val duration: String,
    val publishedAt: String,
    val region: String? = null,
    val country: String? = null,
    val city: String? = null,
    val theme: String? = null,
)

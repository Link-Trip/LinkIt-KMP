package com.linkit.company.data.dto.video

import kotlinx.serialization.Serializable

@Serializable
data class RecentVideoResponse(
    val videoId: String,
    val title: String,
    val thumbnailUrl: String,
    val publishedAt: String,
    val videoUrl: String,
)

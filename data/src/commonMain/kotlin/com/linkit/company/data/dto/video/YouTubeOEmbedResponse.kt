package com.linkit.company.data.dto.video

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class YouTubeOEmbedResponse(
    val title: String,
    @SerialName("thumbnail_url")
    val thumbnailUrl: String,
)

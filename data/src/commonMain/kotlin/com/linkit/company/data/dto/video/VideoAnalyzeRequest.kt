package com.linkit.company.data.dto.video

import kotlinx.serialization.Serializable

@Serializable
internal data class VideoAnalyzeRequest(
    val youtubeUrl: String,
)

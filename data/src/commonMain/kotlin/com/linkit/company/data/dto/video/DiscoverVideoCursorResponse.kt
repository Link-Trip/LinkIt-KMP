package com.linkit.company.data.dto.video

import kotlinx.serialization.Serializable

@Serializable
data class DiscoverVideoCursorResponse(
    val videos: List<DiscoverVideoResponse> = emptyList(),
    val nextCursor: String? = null,
    val hasNext: Boolean = false,
)

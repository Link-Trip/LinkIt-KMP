package com.linkit.company.data.dto.video

import kotlinx.serialization.Serializable

@Serializable
data class TimelineResponse(
    val timestampSeconds: Int,
    val timestamp: String,
    val timestampUrl: String,
    val description: String,
)

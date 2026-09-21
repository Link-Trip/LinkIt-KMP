package com.linkit.company.data.dto.member

import kotlinx.serialization.Serializable

@Serializable
internal data class FcmTokenRequest(
    val fcmToken: String,
    val platform: String,
)

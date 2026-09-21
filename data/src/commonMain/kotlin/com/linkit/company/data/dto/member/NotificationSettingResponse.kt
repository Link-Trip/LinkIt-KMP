package com.linkit.company.data.dto.member

import kotlinx.serialization.Serializable

@Serializable
data class NotificationSettingResponse(
    val enabled: Boolean,
)

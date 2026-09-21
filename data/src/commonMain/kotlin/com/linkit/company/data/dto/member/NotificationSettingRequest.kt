package com.linkit.company.data.dto.member

import kotlinx.serialization.Serializable

@Serializable
internal data class NotificationSettingRequest(
    val enabled: Boolean,
)

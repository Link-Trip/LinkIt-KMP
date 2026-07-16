package com.linkit.company.data.dto.auth

import kotlinx.serialization.Serializable

@Serializable
internal data class AuthRequest(
    val serialNumber: String,
)

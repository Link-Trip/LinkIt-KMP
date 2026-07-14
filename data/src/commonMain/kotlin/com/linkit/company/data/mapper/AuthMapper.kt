package com.linkit.company.data.mapper

import com.linkit.company.data.dto.auth.AuthResponse
import com.linkit.company.domain.model.Auth

internal fun AuthResponse.toDomain(): Auth {
    return Auth(
        memberId = memberId,
        accessToken = accessToken,
    )
}

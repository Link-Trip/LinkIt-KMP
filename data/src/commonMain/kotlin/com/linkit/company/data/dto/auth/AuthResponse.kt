package com.linkit.company.data.dto.auth

import kotlinx.serialization.Serializable

/**
 * public인 이유: DataSource 인터페이스 시그니처에 노출되는 응답 DTO는
 * iOS 수동 DI 등록(IosAppGraph) 경로에서 타입 해석이 가능해야 한다.
 */
@Serializable
data class AuthResponse(
    val memberId: String,
    val accessToken: String,
)

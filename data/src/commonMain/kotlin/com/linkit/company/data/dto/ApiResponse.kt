package com.linkit.company.data.dto

import kotlinx.serialization.Serializable

/**
 * 성공(2xx) 응답의 공통 래퍼.
 *
 * [status]는 HTTP 코드와 별개로 바디에 담기는 의미상 코드다.
 * (예: 로그인 API는 HTTP 200이어도 신규 회원가입이면 status=201)
 */
@Serializable
internal data class ApiResponse<T>(
    val status: Int,
    val message: String,
    val data: T? = null,
)

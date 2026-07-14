package com.linkit.company.data.dto

import kotlinx.serialization.Serializable

/** 실패(4xx/5xx) 응답 바디. 성공 응답과 스키마가 다르다. */
@Serializable
internal data class ErrorResponse(
    val code: String,
    val message: String,
)

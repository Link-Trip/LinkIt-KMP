package com.linkit.company.data.dto.terms

import kotlinx.serialization.Serializable

/** `GET /terms` 응답 `data` (서버 스키마 `TermsResponses`). */
@Serializable
data class TermsListResponse(
    val terms: List<TermsResponse> = emptyList(),
)

package com.linkit.company.data.dto.terms

import kotlinx.serialization.Serializable

/** `GET /terms` 항목. [type]은 서버 enum 문자열(`SERVICE`, `PRIVACY`)이며 매퍼가 도메인 enum으로 바꾼다. */
@Serializable
data class TermsResponse(
    val type: String,
    val title: String,
    val required: Boolean,
    val version: Int,
    val detailUrl: String,
    val agreed: Boolean,
)

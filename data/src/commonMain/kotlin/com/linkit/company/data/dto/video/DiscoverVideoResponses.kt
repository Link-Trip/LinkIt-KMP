package com.linkit.company.data.dto.video

import kotlinx.serialization.Serializable

/** 카테고리별 탐색 응답 래퍼 — DataSource에서 내부 리스트로 언래핑된다 */
@Serializable
internal data class DiscoverVideoResponses(
    val videos: List<DiscoverVideoResponse> = emptyList(),
)

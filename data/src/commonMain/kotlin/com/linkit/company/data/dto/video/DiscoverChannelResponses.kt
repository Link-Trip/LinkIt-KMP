package com.linkit.company.data.dto.video

import kotlinx.serialization.Serializable

/** 채널 목록 응답 래퍼 — DataSource에서 내부 리스트로 언래핑된다 */
@Serializable
internal data class DiscoverChannelResponses(
    val channels: List<DiscoverChannelResponse> = emptyList(),
)

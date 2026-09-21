package com.linkit.company.data.dto.feedback

import kotlinx.serialization.Serializable

/**
 * 의견 전송 요청. 서버 상한: content 200, appVersion 20, osVersion 20, deviceModel 50.
 *
 * @property type `SUGGESTION | BUG | ETC`
 * @property platform `IOS | ANDROID`
 */
@Serializable
internal data class CreateFeedbackRequest(
    val type: String,
    val content: String,
    val appVersion: String,
    val platform: String,
    val osVersion: String,
    val deviceModel: String,
)

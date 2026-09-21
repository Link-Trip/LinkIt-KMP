package com.linkit.company.domain.repository

import com.linkit.company.domain.model.app.AppInfo
import com.linkit.company.domain.model.feedback.FeedbackType

interface FeedbackRepository {

    /**
     * 의견을 서버에 전송한다.
     *
     * 하루 5회를 초과하면 `LinkTripApiException(FEEDBACK_DAILY_LIMIT_EXCEEDED)`가 던져진다.
     */
    suspend fun sendFeedback(type: FeedbackType, content: String, appInfo: AppInfo)
}

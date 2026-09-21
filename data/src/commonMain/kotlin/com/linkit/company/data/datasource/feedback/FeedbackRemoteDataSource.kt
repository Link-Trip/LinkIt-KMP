package com.linkit.company.data.datasource.feedback

interface FeedbackRemoteDataSource {
    suspend fun createFeedback(
        type: String,
        content: String,
        appVersion: String,
        platform: String,
        osVersion: String,
        deviceModel: String,
    )
}

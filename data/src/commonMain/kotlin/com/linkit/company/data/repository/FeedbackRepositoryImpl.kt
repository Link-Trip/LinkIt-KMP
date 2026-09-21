package com.linkit.company.data.repository

import com.linkit.company.data.DataScope
import com.linkit.company.data.datasource.feedback.FeedbackRemoteDataSource
import com.linkit.company.domain.model.app.AppInfo
import com.linkit.company.domain.model.feedback.FeedbackType
import com.linkit.company.domain.repository.FeedbackRepository
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject

@Inject
@ContributesBinding(DataScope::class)
class FeedbackRepositoryImpl(
    private val feedbackRemoteDataSource: FeedbackRemoteDataSource,
) : FeedbackRepository {

    override suspend fun sendFeedback(type: FeedbackType, content: String, appInfo: AppInfo) {
        // 서버 문자열 상한(appVersion·osVersion 20, deviceModel 50)을 넘기면 400이므로 잘라 보낸다
        feedbackRemoteDataSource.createFeedback(
            type = type.name,
            content = content,
            appVersion = appInfo.appVersion.take(MaxVersionLength),
            platform = appInfo.platform,
            osVersion = appInfo.osVersion.take(MaxVersionLength),
            deviceModel = appInfo.deviceModel.take(MaxDeviceModelLength),
        )
    }

    private companion object {
        const val MaxVersionLength = 20
        const val MaxDeviceModelLength = 50
    }
}

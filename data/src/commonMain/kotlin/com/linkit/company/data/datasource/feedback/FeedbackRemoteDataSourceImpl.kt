package com.linkit.company.data.datasource.feedback

import com.linkit.company.data.DataScope
import com.linkit.company.data.api.FeedbackApi
import com.linkit.company.data.dto.feedback.CreateFeedbackRequest
import de.jensklingenberg.ktorfit.Ktorfit
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject

@Inject
@ContributesBinding(DataScope::class)
class FeedbackRemoteDataSourceImpl(
    ktorfit: Ktorfit,
) : FeedbackRemoteDataSource {

    private val api = ktorfit.create<FeedbackApi>()

    override suspend fun createFeedback(
        type: String,
        content: String,
        appVersion: String,
        platform: String,
        osVersion: String,
        deviceModel: String,
    ) {
        api.createFeedback(
            CreateFeedbackRequest(
                type = type,
                content = content,
                appVersion = appVersion,
                platform = platform,
                osVersion = osVersion,
                deviceModel = deviceModel,
            ),
        )
    }
}

package com.linkit.company.data.api

import com.linkit.company.data.dto.ApiResponse
import com.linkit.company.data.dto.feedback.CreateFeedbackRequest
import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.Headers
import de.jensklingenberg.ktorfit.http.POST

internal interface FeedbackApi {

    /** 의견 전송. 하루(KST) 5회 초과 시 429 `FEEDBACK_DAILY_LIMIT_EXCEEDED`. */
    @POST("feedback")
    @Headers("Content-Type: application/json")
    suspend fun createFeedback(@Body request: CreateFeedbackRequest): ApiResponse<Unit>
}

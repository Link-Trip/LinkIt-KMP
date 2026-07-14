package com.linkit.company.data.api

import com.linkit.company.data.dto.ApiResponse
import com.linkit.company.data.dto.video.DiscoverChannelResponses
import com.linkit.company.data.dto.video.DiscoverVideoCursorResponse
import com.linkit.company.data.dto.video.DiscoverVideoResponses
import com.linkit.company.data.dto.video.VideoAnalyzeRequest
import com.linkit.company.data.dto.video.VideoAnalyzeResponse
import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Headers
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.Path
import de.jensklingenberg.ktorfit.http.Query

internal interface VideoApi {

    @POST("video/analyze")
    @Headers("Content-Type: application/json")
    suspend fun analyzeVideo(@Body request: VideoAnalyzeRequest): ApiResponse<VideoAnalyzeResponse>

    @GET("video/schedule/{videoAnalysisTaskId}")
    suspend fun getVideoAnalysis(
        @Path("videoAnalysisTaskId") videoAnalysisTaskId: String,
    ): ApiResponse<VideoAnalyzeResponse>

    @GET("video/discover/theme")
    suspend fun getDiscoverVideosByTheme(
        @Query("theme") theme: String,
        @Query("cursor") cursor: String?,
    ): ApiResponse<DiscoverVideoCursorResponse>

    @GET("video/discover/channels")
    suspend fun getDiscoverChannels(): ApiResponse<DiscoverChannelResponses>

    @GET("video/discover/category")
    suspend fun getDiscoverVideosByCategory(
        @Query("country") country: String?,
        @Query("region") region: String?,
    ): ApiResponse<DiscoverVideoResponses>
}

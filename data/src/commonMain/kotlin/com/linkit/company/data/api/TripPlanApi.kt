package com.linkit.company.data.api

import com.linkit.company.data.dto.ApiResponse
import com.linkit.company.data.dto.tripplan.TripPlanCursorResponse
import com.linkit.company.data.dto.tripplan.TripPlanDetailResponse
import com.linkit.company.data.dto.tripplan.UpdateTripPlanRequest
import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.DELETE
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Headers
import de.jensklingenberg.ktorfit.http.PUT
import de.jensklingenberg.ktorfit.http.Path
import de.jensklingenberg.ktorfit.http.Query

internal interface TripPlanApi {

    @GET("trip-plans")
    suspend fun getTripPlans(@Query("cursor") cursor: String?): ApiResponse<TripPlanCursorResponse>

    @GET("trip-plans/{tripPlanId}")
    suspend fun getTripPlan(@Path("tripPlanId") tripPlanId: String): ApiResponse<TripPlanDetailResponse>

    @PUT("trip-plans/{tripPlanId}")
    @Headers("Content-Type: application/json")
    suspend fun updateTripPlan(
        @Path("tripPlanId") tripPlanId: String,
        @Body request: UpdateTripPlanRequest,
    ): ApiResponse<TripPlanDetailResponse>

    @DELETE("trip-plans/{tripPlanId}")
    suspend fun deleteTripPlan(@Path("tripPlanId") tripPlanId: String): ApiResponse<Unit>
}

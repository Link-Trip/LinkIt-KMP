package com.linkit.company.data.api

import com.linkit.company.data.dto.ApiResponse
import com.linkit.company.data.dto.auth.AuthRequest
import com.linkit.company.data.dto.auth.AuthResponse
import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.Headers
import de.jensklingenberg.ktorfit.http.POST

internal interface AuthApi {

    @POST("auth/login")
    @Headers("Content-Type: application/json")
    suspend fun login(@Body request: AuthRequest): ApiResponse<AuthResponse>
}

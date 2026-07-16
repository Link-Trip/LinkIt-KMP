package com.linkit.company.data.datasource.auth

import com.linkit.company.data.dto.auth.AuthResponse

interface AuthRemoteDataSource {

    suspend fun login(serialNumber: String): AuthResponse
}

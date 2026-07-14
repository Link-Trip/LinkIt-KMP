package com.linkit.company.data.datasource.auth

import com.linkit.company.data.api.AuthApi
import com.linkit.company.data.dto.auth.AuthRequest
import com.linkit.company.data.dto.auth.AuthResponse
import de.jensklingenberg.ktorfit.Ktorfit
import dev.zacsweers.metro.Inject

@Inject
class AuthRemoteDataSourceImpl(
    ktorfit: Ktorfit,
) : AuthRemoteDataSource {

    private val api = ktorfit.create<AuthApi>()

    override suspend fun login(serialNumber: String): AuthResponse {
        val response = api.login(AuthRequest(serialNumber = serialNumber))
        return checkNotNull(response.data) { "auth/login 응답에 data가 없습니다" }
    }
}

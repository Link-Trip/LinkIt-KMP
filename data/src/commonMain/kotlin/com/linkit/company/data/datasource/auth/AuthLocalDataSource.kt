package com.linkit.company.data.datasource.auth

interface AuthLocalDataSource {

    suspend fun getAccessToken(): String?

    suspend fun saveAccessToken(accessToken: String)

    suspend fun clearAccessToken()
}

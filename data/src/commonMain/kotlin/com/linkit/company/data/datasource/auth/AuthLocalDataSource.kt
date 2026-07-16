package com.linkit.company.data.datasource.auth

interface AuthLocalDataSource {

    suspend fun getAccessToken(): String?

    suspend fun saveAccessToken(accessToken: String)

    suspend fun clearAccessToken()

    /** 최초 확보한 기기 Device ID (앱 설치 후 첫 로그인 시점에 저장) */
    suspend fun getDeviceId(): String?

    suspend fun saveDeviceId(deviceId: String)
}

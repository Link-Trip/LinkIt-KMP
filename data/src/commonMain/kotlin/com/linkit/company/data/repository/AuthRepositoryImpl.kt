package com.linkit.company.data.repository

import com.linkit.company.data.core.DeviceIdProvider
import com.linkit.company.data.datasource.auth.AuthLocalDataSource
import com.linkit.company.data.datasource.auth.AuthRemoteDataSource
import com.linkit.company.data.mapper.toDomain
import com.linkit.company.domain.model.Auth
import com.linkit.company.data.DataScope
import com.linkit.company.domain.repository.AuthRepository
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject

@Inject
@ContributesBinding(DataScope::class)
class AuthRepositoryImpl(
    private val authRemoteDataSource: AuthRemoteDataSource,
    private val authLocalDataSource: AuthLocalDataSource,
    private val deviceIdProvider: DeviceIdProvider,
) : AuthRepository {

    override suspend fun login(): Auth {
        // 최초 확보한 Device ID를 영구 보존해 재사용한다 (플랫폼 식별자 변동에도 계정 유지)
        val deviceId = authLocalDataSource.getDeviceId()
            ?: deviceIdProvider.getDeviceId().also { authLocalDataSource.saveDeviceId(it) }

        val auth = authRemoteDataSource.login(serialNumber = deviceId).toDomain()
        authLocalDataSource.saveAccessToken(auth.accessToken)
        return auth
    }

    override suspend fun isLoggedIn(): Boolean {
        return authLocalDataSource.getAccessToken() != null
    }

    override suspend fun logout() {
        authLocalDataSource.clearAccessToken()
    }
}

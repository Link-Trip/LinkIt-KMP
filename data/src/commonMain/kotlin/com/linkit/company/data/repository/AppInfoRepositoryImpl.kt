package com.linkit.company.data.repository

import com.linkit.company.data.DataScope
import com.linkit.company.data.core.AppInfoProvider
import com.linkit.company.domain.model.app.AppInfo
import com.linkit.company.domain.repository.AppInfoRepository
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject

@Inject
@ContributesBinding(DataScope::class)
class AppInfoRepositoryImpl(
    private val appInfoProvider: AppInfoProvider,
) : AppInfoRepository {

    override suspend fun getAppInfo(): AppInfo {
        val value = appInfoProvider.getAppInfo()
        return AppInfo(
            appVersion = value.appVersion,
            platform = value.platform,
            osVersion = value.osVersion,
            deviceModel = value.deviceModel,
        )
    }
}

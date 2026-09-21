package com.linkit.company.domain.repository

import com.linkit.company.domain.model.app.AppInfo

interface AppInfoRepository {
    suspend fun getAppInfo(): AppInfo
}

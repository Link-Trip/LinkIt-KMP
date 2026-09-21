package com.linkit.company.data.core

/**
 * 플랫폼별 앱·기기 정보를 제공한다. [DeviceIdProvider]와 같이 플랫폼 그래프가 구현을 제공한다.
 * - Android: PackageManager versionName, "ANDROID", Build.VERSION.RELEASE, Build.MODEL
 * - iOS: CFBundleShortVersionString, "IOS", UIDevice.systemVersion, UIDevice.model
 */
fun interface AppInfoProvider {
    fun getAppInfo(): AppInfoValue
}

/** data 내부 값 객체. domain 변환은 [com.linkit.company.data.repository.AppInfoRepositoryImpl]이 담당한다. */
data class AppInfoValue(
    val appVersion: String,
    val platform: String,
    val osVersion: String,
    val deviceModel: String,
)

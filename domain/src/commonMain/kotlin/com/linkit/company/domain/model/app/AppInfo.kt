package com.linkit.company.domain.model.app

/**
 * 의견 전송 시 자동으로 첨부하는 앱·기기 정보.
 *
 * @property platform `"ANDROID"` 또는 `"IOS"` (서버 `platform` enum과 동일)
 */
data class AppInfo(
    val appVersion: String,
    val platform: String,
    val osVersion: String,
    val deviceModel: String,
)

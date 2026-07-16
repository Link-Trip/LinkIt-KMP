package com.linkit.company.data.core

/**
 * 플랫폼별 기기 식별자를 제공한다.
 * - Android: `Settings.Secure.ANDROID_ID`
 * - iOS: `UIDevice.identifierForVendor` (nil이면 생성 UUID)
 *
 * 플랫폼 식별자는 재설치 등으로 변할 수 있으므로 매번 이 값을 쓰지 않는다.
 * 최초 확보한 값을 AuthLocalDataSource(DataStore)에 영구 저장하고 이후에는 저장값을 재사용한다.
 * (조율 책임: AuthRepositoryImpl)
 */
fun interface DeviceIdProvider {
    fun getDeviceId(): String
}

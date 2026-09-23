package com.linkit.company.domain.repository

import com.linkit.company.domain.model.settings.MapDisplayType
import kotlinx.coroutines.flow.Flow

/**
 * 앱 로컬 설정 저장소.
 *
 * 지도 표시 방식, 알림 안내 노출 이력, 앱 알림 수신 설정 캐시를 보관한다.
 * 온보딩 완료·약관 동의·튜토리얼 단계는 [OnboardingRepository] 책임이다.
 * 앱 초기화 시 [clearAll]로 첫 설치 상태로 되돌린다 (인증 정보는 [AuthRepository] 책임).
 */
interface AppSettingsRepository {

    /** 저장값이 없으면 [MapDisplayType.DEFAULT]를 내보낸다. */
    fun observeMapDisplayType(): Flow<MapDisplayType>

    suspend fun setMapDisplayType(type: MapDisplayType)

    suspend fun isNotificationPrompted(): Boolean

    suspend fun setNotificationPrompted(prompted: Boolean)

    /**
     * 앱 알림 수신 설정. 서버 값의 표시용 캐시이며 저장값이 없으면 `true`를 내보낸다.
     * 서버 조회·변경이 성공했을 때만 [setNotificationEnabled]로 갱신한다.
     */
    fun observeNotificationEnabled(): Flow<Boolean>

    suspend fun setNotificationEnabled(enabled: Boolean)

    /** 지도 설정·알림 안내 이력·알림 수신 설정 캐시를 제거한다. */
    suspend fun clearAll()
}

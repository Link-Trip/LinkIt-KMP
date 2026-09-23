package com.linkit.company.domain.repository

import com.linkit.company.domain.model.settings.MapDisplayType
import kotlinx.coroutines.flow.Flow

/**
 * 앱 로컬 설정 저장소.
 *
 * 지도 표시 방식과 알림 안내 노출 이력을 보관한다. 온보딩 완료·약관 동의·튜토리얼 단계는 [OnboardingRepository] 책임이다.
 * 앱 초기화 시 [clearAll]로 첫 설치 상태로 되돌린다 (인증 정보는 [AuthRepository] 책임).
 */
interface AppSettingsRepository {

    /** 저장값이 없으면 [MapDisplayType.DEFAULT]를 내보낸다. */
    fun observeMapDisplayType(): Flow<MapDisplayType>

    suspend fun setMapDisplayType(type: MapDisplayType)

    suspend fun isNotificationPrompted(): Boolean

    suspend fun setNotificationPrompted(prompted: Boolean)

    /** 지도 설정·알림 안내 이력을 제거한다. */
    suspend fun clearAll()
}

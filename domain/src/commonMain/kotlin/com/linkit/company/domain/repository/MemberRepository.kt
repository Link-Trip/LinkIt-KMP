package com.linkit.company.domain.repository

import com.linkit.company.domain.model.member.NotificationSetting

interface MemberRepository {

    /** 회원의 푸시 알림 수신 여부를 서버에 반영하고 변경 후 설정값을 돌려준다. */
    suspend fun updateNotificationSetting(enabled: Boolean): NotificationSetting

    /** 현재 기기의 FCM 토큰을 등록하거나 갱신한다. */
    suspend fun registerFcmToken(fcmToken: String, platform: String)

    /**
     * 회원을 탈퇴 처리한다. 서버가 여행 계획 전체 소프트 삭제·FCM 제거·기기식별자 마스킹을
     * 단일 트랜잭션으로 수행하며, 이미 탈퇴한 회원에 재호출해도 성공한다.
     *
     * @return 삭제된 여행 계획 수
     */
    suspend fun withdraw(): Int
}

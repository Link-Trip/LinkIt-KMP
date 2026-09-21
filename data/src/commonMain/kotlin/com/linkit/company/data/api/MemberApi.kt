package com.linkit.company.data.api

import com.linkit.company.data.dto.ApiResponse
import com.linkit.company.data.dto.member.NotificationSettingRequest
import com.linkit.company.data.dto.member.NotificationSettingResponse
import com.linkit.company.data.dto.member.WithdrawMemberResponse
import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.DELETE
import de.jensklingenberg.ktorfit.http.Headers
import de.jensklingenberg.ktorfit.http.PUT

internal interface MemberApi {

    @PUT("members/me/notification")
    @Headers("Content-Type: application/json")
    suspend fun updateNotificationSetting(
        @Body request: NotificationSettingRequest,
    ): ApiResponse<NotificationSettingResponse>

    /** 회원 탈퇴. 여행 계획 전체 소프트 삭제·FCM 제거·기기식별자 마스킹을 단일 트랜잭션으로 처리한다. */
    @DELETE("members/me")
    suspend fun withdraw(): ApiResponse<WithdrawMemberResponse>
}

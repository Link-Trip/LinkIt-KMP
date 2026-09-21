package com.linkit.company.data.datasource.member

import com.linkit.company.data.dto.member.NotificationSettingResponse
import com.linkit.company.data.dto.member.WithdrawMemberResponse

interface MemberRemoteDataSource {
    suspend fun updateNotificationSetting(enabled: Boolean): NotificationSettingResponse
    suspend fun withdraw(): WithdrawMemberResponse
}

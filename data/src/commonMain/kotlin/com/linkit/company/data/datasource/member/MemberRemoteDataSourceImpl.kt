package com.linkit.company.data.datasource.member

import com.linkit.company.data.DataScope
import com.linkit.company.data.api.MemberApi
import com.linkit.company.data.dto.member.NotificationSettingRequest
import com.linkit.company.data.dto.member.NotificationSettingResponse
import com.linkit.company.data.dto.member.WithdrawMemberResponse
import de.jensklingenberg.ktorfit.Ktorfit
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject

@Inject
@ContributesBinding(DataScope::class)
class MemberRemoteDataSourceImpl(
    ktorfit: Ktorfit,
) : MemberRemoteDataSource {

    private val api = ktorfit.create<MemberApi>()

    override suspend fun getNotificationSetting(): NotificationSettingResponse {
        val response = api.getNotificationSetting()
        return checkNotNull(response.data) { "members/me/notification 조회 응답에 data가 없습니다" }
    }

    override suspend fun updateNotificationSetting(enabled: Boolean): NotificationSettingResponse {
        val response = api.updateNotificationSetting(NotificationSettingRequest(enabled = enabled))
        return checkNotNull(response.data) { "members/me/notification 응답에 data가 없습니다" }
    }

    override suspend fun withdraw(): WithdrawMemberResponse {
        val response = api.withdraw()
        return checkNotNull(response.data) { "members/me 탈퇴 응답에 data가 없습니다" }
    }
}

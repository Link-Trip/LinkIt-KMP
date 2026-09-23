package com.linkit.company.data.repository

import com.linkit.company.data.DataScope
import com.linkit.company.data.datasource.member.MemberRemoteDataSource
import com.linkit.company.data.mapper.toDomain
import com.linkit.company.domain.model.member.NotificationSetting
import com.linkit.company.domain.repository.MemberRepository
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject

@Inject
@ContributesBinding(DataScope::class)
class MemberRepositoryImpl(
    private val memberRemoteDataSource: MemberRemoteDataSource,
) : MemberRepository {

    override suspend fun getNotificationSetting(): NotificationSetting {
        return memberRemoteDataSource.getNotificationSetting().toDomain()
    }

    override suspend fun updateNotificationSetting(enabled: Boolean): NotificationSetting {
        return memberRemoteDataSource.updateNotificationSetting(enabled).toDomain()
    }

    override suspend fun withdraw(): Int {
        return memberRemoteDataSource.withdraw().deletedTripPlanCount
    }
}

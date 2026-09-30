package com.linkit.company.data.datasource.tripplan

import kotlinx.coroutines.flow.Flow

/** 여행 계획의 기기 로컬 상태(`확인전` 집합). 서버 필드가 없어 로컬에만 둔다. */
interface TripPlanLocalDataSource {
    fun observeUncheckedIds(): Flow<Set<String>>
    suspend fun addUncheckedId(id: String)
    suspend fun removeUncheckedId(id: String)
    suspend fun clearUnchecked()
}

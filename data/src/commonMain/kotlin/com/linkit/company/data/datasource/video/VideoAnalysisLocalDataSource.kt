package com.linkit.company.data.datasource.video

import kotlinx.coroutines.flow.Flow

interface VideoAnalysisLocalDataSource {
    fun observePendingTaskId(): Flow<String?>

    suspend fun savePendingTaskId(taskId: String, excludedTripPlanIds: Set<String> = emptySet())

    suspend fun getExcludedTripPlanIds(): Set<String>

    suspend fun clearPendingTaskId(expectedTaskId: String)
}

package com.linkit.company.domain.usecase

import com.linkit.company.domain.repository.VideoRepository
import dev.zacsweers.metro.Inject

/** 사용자가 결과를 확인한 분석 작업만 지운다. */
@Inject
class AcknowledgeVideoScheduleCreationUseCase(
    private val videoRepository: VideoRepository,
) {
    suspend operator fun invoke(taskId: String) {
        videoRepository.clearPendingVideoAnalysisTaskId(taskId)
    }
}

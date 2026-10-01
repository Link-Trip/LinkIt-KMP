package com.linkit.company.domain.usecase

import com.linkit.company.domain.exception.LinkTripApiException
import com.linkit.company.domain.exception.LinkTripErrorCode
import com.linkit.company.domain.model.tripplan.TripPlanSummary
import com.linkit.company.domain.model.video.VideoAnalysisStatus
import com.linkit.company.domain.model.video.VideoScheduleCreationState
import com.linkit.company.domain.repository.TripPlanRepository
import com.linkit.company.domain.repository.VideoRepository
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlin.coroutines.cancellation.CancellationException

/** 화면이 구독하는 동안 저장된 분석 작업을 확인하고 생성된 일정과 연결한다. */
@Inject
class ObserveVideoScheduleCreationUseCase(
    private val ensureAuthenticated: EnsureAuthenticatedUseCase,
    private val videoRepository: VideoRepository,
    private val tripPlanRepository: TripPlanRepository,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(
        pollIntervalMillis: Long = 3_000,
        maxPollAttempts: Int = 100,
    ): Flow<VideoScheduleCreationState> {
        require(pollIntervalMillis >= 0 && maxPollAttempts > 0)
        return videoRepository.observePendingVideoAnalysisTaskId()
            .distinctUntilChanged()
            .flatMapLatest { taskId ->
                if (taskId == null) {
                    flow { emit(VideoScheduleCreationState.Idle) }
                } else {
                    observeTask(taskId, pollIntervalMillis, maxPollAttempts)
                }
            }
            .catch { error ->
                if (error is CancellationException) throw error
                emit(VideoScheduleCreationState.Error(null, "진행 중인 일정 정보를 불러오지 못했어요. 다시 확인해 주세요."))
            }
    }

    private fun observeTask(
        taskId: String,
        pollIntervalMillis: Long,
        maxPollAttempts: Int,
    ): Flow<VideoScheduleCreationState> = flow {
        emit(VideoScheduleCreationState.InProgress(taskId))
        var authenticated = false
        var refreshedAuthentication = false
        var consecutiveErrors = 0

        repeat(maxPollAttempts) { attempt ->
            val state = try {
                if (!authenticated) {
                    ensureAuthenticated()
                    authenticated = true
                }
                try {
                    readState(taskId)
                } catch (error: LinkTripApiException) {
                    val unauthorized = error.errorCode == LinkTripErrorCode.UNAUTHORIZED_AUTHENTICATION_FAILED ||
                        error.httpStatus == 401
                    if (!unauthorized || refreshedAuthentication) {
                        throw error
                    }
                    refreshedAuthentication = true
                    ensureAuthenticated(forceRefresh = true)
                    readState(taskId)
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: LinkTripApiException) {
                if (error.errorCode == LinkTripErrorCode.NOT_FOUND_VIDEO_ANALYSIS_TASK) {
                    VideoScheduleCreationState.Failed(taskId, "분석 중인 영상을 찾을 수 없어요. 다시 시도해 주세요.")
                } else {
                    null
                }
            } catch (_: Exception) {
                null
            }

            if (state == null) {
                consecutiveErrors += 1
                if (consecutiveErrors >= 3) {
                    emit(VideoScheduleCreationState.Error(taskId, "일정 생성 상태를 확인하지 못했어요. 다시 확인해 주세요."))
                    return@flow
                }
            } else {
                consecutiveErrors = 0
                emit(state)
                if (state !is VideoScheduleCreationState.InProgress) return@flow
            }
            if (attempt + 1 < maxPollAttempts) delay(pollIntervalMillis)
        }

        emit(VideoScheduleCreationState.Error(taskId, "일정 생성이 지연되고 있어요. 잠시 후 다시 확인해 주세요."))
    }.distinctUntilChanged()

    private suspend fun readState(taskId: String): VideoScheduleCreationState {
        val analysis = videoRepository.getVideoAnalysis(taskId)
        return when (analysis.status) {
            VideoAnalysisStatus.PENDING, VideoAnalysisStatus.PROCESSING ->
                VideoScheduleCreationState.InProgress(taskId)
            VideoAnalysisStatus.COMPLETED -> {
                if (!analysis.isValid) {
                    VideoScheduleCreationState.Failed(taskId, "일정을 만들 수 없는 영상이에요. 다른 영상을 선택해 주세요.")
                } else {
                    val tripPlan = findCreatedTripPlan(taskId)
                    if (tripPlan == null) {
                        // 분석 완료와 일정 저장이 별도로 반영될 수 있으므로 일정을 찾을 때까지 기다린다.
                        VideoScheduleCreationState.InProgress(taskId)
                    } else {
                        VideoScheduleCreationState.Completed(taskId, tripPlan.id, tripPlan.title)
                    }
                }
            }
            VideoAnalysisStatus.INVALID ->
                VideoScheduleCreationState.Failed(taskId, "일정을 만들 수 없는 영상이에요. 다른 영상을 선택해 주세요.")
            VideoAnalysisStatus.FAILED ->
                VideoScheduleCreationState.Failed(taskId, "영상 분석에 실패했어요. 다시 시도해 주세요.")
            VideoAnalysisStatus.UNKNOWN ->
                VideoScheduleCreationState.Error(taskId, "일정 생성 상태를 확인하지 못했어요. 다시 확인해 주세요.")
        }
    }

    private suspend fun findCreatedTripPlan(taskId: String): TripPlanSummary? {
        val excludedTripPlanIds = videoRepository.getPendingVideoAnalysisExcludedTripPlanIds()
        val requestedCursors = mutableSetOf<String?>(null)
        var cursor: String? = null
        while (true) {
            val page = tripPlanRepository.getTripPlans(cursor)
            page.items.firstOrNull {
                it.videoAnalysisTaskId == taskId && it.id !in excludedTripPlanIds
            }?.let { return it }
            if (!page.hasNext) return null
            val nextCursor = page.nextCursor ?: return null
            if (!requestedCursors.add(nextCursor)) return null
            cursor = nextCursor
        }
    }
}

package com.linkit.company.domain.usecase

import com.linkit.company.domain.exception.LinkTripApiException
import com.linkit.company.domain.exception.LinkTripErrorCode
import com.linkit.company.domain.model.tripplan.TripPlanSummary
import com.linkit.company.domain.model.video.VideoAnalysis
import com.linkit.company.domain.model.video.VideoAnalysisStatus
import com.linkit.company.domain.model.video.YouTubeVideoMetadata
import com.linkit.company.domain.repository.TripPlanRepository
import com.linkit.company.domain.repository.VideoRepository
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.coroutines.cancellation.CancellationException

sealed interface StartVideoScheduleCreationResult {
    data object InvalidFormat : StartVideoScheduleCreationResult

    data class AlreadyInProgress(val taskId: String) : StartVideoScheduleCreationResult

    data class ExistingSchedule(
        val tripPlanId: String,
        val title: String,
    ) : StartVideoScheduleCreationResult

    data class AnalysisStarted(
        val analysis: VideoAnalysis,
        val metadata: YouTubeVideoMetadata?,
    ) : StartVideoScheduleCreationResult
}

/** 영상 링크를 검증하고 기존 일정을 확인한 뒤 영상 분석을 시작한다. */
@Inject
class StartVideoScheduleCreationUseCase(
    private val ensureAuthenticated: EnsureAuthenticatedUseCase,
    private val tripPlanRepository: TripPlanRepository,
    private val videoRepository: VideoRepository,
) {
    suspend operator fun invoke(
        youtubeUrl: String,
        allowDuplicate: Boolean = false,
    ): StartVideoScheduleCreationResult = creationMutex.withLock {
        val normalizedUrl = youtubeUrl.trim()
        val videoId = normalizedUrl.youtubeVideoIdOrNull()
            ?: return@withLock StartVideoScheduleCreationResult.InvalidFormat

        videoRepository.observePendingVideoAnalysisTaskId().first()?.let { taskId ->
            return@withLock StartVideoScheduleCreationResult.AlreadyInProgress(taskId)
        }

        ensureAuthenticated()

        val existingSchedules = withAuthenticationRetry {
            findExistingSchedules(videoId, stopAfterFirst = !allowDuplicate)
        }
        if (!allowDuplicate) {
            existingSchedules.firstOrNull()?.let { existing ->
                return@withLock StartVideoScheduleCreationResult.ExistingSchedule(
                    tripPlanId = existing.id,
                    title = existing.title,
                )
            }
        }

        val analysis = withAuthenticationRetry { videoRepository.analyzeVideo(normalizedUrl) }
        if (analysis.isInProgress || analysis.status == VideoAnalysisStatus.COMPLETED && analysis.isValid) {
            // 분석 요청이 수락된 뒤 화면을 떠나도 작업 ID 저장은 끝낸다.
            withContext(NonCancellable) {
                videoRepository.savePendingVideoAnalysisTaskId(
                    taskId = analysis.id,
                    excludedTripPlanIds = existingSchedules.map { it.id }.toSet(),
                )
            }
        }
        val metadata = try {
            videoRepository.getYouTubeVideoMetadata(normalizedUrl)
        } catch (error: CancellationException) {
            throw error
        } catch (_: Throwable) {
            null
        }

        StartVideoScheduleCreationResult.AnalysisStarted(analysis, metadata)
    }

    private suspend fun <T> withAuthenticationRetry(action: suspend () -> T): T {
        return try {
            action()
        } catch (error: LinkTripApiException) {
            if (error.errorCode != LinkTripErrorCode.UNAUTHORIZED_AUTHENTICATION_FAILED && error.httpStatus != 401) {
                throw error
            }
            ensureAuthenticated(forceRefresh = true)
            action()
        }
    }

    private suspend fun findExistingSchedules(videoId: String, stopAfterFirst: Boolean): List<TripPlanSummary> {
        val matchingSchedules = mutableListOf<TripPlanSummary>()
        val requestedCursors = mutableSetOf<String?>(null)
        var cursor: String? = null

        while (true) {
            val page = tripPlanRepository.getTripPlans(cursor)
            matchingSchedules += page.items.filter { summary ->
                summary.youtubeUrl.youtubeVideoIdOrNull() == videoId
            }
            if (stopAfterFirst && matchingSchedules.isNotEmpty()) return matchingSchedules

            if (!page.hasNext) return matchingSchedules

            val nextCursor = page.nextCursor ?: return matchingSchedules
            if (!requestedCursors.add(nextCursor)) return matchingSchedules
            cursor = nextCursor
        }
    }

    private companion object {
        // ponytail: 단일 기기 계정 전체를 직렬화한다. 다중 계정 동시 생성을 지원하면 계정별 lock으로 분리한다.
        val creationMutex = Mutex()
    }
}

private fun String.youtubeVideoIdOrNull(): String? {
    val url = trim()

    YouTubeShortUrl.matchEntire(url)?.let { match ->
        return match.groupValues[1]
    }
    YouTubePathUrl.matchEntire(url)?.let { match ->
        return match.groupValues[1]
    }
    YouTubeWatchUrl.matchEntire(url)?.let { match ->
        return match.groupValues[1]
            .split('&')
            .firstOrNull { it.startsWith("v=") }
            ?.substringAfter("v=")
            ?.takeIf(String::isNotBlank)
    }

    return null
}

private val YouTubeShortUrl = Regex(
    pattern = """https?://(?:www\.)?youtu\.be/([A-Za-z0-9_-]+)(?:[/?#&].*)?""",
    option = RegexOption.IGNORE_CASE,
)

private val YouTubePathUrl = Regex(
    pattern = """https?://(?:www\.|m\.)?youtube\.com/(?:shorts|live|embed)/([A-Za-z0-9_-]+)(?:[/?#&].*)?""",
    option = RegexOption.IGNORE_CASE,
)

private val YouTubeWatchUrl = Regex(
    pattern = """https?://(?:www\.|m\.)?youtube\.com/watch\?([^#\s]+)(?:#.*)?""",
    option = RegexOption.IGNORE_CASE,
)

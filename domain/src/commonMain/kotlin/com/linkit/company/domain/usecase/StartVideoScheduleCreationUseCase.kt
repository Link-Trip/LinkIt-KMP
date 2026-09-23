package com.linkit.company.domain.usecase

import com.linkit.company.domain.model.tripplan.TripPlanSummary
import com.linkit.company.domain.model.video.VideoAnalysis
import com.linkit.company.domain.model.video.YouTubeVideoMetadata
import com.linkit.company.domain.repository.TripPlanRepository
import com.linkit.company.domain.repository.VideoRepository
import com.linkit.company.domain.util.YouTubeUrl
import dev.zacsweers.metro.Inject
import kotlin.coroutines.cancellation.CancellationException

sealed interface StartVideoScheduleCreationResult {
    data object InvalidFormat : StartVideoScheduleCreationResult

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
    ): StartVideoScheduleCreationResult {
        val normalizedUrl = YouTubeUrl.normalize(youtubeUrl)
        val videoId = YouTubeUrl.videoIdOrNull(normalizedUrl)
            ?: return StartVideoScheduleCreationResult.InvalidFormat

        ensureAuthenticated()

        if (!allowDuplicate) {
            findExistingSchedule(videoId)?.let { existing ->
                return StartVideoScheduleCreationResult.ExistingSchedule(
                    tripPlanId = existing.id,
                    title = existing.title,
                )
            }
        }

        val analysis = videoRepository.analyzeVideo(normalizedUrl)
        val metadata = try {
            videoRepository.getYouTubeVideoMetadata(normalizedUrl)
        } catch (error: CancellationException) {
            throw error
        } catch (_: Throwable) {
            null
        }

        return StartVideoScheduleCreationResult.AnalysisStarted(analysis, metadata)
    }

    private suspend fun findExistingSchedule(videoId: String): TripPlanSummary? {
        val requestedCursors = mutableSetOf<String?>(null)
        var cursor: String? = null

        while (true) {
            val page = tripPlanRepository.getTripPlans(cursor)
            page.items.firstOrNull { summary ->
                YouTubeUrl.videoIdOrNull(summary.youtubeUrl) == videoId
            }?.let { return it }

            if (!page.hasNext) return null

            val nextCursor = page.nextCursor ?: return null
            if (!requestedCursors.add(nextCursor)) return null
            cursor = nextCursor
        }
    }
}

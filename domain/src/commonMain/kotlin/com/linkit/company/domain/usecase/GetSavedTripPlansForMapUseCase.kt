package com.linkit.company.domain.usecase

import com.linkit.company.domain.exception.LinkTripApiException
import com.linkit.company.domain.exception.LinkTripErrorCode
import com.linkit.company.domain.model.map.GeoCoordinate
import com.linkit.company.domain.model.map.TripPlanMapData
import com.linkit.company.domain.model.map.TripPlanMapPlace
import com.linkit.company.domain.model.map.boundingBoxCenterOrNull
import com.linkit.company.domain.model.tripplan.TripPlanSummary
import com.linkit.company.domain.repository.TripPlanRepository
import com.linkit.company.domain.repository.VideoRepository
import dev.zacsweers.metro.Inject
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

/** 저장 일정 전체와 각 상세를 조합해 지도에서 사용할 일정/장소 데이터를 만든다. */
@Inject
class GetSavedTripPlansForMapUseCase(
    private val ensureAuthenticated: EnsureAuthenticatedUseCase,
    private val tripPlanRepository: TripPlanRepository,
    private val videoRepository: VideoRepository,
) {
    suspend operator fun invoke(): List<TripPlanMapData> {
        ensureAuthenticated()
        val summaries = getAllTripPlanSummaries()

        return coroutineScope {
            val requestSlots = Semaphore(4)
            fun <T> loadOptional(
                authenticatedRequest: Boolean = true,
                block: suspend () -> T,
            ) = async(start = CoroutineStart.UNDISPATCHED) {
                requestSlots.withPermit { requestOrNull(authenticatedRequest, block) }
            }

            val details = summaries.associate { summary ->
                summary.id to loadOptional { tripPlanRepository.getTripPlan(summary.id) }
            }
            val analyses = summaries.map(TripPlanSummary::videoAnalysisTaskId)
                .filter(String::isNotBlank)
                .distinct()
                .associateWith { id -> loadOptional { videoRepository.getVideoAnalysis(id) } }
            val metadata = summaries.map(TripPlanSummary::youtubeUrl)
                .filter(String::isNotBlank)
                .distinct()
                .associateWith { url ->
                    loadOptional(authenticatedRequest = false) {
                        videoRepository.getYouTubeVideoMetadata(url)
                    }
                }

            summaries.map { summary ->
                val detail = details.getValue(summary.id).await()
                val analysis = analyses[summary.videoAnalysisTaskId]?.await()
                val videoMetadata = metadata[summary.youtubeUrl]?.await()
                val places = detail?.items.orEmpty().mapNotNull { item ->
                    val place = item.place ?: return@mapNotNull null
                    val coordinate = GeoCoordinate.fromOrNull(
                        latitude = place.latitude,
                        longitude = place.longitude,
                    ) ?: return@mapNotNull null

                    TripPlanMapPlace(
                        item = item,
                        place = place,
                        coordinate = coordinate,
                    )
                }

                TripPlanMapData(
                    summary = summary,
                    places = places,
                    center = places.map(TripPlanMapPlace::coordinate).boundingBoxCenterOrNull(),
                    analysisSummary = analysis?.summary?.takeIf(String::isNotBlank),
                    estimatedMinCost = analysis?.estimatedMinCost,
                    estimatedMaxCost = analysis?.estimatedMaxCost,
                    costBasis = analysis?.costBasis,
                    thumbnailUrl = videoMetadata?.thumbnailUrl?.takeIf(String::isNotBlank),
                )
            }
        }
    }

    private suspend fun <T> requestOrNull(
        authenticatedRequest: Boolean,
        block: suspend () -> T,
    ): T? = try {
        block()
    } catch (error: LinkTripApiException) {
        if (authenticatedRequest && error.isUnauthorized()) throw error
        null
    } catch (error: CancellationException) {
        throw error
    } catch (_: Throwable) {
        null
    }

    private suspend fun getAllTripPlanSummaries(): List<TripPlanSummary> {
        val summariesById = linkedMapOf<String, TripPlanSummary>()
        val requestedCursors = mutableSetOf<String?>(null)
        var cursor: String? = null

        while (true) {
            val page = tripPlanRepository.getTripPlans(cursor)
            page.items.forEach { summary ->
                if (summary.id !in summariesById) {
                    summariesById[summary.id] = summary
                }
            }

            if (!page.hasNext) break

            val nextCursor = page.nextCursor ?: break
            if (!requestedCursors.add(nextCursor)) break
            cursor = nextCursor
        }

        return summariesById.values.toList()
    }
}

private fun LinkTripApiException.isUnauthorized(): Boolean =
    httpStatus == 401 || errorCode == LinkTripErrorCode.UNAUTHORIZED_AUTHENTICATION_FAILED

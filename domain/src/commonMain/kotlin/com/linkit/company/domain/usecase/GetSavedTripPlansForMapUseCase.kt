package com.linkit.company.domain.usecase

import com.linkit.company.domain.exception.LinkTripApiException
import com.linkit.company.domain.exception.LinkTripErrorCode
import com.linkit.company.domain.model.map.GeoCoordinate
import com.linkit.company.domain.model.map.TripPlanMapData
import com.linkit.company.domain.model.map.TripPlanMapPlace
import com.linkit.company.domain.model.map.boundingBoxCenterOrNull
import com.linkit.company.domain.model.tripplan.TripPlanSummary
import com.linkit.company.domain.repository.TripPlanRepository
import dev.zacsweers.metro.Inject
import kotlin.coroutines.cancellation.CancellationException

/** 저장 일정 전체와 각 상세를 조합해 지도에서 사용할 일정/장소 데이터를 만든다. */
@Inject
class GetSavedTripPlansForMapUseCase(
    private val ensureAuthenticated: EnsureAuthenticatedUseCase,
    private val tripPlanRepository: TripPlanRepository,
) {
    suspend operator fun invoke(): List<TripPlanMapData> {
        ensureAuthenticated()

        return getAllTripPlanSummaries().map { summary ->
            val detail = try {
                tripPlanRepository.getTripPlan(summary.id)
            } catch (error: LinkTripApiException) {
                if (error.isUnauthorized()) throw error
                return@map summary.withoutMapPlaces()
            } catch (error: CancellationException) {
                throw error
            } catch (_: Throwable) {
                return@map summary.withoutMapPlaces()
            }
            val places = detail.items.mapNotNull { item ->
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
            )
        }
    }

    private fun TripPlanSummary.withoutMapPlaces() = TripPlanMapData(
        summary = this,
        places = emptyList(),
        center = null,
    )

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

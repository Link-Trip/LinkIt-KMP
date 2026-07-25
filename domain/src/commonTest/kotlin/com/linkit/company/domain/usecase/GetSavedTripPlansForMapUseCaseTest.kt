package com.linkit.company.domain.usecase

import com.linkit.company.domain.model.auth.Auth
import com.linkit.company.domain.model.common.CursorPage
import com.linkit.company.domain.model.map.GeoCoordinate
import com.linkit.company.domain.model.place.Place
import com.linkit.company.domain.model.place.PlaceCategory
import com.linkit.company.domain.model.tripplan.TripPlanDetail
import com.linkit.company.domain.model.tripplan.TripPlanItem
import com.linkit.company.domain.model.tripplan.TripPlanItemOrder
import com.linkit.company.domain.model.tripplan.TripPlanSummary
import com.linkit.company.domain.exception.LinkTripApiException
import com.linkit.company.domain.exception.LinkTripErrorCode
import com.linkit.company.domain.repository.AuthRepository
import com.linkit.company.domain.repository.TripPlanRepository
import com.linkit.company.domain.runImmediateSuspend
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class GetSavedTripPlansForMapUseCaseTest {

    @Test
    fun loadsEveryPageAndBuildsMapDataFromValidPlaceCoordinates() = runImmediateSuspend {
        val firstSummary = summary(id = "trip-1")
        val secondSummary = summary(id = "trip-2")
        val tripPlanRepository = FakeTripPlanRepository(
            pages = mapOf(
                null to CursorPage(
                    items = listOf(firstSummary),
                    nextCursor = "cursor-1",
                    hasNext = true,
                ),
                "cursor-1" to CursorPage(
                    items = listOf(secondSummary),
                    nextCursor = null,
                    hasNext = false,
                ),
            ),
            details = mapOf(
                "trip-1" to detail(
                    id = "trip-1",
                    items = listOf(
                        item(id = "seoul", latitude = 37.0, longitude = 126.0),
                        item(id = "busan", latitude = 35.0, longitude = 130.0),
                        item(id = "missing-place", place = null),
                        item(id = "missing-latitude", latitude = null, longitude = 127.0),
                        item(id = "invalid-latitude", latitude = 91.0, longitude = 127.0),
                    ),
                ),
                "trip-2" to detail(
                    id = "trip-2",
                    items = listOf(
                        item(id = "missing-coordinate", latitude = null, longitude = null),
                    ),
                ),
            ),
        )
        val authRepository = MapAuthRepositoryFake(isLoggedIn = false)
        val useCase = GetSavedTripPlansForMapUseCase(
            ensureAuthenticated = EnsureAuthenticatedUseCase(authRepository),
            tripPlanRepository = tripPlanRepository,
        )

        val result = useCase()

        assertEquals(1, authRepository.loginCallCount)
        assertEquals(listOf(null, "cursor-1"), tripPlanRepository.requestedCursors)
        assertEquals(listOf("trip-1", "trip-2"), tripPlanRepository.requestedDetailIds)
        assertEquals(listOf("trip-1", "trip-2"), result.map { it.summary.id })
        assertEquals(listOf("seoul", "busan"), result[0].places.map { it.item.id })
        assertEquals(GeoCoordinate(latitude = 36.0, longitude = 128.0), result[0].center)
        assertEquals(emptyList(), result[1].places)
        assertNull(result[1].center)
    }

    @Test
    fun stopsWhenCursorCyclesAndDoesNotLoadDuplicateSummaryDetails() = runImmediateSuspend {
        val tripPlanRepository = FakeTripPlanRepository(
            pages = mapOf(
                null to CursorPage(
                    items = listOf(summary(id = "trip-1")),
                    nextCursor = "cursor-a",
                    hasNext = true,
                ),
                "cursor-a" to CursorPage(
                    items = listOf(summary(id = "trip-1"), summary(id = "trip-2")),
                    nextCursor = "cursor-b",
                    hasNext = true,
                ),
                "cursor-b" to CursorPage(
                    items = listOf(summary(id = "trip-3")),
                    nextCursor = "cursor-a",
                    hasNext = true,
                ),
            ),
            details = mapOf(
                "trip-1" to detail(id = "trip-1"),
                "trip-2" to detail(id = "trip-2"),
                "trip-3" to detail(id = "trip-3"),
            ),
        )
        val useCase = GetSavedTripPlansForMapUseCase(
            ensureAuthenticated = EnsureAuthenticatedUseCase(MapAuthRepositoryFake(isLoggedIn = true)),
            tripPlanRepository = tripPlanRepository,
        )

        val result = useCase()

        assertEquals(listOf(null, "cursor-a", "cursor-b"), tripPlanRepository.requestedCursors)
        assertEquals(listOf("trip-1", "trip-2", "trip-3"), tripPlanRepository.requestedDetailIds)
        assertEquals(listOf("trip-1", "trip-2", "trip-3"), result.map { it.summary.id })
    }

    @Test
    fun stopsSafelyWhenNextCursorIsMissing() = runImmediateSuspend {
        val tripPlanRepository = FakeTripPlanRepository(
            pages = mapOf(
                null to CursorPage(
                    items = listOf(summary(id = "trip-1")),
                    nextCursor = null,
                    hasNext = true,
                ),
            ),
            details = mapOf("trip-1" to detail(id = "trip-1")),
        )
        val useCase = GetSavedTripPlansForMapUseCase(
            ensureAuthenticated = EnsureAuthenticatedUseCase(MapAuthRepositoryFake(isLoggedIn = true)),
            tripPlanRepository = tripPlanRepository,
        )

        val result = useCase()

        assertEquals(listOf<String?>(null), tripPlanRepository.requestedCursors)
        assertEquals(listOf("trip-1"), result.map { it.summary.id })
    }

    @Test
    fun keepsSummaryWhenOneDetailCannotBeLoaded() = runImmediateSuspend {
        val tripPlanRepository = FakeTripPlanRepository(
            pages = mapOf(
                null to CursorPage(
                    items = listOf(summary(id = "trip-1"), summary(id = "trip-2")),
                    nextCursor = null,
                    hasNext = false,
                ),
            ),
            details = mapOf("trip-1" to detail(id = "trip-1")),
        )
        val useCase = GetSavedTripPlansForMapUseCase(
            ensureAuthenticated = EnsureAuthenticatedUseCase(MapAuthRepositoryFake(isLoggedIn = true)),
            tripPlanRepository = tripPlanRepository,
        )

        val result = useCase()

        assertEquals(listOf("trip-1", "trip-2"), result.map { it.summary.id })
        assertEquals(emptyList(), result[1].places)
        assertNull(result[1].center)
    }

    @Test
    fun propagatesUnauthorizedDetailFailureForAuthenticationRetry() {
        val unauthorized = LinkTripApiException(
            errorCode = LinkTripErrorCode.UNAUTHORIZED_AUTHENTICATION_FAILED,
            httpStatus = 401,
            message = "Unauthorized",
        )
        val tripPlanRepository = FakeTripPlanRepository(
            pages = mapOf(
                null to CursorPage(
                    items = listOf(summary(id = "trip-1")),
                    nextCursor = null,
                    hasNext = false,
                ),
            ),
            details = emptyMap(),
            detailErrors = mapOf("trip-1" to unauthorized),
        )
        val useCase = GetSavedTripPlansForMapUseCase(
            ensureAuthenticated = EnsureAuthenticatedUseCase(MapAuthRepositoryFake(isLoggedIn = true)),
            tripPlanRepository = tripPlanRepository,
        )

        assertFailsWith<LinkTripApiException> {
            runImmediateSuspend { useCase() }
        }
    }
}

private class MapAuthRepositoryFake(
    private var isLoggedIn: Boolean,
) : AuthRepository {
    var loginCallCount = 0
        private set

    override suspend fun login(): Auth {
        loginCallCount += 1
        isLoggedIn = true
        return Auth(memberId = "member-id", accessToken = "access-token")
    }

    override suspend fun isLoggedIn(): Boolean = isLoggedIn

    override suspend fun logout() {
        isLoggedIn = false
    }
}

private class FakeTripPlanRepository(
    private val pages: Map<String?, CursorPage<TripPlanSummary>>,
    private val details: Map<String, TripPlanDetail>,
    private val detailErrors: Map<String, Throwable> = emptyMap(),
) : TripPlanRepository {
    val requestedCursors = mutableListOf<String?>()
    val requestedDetailIds = mutableListOf<String>()

    override suspend fun getTripPlans(cursor: String?): CursorPage<TripPlanSummary> {
        requestedCursors += cursor
        return checkNotNull(pages[cursor]) { "No page fixture for cursor=$cursor" }
    }

    override suspend fun getTripPlan(tripPlanId: String): TripPlanDetail {
        requestedDetailIds += tripPlanId
        detailErrors[tripPlanId]?.let { throw it }
        return checkNotNull(details[tripPlanId]) { "No detail fixture for id=$tripPlanId" }
    }

    override suspend fun updateTripPlan(
        tripPlanId: String,
        title: String?,
        items: List<TripPlanItemOrder>?,
    ): TripPlanDetail = error("Not used in this test")

    override suspend fun deleteTripPlan(tripPlanId: String) = error("Not used in this test")
}

private fun summary(id: String) = TripPlanSummary(
    id = id,
    title = "Title $id",
    videoAnalysisTaskId = "task-$id",
    youtubeUrl = "https://youtube.com/watch?v=$id",
    itemCount = 1,
    nights = 1,
    days = 2,
    hashtags = listOf("trip"),
    createdAt = "2026-07-22T00:00:00Z",
    updatedAt = "2026-07-22T00:00:00Z",
)

private fun detail(
    id: String,
    items: List<TripPlanItem> = emptyList(),
) = TripPlanDetail(
    id = id,
    title = "Title $id",
    videoAnalysisTaskId = "task-$id",
    items = items,
    createdAt = "2026-07-22T00:00:00Z",
    updatedAt = "2026-07-22T00:00:00Z",
)

private fun item(
    id: String,
    latitude: Double? = 37.5665,
    longitude: Double? = 126.978,
    place: Place? = Place(
        id = "place-$id",
        name = "Place $id",
        googlePlaceId = "google-$id",
        address = "Address $id",
        latitude = latitude,
        longitude = longitude,
    ),
) = TripPlanItem(
    id = id,
    travelItineraryItemId = "itinerary-$id",
    day = 1,
    itemOrder = 1,
    name = "Item $id",
    category = PlaceCategory.ATTRACTION,
    description = "Description $id",
    tips = "Tips $id",
    place = place,
)

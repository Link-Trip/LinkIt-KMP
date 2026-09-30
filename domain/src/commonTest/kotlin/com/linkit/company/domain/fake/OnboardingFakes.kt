package com.linkit.company.domain.fake

import com.linkit.company.domain.model.common.CursorPage
import com.linkit.company.domain.model.onboarding.TutorialStep
import com.linkit.company.domain.model.tripplan.TripPlanDetail
import com.linkit.company.domain.model.tripplan.TripPlanItemOrder
import com.linkit.company.domain.model.tripplan.TripPlanSummary
import com.linkit.company.domain.repository.OnboardingRepository
import com.linkit.company.domain.repository.TripPlanRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/** 호출 순서를 기록하는 메모리 온보딩 저장소. */
internal class InMemoryOnboardingRepository(
    completed: Boolean = false,
) : OnboardingRepository {
    val events = mutableListOf<String>()
    var onboardingCompleted = completed
    var termsAgreedAt: Long? = null
    val tutorialStep = MutableStateFlow<TutorialStep?>(null)

    override suspend fun isOnboardingCompleted(): Boolean = onboardingCompleted

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        events += "setOnboardingCompleted($completed)"
        onboardingCompleted = completed
    }

    override suspend fun isTermsAgreed(): Boolean = termsAgreedAt != null

    override suspend fun setTermsAgreed(agreedAtEpochMillis: Long) {
        events += "setTermsAgreed"
        termsAgreedAt = agreedAtEpochMillis
    }

    override fun observeTutorialStep(): Flow<TutorialStep?> = tutorialStep

    override suspend fun setTutorialStep(step: TutorialStep?) {
        events += "setTutorialStep($step)"
        tutorialStep.value = step
    }

    override suspend fun clearAll() {
        events += "clearAll"
        onboardingCompleted = false
        termsAgreedAt = null
        tutorialStep.value = null
    }
}

/**
 * 페이지 픽스처와 `확인전` 집합을 갖는 여행 계획 저장소.
 * [getTripPlansResults]에 [Throwable]을 넣으면 해당 호출에서 던진다.
 */
internal class ScriptedTripPlanRepository(
    private val pages: Map<String?, CursorPage<TripPlanSummary>> = emptyMap(),
    getTripPlansResults: List<Throwable?> = emptyList(),
) : TripPlanRepository {
    val events = mutableListOf<String>()
    val requestedCursors = mutableListOf<String?>()
    val uncheckedIds = MutableStateFlow<Set<String>>(emptySet())
    private val getTripPlansQueue = ArrayDeque(getTripPlansResults)

    /** 커서별 페이지를 호출 시점에 바꿀 수 있게 한다(재조회 후 성공 시나리오). */
    var pageOverrides: Map<String?, CursorPage<TripPlanSummary>> = emptyMap()

    override suspend fun getTripPlans(cursor: String?): CursorPage<TripPlanSummary> {
        requestedCursors += cursor
        getTripPlansQueue.removeFirstOrNull()?.let { throw it }
        return pageOverrides[cursor]
            ?: pages[cursor]
            ?: CursorPage(items = emptyList(), nextCursor = null, hasNext = false)
    }

    override suspend fun getTripPlan(tripPlanId: String): TripPlanDetail = error("Not used in this test")

    override suspend fun updateTripPlan(
        tripPlanId: String,
        title: String?,
        items: List<TripPlanItemOrder>?,
    ): TripPlanDetail = error("Not used in this test")

    override suspend fun deleteTripPlan(tripPlanId: String) = error("Not used in this test")

    override fun observeUncheckedTripPlanIds(): Flow<Set<String>> = uncheckedIds

    override suspend fun markTripPlanUnchecked(tripPlanId: String) {
        events += "markUnchecked($tripPlanId)"
        uncheckedIds.value = uncheckedIds.value + tripPlanId
    }

    override suspend fun markTripPlanChecked(tripPlanId: String) {
        events += "markChecked($tripPlanId)"
        uncheckedIds.value = uncheckedIds.value - tripPlanId
    }

    override suspend fun clearUncheckedTripPlans() {
        events += "clearUnchecked"
        uncheckedIds.value = emptySet()
    }
}

internal fun tripPlanSummary(
    id: String,
    videoAnalysisTaskId: String = "task-$id",
    youtubeUrl: String = "https://youtu.be/$id",
) = TripPlanSummary(
    id = id,
    title = "Title $id",
    videoAnalysisTaskId = videoAnalysisTaskId,
    youtubeUrl = youtubeUrl,
    itemCount = 1,
    nights = 1,
    days = 2,
    hashtags = emptyList(),
    createdAt = "2026-09-21T00:00:00Z",
    updatedAt = "2026-09-21T00:00:00Z",
)

package com.linkit.company.feature.schedule

import android.os.Looper
import com.linkit.company.domain.model.auth.Auth
import com.linkit.company.domain.model.common.CursorPage
import com.linkit.company.domain.model.onboarding.TutorialStep
import com.linkit.company.domain.model.tripplan.TripPlanDetail
import com.linkit.company.domain.model.tripplan.TripPlanItemOrder
import com.linkit.company.domain.model.tripplan.TripPlanSummary
import com.linkit.company.domain.model.video.DiscoverChannel
import com.linkit.company.domain.model.video.DiscoverVideo
import com.linkit.company.domain.model.video.VideoAnalysis
import com.linkit.company.domain.model.video.VideoAnalysisStatus
import com.linkit.company.domain.model.video.YouTubeVideoMetadata
import com.linkit.company.domain.repository.AuthRepository
import com.linkit.company.domain.repository.OnboardingRepository
import com.linkit.company.domain.repository.TripPlanRepository
import com.linkit.company.domain.repository.VideoRepository
import com.linkit.company.domain.usecase.CompleteOnboardingUseCase
import com.linkit.company.domain.usecase.CreateOnboardingScheduleUseCase
import com.linkit.company.domain.usecase.EnsureAuthenticatedUseCase
import com.linkit.company.domain.usecase.StartVideoScheduleCreationUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ScheduleViewModelTest {

    private val recommendedUrl = "https://www.youtube.com/watch?v=rec1"

    // ---- 추천 영상 ----

    @Test
    fun recommendedVideosGoFromLoadingToContent() {
        val video = FakeVideoRepository(onboardingVideos = listOf(discoverVideo("rec1")))
        val viewModel = createViewModel(video = video)
        assertEquals(RecommendedVideosState.Loading, viewModel.uiState.value.recommendedVideos)

        viewModel.onIntent(ScheduleIntent.LoadRecommendedVideos)
        idle()

        assertEquals(listOf(recommendedUrl), viewModel.uiState.value.recommendedVideoUrls)
    }

    @Test
    fun recommendedVideosFailureAndEmptyListBecomeError() {
        val failing = createViewModel(video = FakeVideoRepository(onboardingError = IllegalStateException("offline")))
        failing.onIntent(ScheduleIntent.LoadRecommendedVideos)
        idle()
        assertEquals(RecommendedVideosState.Error, failing.uiState.value.recommendedVideos)

        val empty = createViewModel(video = FakeVideoRepository(onboardingVideos = emptyList()))
        empty.onIntent(ScheduleIntent.LoadRecommendedVideos)
        idle()
        assertEquals(RecommendedVideosState.Error, empty.uiState.value.recommendedVideos)
    }

    @Test
    fun guideAppearsOnlyAfterDelayWithContentInCopyStep() {
        val onboarding = FakeOnboardingRepository(TutorialStep.COPY_LINK)
        val viewModel = createViewModel(onboarding = onboarding, video = FakeVideoRepository(listOf(discoverVideo("rec1"))))
        idle()

        viewModel.onIntent(ScheduleIntent.GuideDelayElapsed)
        assertFalse(viewModel.uiState.value.isGuideVisible)

        viewModel.onIntent(ScheduleIntent.LoadRecommendedVideos)
        idle()
        assertTrue(viewModel.uiState.value.isGuideVisible)
    }

    // ---- 클립보드 · 단계 전이 ----

    @Test
    fun copyingRecommendedLinkWritesClipboardShowsToastAndAdvancesToPaste() {
        val onboarding = FakeOnboardingRepository(TutorialStep.COPY_LINK)
        val viewModel = createViewModel(onboarding = onboarding)
        val effects = viewModel.collectSideEffects()
        idle()

        viewModel.onIntent(ScheduleIntent.CopyRecommendedLink(recommendedUrl))
        idle()

        assertEquals(listOf(ScheduleSideEffect.WriteClipboard(recommendedUrl)), effects)
        assertEquals(recommendedUrl, viewModel.uiState.value.clipboardToastUrl)
        assertTrue(viewModel.uiState.value.hasClipboardText)
        assertEquals(TutorialStep.PASTE_LINK, viewModel.uiState.value.tutorialStep)
        assertEquals(listOf("setTutorialStep(PASTE_LINK)"), onboarding.events)
    }

    @Test
    fun pastingFillsLinkAndMovesToFreeStep() {
        val onboarding = FakeOnboardingRepository(TutorialStep.PASTE_LINK)
        val viewModel = createViewModel(onboarding = onboarding)
        idle()

        viewModel.onIntent(ScheduleIntent.PasteFromClipboard(recommendedUrl))
        idle()

        assertEquals(recommendedUrl, viewModel.uiState.value.videoLink)
        assertEquals(TutorialStep.FREE, viewModel.uiState.value.tutorialStep)
        assertTrue(viewModel.uiState.value.canCreate)
    }

    @Test
    fun applyingClipboardToastFillsLinkAndClearsToast() {
        val onboarding = FakeOnboardingRepository(TutorialStep.COPY_LINK)
        val viewModel = createViewModel(onboarding = onboarding)
        idle()
        viewModel.onIntent(ScheduleIntent.CopyRecommendedLink(recommendedUrl))
        idle()

        viewModel.onIntent(ScheduleIntent.ApplyClipboardToast)
        idle()

        assertEquals(recommendedUrl, viewModel.uiState.value.videoLink)
        assertNull(viewModel.uiState.value.clipboardToastUrl)
        assertEquals(TutorialStep.FREE, viewModel.uiState.value.tutorialStep)
    }

    // ---- 온보딩 일정 생성 분기 ----

    @Test
    fun onboardingSubmitWithMalformedLinkShowsInvalidToastAndDisablesButton() {
        val viewModel = createOnboardingViewModelWithLink("https://example.com/x")

        viewModel.onIntent(ScheduleIntent.SubmitVideoLink)
        idle()

        assertEquals(ScheduleEditStrings.ToastInvalid, viewModel.uiState.value.errorToast)
        assertEquals(VideoLinkError.WRONG_FORMAT, viewModel.uiState.value.videoLinkError)
        assertFalse(viewModel.uiState.value.canCreate)
    }

    @Test
    fun onboardingSubmitWithNonRecommendedLinkShowsRecommendedOnlyToast() {
        val viewModel = createOnboardingViewModelWithLink("https://youtu.be/other")

        viewModel.onIntent(ScheduleIntent.SubmitVideoLink)
        idle()

        assertEquals(ScheduleEditStrings.ToastNotRecommended, viewModel.uiState.value.errorToast)
        assertEquals(VideoLinkError.INVALID_LINK, viewModel.uiState.value.videoLinkError)
        assertFalse(viewModel.uiState.value.canCreate)

        viewModel.onIntent(ScheduleIntent.UpdateVideoLink(recommendedUrl))
        assertNull(viewModel.uiState.value.videoLinkError)
        assertTrue(viewModel.uiState.value.canCreate)
    }

    @Test
    fun onboardingSubmitWithPendingAnalysisShowsNotReadyToastAndKeepsButtonEnabled() {
        val viewModel = createOnboardingViewModelWithLink(
            link = recommendedUrl,
            video = FakeVideoRepository(
                onboardingVideos = listOf(discoverVideo("rec1")),
                analysisStatus = VideoAnalysisStatus.PENDING,
            ),
        )

        viewModel.onIntent(ScheduleIntent.SubmitVideoLink)
        idle()

        assertEquals(ScheduleEditStrings.ToastNotReady, viewModel.uiState.value.errorToast)
        assertNull(viewModel.uiState.value.videoLinkError)
        assertTrue(viewModel.uiState.value.canCreate)
    }

    @Test
    fun onboardingSubmitWithRecommendedLinkNavigatesToCompleteAndMarksUnchecked() {
        val tripPlans = FakeTripPlanRepository(
            plans = listOf(tripPlanSummary("plan-1", videoAnalysisTaskId = "analysis-rec1")),
        )
        val viewModel = createOnboardingViewModelWithLink(recommendedUrl, tripPlans = tripPlans)
        val effects = viewModel.collectSideEffects()

        viewModel.onIntent(ScheduleIntent.SubmitVideoLink)
        idle()

        assertEquals(listOf(ScheduleSideEffect.NavigateToAnalysisComplete), effects)
        assertEquals(setOf("plan-1"), tripPlans.uncheckedIds.value)
        assertFalse(viewModel.uiState.value.isSubmittingVideoLink)
    }

    @Test
    fun withoutTutorialStepSubmitUsesRegularAnalysisPath() {
        val video = FakeVideoRepository(onboardingVideos = listOf(discoverVideo("rec1")))
        val viewModel = createViewModel(onboarding = FakeOnboardingRepository(null), video = video)
        val effects = viewModel.collectSideEffects()
        viewModel.onIntent(ScheduleIntent.UpdateVideoLink("https://youtu.be/other"))

        viewModel.onIntent(ScheduleIntent.SubmitVideoLink)
        idle()

        assertEquals(1, effects.size)
        assertTrue(effects.single() is ScheduleSideEffect.NavigateToAnalysis)
        assertNull(viewModel.uiState.value.errorToast)
    }

    // ---- 건너뛰기 · 완료 ----

    @Test
    fun skipOnboardingRecordsCompletionAndFinishes() {
        val onboarding = FakeOnboardingRepository(TutorialStep.COPY_LINK)
        val viewModel = createViewModel(onboarding = onboarding)
        val effects = viewModel.collectSideEffects()
        idle()

        viewModel.onIntent(ScheduleIntent.SkipOnboarding)
        idle()

        assertEquals(listOf("setOnboardingCompleted(true)", "setTutorialStep(null)"), onboarding.events)
        assertEquals(listOf(ScheduleSideEffect.FinishOnboarding), effects)
        assertNull(viewModel.uiState.value.tutorialStep)
    }

    @Test
    fun confirmingOnboardingScheduleRecordsCompletionAndFinishes() {
        val onboarding = FakeOnboardingRepository(TutorialStep.FREE)
        val viewModel = createViewModel(onboarding = onboarding)
        val effects = viewModel.collectSideEffects()
        idle()

        viewModel.onIntent(ScheduleIntent.ConfirmOnboardingSchedule)
        idle()

        assertTrue(onboarding.onboardingCompleted)
        assertEquals(listOf(ScheduleSideEffect.FinishOnboarding), effects)
    }

    @Test
    fun skipWithoutTutorialDoesNothing() {
        val onboarding = FakeOnboardingRepository(null)
        val viewModel = createViewModel(onboarding = onboarding)
        val effects = viewModel.collectSideEffects()

        viewModel.onIntent(ScheduleIntent.SkipOnboarding)
        idle()

        assertEquals(emptyList<String>(), onboarding.events)
        assertEquals(emptyList<ScheduleSideEffect>(), effects)
    }

    // ---- helpers ----

    private fun createOnboardingViewModelWithLink(
        link: String,
        video: FakeVideoRepository = FakeVideoRepository(onboardingVideos = listOf(discoverVideo("rec1"))),
        tripPlans: FakeTripPlanRepository = FakeTripPlanRepository(),
    ): ScheduleViewModel {
        val viewModel = createViewModel(
            onboarding = FakeOnboardingRepository(TutorialStep.FREE),
            video = video,
            tripPlans = tripPlans,
        )
        viewModel.onIntent(ScheduleIntent.LoadRecommendedVideos)
        idle()
        viewModel.onIntent(ScheduleIntent.UpdateVideoLink(link))
        return viewModel
    }

    private fun createViewModel(
        onboarding: FakeOnboardingRepository = FakeOnboardingRepository(null),
        video: FakeVideoRepository = FakeVideoRepository(),
        tripPlans: FakeTripPlanRepository = FakeTripPlanRepository(),
    ): ScheduleViewModel {
        val ensureAuthenticated = EnsureAuthenticatedUseCase(FakeAuthRepository())
        return ScheduleViewModel(
            startVideoScheduleCreation = StartVideoScheduleCreationUseCase(
                ensureAuthenticated = ensureAuthenticated,
                tripPlanRepository = tripPlans,
                videoRepository = video,
            ),
            createOnboardingSchedule = CreateOnboardingScheduleUseCase(
                ensureAuthenticated = ensureAuthenticated,
                videoRepository = video,
                tripPlanRepository = tripPlans,
            ),
            completeOnboarding = CompleteOnboardingUseCase(onboarding),
            onboardingRepository = onboarding,
            videoRepository = video,
        )
    }

    private fun ScheduleViewModel.collectSideEffects(): List<ScheduleSideEffect> {
        val effects = mutableListOf<ScheduleSideEffect>()
        CoroutineScope(Dispatchers.Main).launch { sideEffect.collect(effects::add) }
        idle()
        return effects
    }

    private fun idle() = shadowOf(Looper.getMainLooper()).idle()

    private fun discoverVideo(id: String) = DiscoverVideo(
        videoId = id,
        videoUrl = "https://www.youtube.com/watch?v=$id",
        title = "title $id",
        description = "",
        thumbnailUrl = "https://img/$id.jpg",
        channelId = "channel",
        channelTitle = "channel",
        viewCount = 1_130_000,
        likeCount = 0,
        duration = "PT10M",
        publishedAt = "2026-09-21T00:00:00Z",
        region = "",
        country = "",
        city = "",
        theme = "",
    )

    private fun tripPlanSummary(id: String, videoAnalysisTaskId: String) = TripPlanSummary(
        id = id,
        title = "Title $id",
        videoAnalysisTaskId = videoAnalysisTaskId,
        youtubeUrl = "https://youtu.be/$id",
        itemCount = 1,
        nights = 1,
        days = 2,
        hashtags = emptyList(),
        createdAt = "",
        updatedAt = "",
    )
}

private class FakeOnboardingRepository(initialStep: TutorialStep?) : OnboardingRepository {
    val events = mutableListOf<String>()
    var onboardingCompleted = false
    val tutorialStep = MutableStateFlow(initialStep)

    override suspend fun isOnboardingCompleted(): Boolean = onboardingCompleted

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        events += "setOnboardingCompleted($completed)"
        onboardingCompleted = completed
    }

    override suspend fun isTermsAgreed(): Boolean = true

    override suspend fun setTermsAgreed(agreedAtEpochMillis: Long) = Unit

    override fun observeTutorialStep(): Flow<TutorialStep?> = tutorialStep

    override suspend fun setTutorialStep(step: TutorialStep?) {
        events += "setTutorialStep($step)"
        tutorialStep.value = step
    }

    override suspend fun clearAll() = Unit
}

private class FakeAuthRepository : AuthRepository {
    override suspend fun login(): Auth = Auth("member", "token")

    override suspend fun isLoggedIn(): Boolean = true

    override suspend fun logout() = Unit
}

private class FakeVideoRepository(
    private val onboardingVideos: List<DiscoverVideo> = emptyList(),
    private val onboardingError: Throwable? = null,
    private val analysisStatus: VideoAnalysisStatus = VideoAnalysisStatus.COMPLETED,
) : VideoRepository {
    override suspend fun analyzeVideo(youtubeUrl: String): VideoAnalysis = VideoAnalysis(
        id = "analysis-rec1",
        youtubeUrl = youtubeUrl,
        isValid = true,
        status = analysisStatus,
        summary = "",
        estimatedMinCost = null,
        estimatedMaxCost = null,
        costBasis = null,
        placeEnrichmentCompleted = true,
        timelines = emptyList(),
        itineraryItems = emptyList(),
    )

    override suspend fun getVideoAnalysis(videoAnalysisTaskId: String): VideoAnalysis = analyzeVideo("")

    override suspend fun getYouTubeVideoMetadata(youtubeUrl: String): YouTubeVideoMetadata =
        YouTubeVideoMetadata(title = "title", thumbnailUrl = "https://img/thumb.jpg")

    override suspend fun getDiscoverVideosByTheme(theme: String, cursor: String?): CursorPage<DiscoverVideo> =
        error("Not used")

    override suspend fun getDiscoverChannels(): List<DiscoverChannel> = error("Not used")

    override suspend fun getDiscoverVideosByCountry(country: String): List<DiscoverVideo> = error("Not used")

    override suspend fun getDiscoverVideosByRegion(region: String): List<DiscoverVideo> = error("Not used")

    override suspend fun getOnboardingVideos(): List<DiscoverVideo> {
        onboardingError?.let { throw it }
        return onboardingVideos
    }
}

private class FakeTripPlanRepository(
    private val plans: List<TripPlanSummary> = emptyList(),
) : TripPlanRepository {
    val uncheckedIds = MutableStateFlow<Set<String>>(emptySet())

    override suspend fun getTripPlans(cursor: String?): CursorPage<TripPlanSummary> =
        CursorPage(items = plans, nextCursor = null, hasNext = false)

    override suspend fun getTripPlan(tripPlanId: String): TripPlanDetail = error("Not used")

    override suspend fun updateTripPlan(
        tripPlanId: String,
        title: String?,
        items: List<TripPlanItemOrder>?,
    ): TripPlanDetail = error("Not used")

    override suspend fun deleteTripPlan(tripPlanId: String) = error("Not used")

    override fun observeUncheckedTripPlanIds(): Flow<Set<String>> = uncheckedIds

    override suspend fun markTripPlanUnchecked(tripPlanId: String) {
        uncheckedIds.value = uncheckedIds.value + tripPlanId
    }

    override suspend fun markTripPlanChecked(tripPlanId: String) {
        uncheckedIds.value = uncheckedIds.value - tripPlanId
    }

    override suspend fun clearUncheckedTripPlans() {
        uncheckedIds.value = emptySet()
    }
}

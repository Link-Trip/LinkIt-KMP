package com.linkit.company.feature.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.linkit.company.core.common.architecture.MviContainer
import com.linkit.company.core.common.architecture.MviContext
import com.linkit.company.domain.exception.LinkTripApiException
import com.linkit.company.domain.model.video.VideoAnalysisStatus
import com.linkit.company.domain.usecase.DeleteTripPlanUseCase
import com.linkit.company.domain.usecase.GetExploreVideosUseCase
import com.linkit.company.domain.usecase.RenameTripPlanUseCase
import com.linkit.company.domain.model.onboarding.OnboardingCompletion
import com.linkit.company.domain.model.onboarding.TutorialStep
import com.linkit.company.domain.repository.OnboardingRepository
import com.linkit.company.domain.repository.VideoRepository
import com.linkit.company.domain.usecase.CompleteOnboardingUseCase
import com.linkit.company.domain.usecase.CreateOnboardingScheduleResult
import com.linkit.company.domain.usecase.CreateOnboardingScheduleUseCase
import com.linkit.company.domain.usecase.StartVideoScheduleCreationResult
import com.linkit.company.domain.usecase.StartVideoScheduleCreationUseCase
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first

/**
 * 영상 링크로 만들기 화면의 상태.
 *
 * 튜토리얼 단계는 [OnboardingRepository]가 단일 출처다(지도 Activity와 공유). 단계가 `null`이 아니면 온보딩 모드로
 * 추천 영상 링크만 받아 [CreateOnboardingScheduleUseCase]로 곧바로 완료 화면까지 가고, `null`이면 기존
 * [StartVideoScheduleCreationUseCase] 경로(중복 확인·분석 중 화면)를 그대로 쓴다 (research R8).
 */
@ContributesIntoMap(AppScope::class)
@ViewModelKey(ScheduleViewModel::class)
@Inject
class ScheduleViewModel(
    private val startVideoScheduleCreation: StartVideoScheduleCreationUseCase,
    private val renameTripPlan: RenameTripPlanUseCase,
    private val deleteTripPlan: DeleteTripPlanUseCase,
    private val getExploreVideos: GetExploreVideosUseCase,
    private val createOnboardingSchedule: CreateOnboardingScheduleUseCase,
    private val completeOnboarding: CompleteOnboardingUseCase,
    private val onboardingRepository: OnboardingRepository,
    private val videoRepository: VideoRepository,
) : ViewModel() {
    private val container = MviContainer<ScheduleIntent, ScheduleSideEffect, ScheduleUiState>(
        initialState = ScheduleUiState(),
        onIntent = { handleIntent(it) },
    )
    private var submissionJob: Job? = null
    private var recommendationsJob: Job? = null
    private var guideDelayElapsed = false

    val uiState = container.uiState
    val sideEffect = container.sideEffect

    init {
        observeTutorialStep()
    }

    fun onIntent(intent: ScheduleIntent) = container.intent(intent)

    private fun MviContext<ScheduleUiState, ScheduleSideEffect>.handleIntent(intent: ScheduleIntent) {
        when (intent) {
            ScheduleIntent.LoadRecommendedVideos -> loadRecommendedVideos()
            ScheduleIntent.ToggleRecommendedVideos -> reduce {
                copy(areRecommendedVideosExpanded = !areRecommendedVideosExpanded)
            }
            is ScheduleIntent.UpdateVideoLink -> {
                if (currentState.isSubmittingVideoLink || submissionJob?.isActive == true) return
                reduce {
                    copy(
                        videoLink = intent.link,
                        videoLinkError = null,
                        isSubmittingVideoLink = false,
                        existingSchedule = null,
                    )
                }
            }
            ScheduleIntent.SubmitVideoLink -> {
                if (currentState.isOnboardingMode) submitOnboardingVideoLink() else submitVideoLink(allowDuplicate = false)
            }
            ScheduleIntent.CreateDuplicateVideoSchedule -> {
                reduce { copy(existingSchedule = null) }
                submitVideoLink(allowDuplicate = true)
            }
            ScheduleIntent.OpenExistingSchedule -> {
                currentState.existingSchedule?.let { existing ->
                    reduce { copy(existingSchedule = null) }
                    postSideEffect(
                        ScheduleSideEffect.OpenExistingSchedule(
                            tripPlanId = existing.tripPlanId,
                            title = existing.title,
                        ),
                    )
                }
            }
            ScheduleIntent.DismissExistingSchedule -> reduce { copy(existingSchedule = null) }
            is ScheduleIntent.SelectTripDetailTab -> reduce {
                copy(tripDetailTab = intent.tab, showTripMapPreview = false)
            }
            ScheduleIntent.ToggleTripDetailMenu -> reduce {
                copy(tripDetailMenuExpanded = !tripDetailMenuExpanded)
            }
            ScheduleIntent.DismissTripDetailMenu -> reduce {
                copy(tripDetailMenuExpanded = false)
            }
            is ScheduleIntent.ShowTripDetailRenameDialog -> reduce {
                copy(
                    tripDetailMenuExpanded = false,
                    tripDetailDialog = TripDetailDialog.RENAME,
                    tripDetailActionTripPlanId = intent.tripPlanId,
                    tripDetailNameDraft = intent.currentTitle,
                    tripDetailActionError = null,
                )
            }
            is ScheduleIntent.ShowTripDetailDeleteDialog -> reduce {
                copy(
                    tripDetailMenuExpanded = false,
                    tripDetailDialog = TripDetailDialog.DELETE,
                    tripDetailActionTripPlanId = intent.tripPlanId,
                    tripDetailActionError = null,
                )
            }
            is ScheduleIntent.UpdateTripDetailName -> reduce {
                if (
                    tripDetailDialog == TripDetailDialog.RENAME &&
                    !isTripDetailActionInProgress
                ) {
                    copy(
                        tripDetailNameDraft = intent.value.take(MaxTripPlanTitleLength),
                        tripDetailActionError = null,
                    )
                } else {
                    this
                }
            }
            ScheduleIntent.ConfirmTripDetailRename -> confirmTripDetailRename()
            ScheduleIntent.ConfirmTripDetailDelete -> confirmTripDetailDelete()
            ScheduleIntent.DismissTripDetailDialog -> dismissTripDetailDialog()

            // ---- 추천 영상 · 클립보드 · 튜토리얼 (FR-019 ~ FR-023) ----
            is ScheduleIntent.CopyRecommendedLink -> {
                postSideEffect(ScheduleSideEffect.WriteClipboard(intent.url))
                reduce {
                    copy(
                        clipboardToastUrl = intent.url,
                        hasClipboardText = true,
                        errorToast = null,
                        isGuideVisible = false,
                    )
                }
                advanceTutorialStep(from = TutorialStep.COPY_LINK, to = TutorialStep.PASTE_LINK)
            }
            is ScheduleIntent.ClipboardAvailabilityChanged -> reduce { copy(hasClipboardText = intent.hasText) }
            is ScheduleIntent.PasteFromClipboard -> applyLink(intent.text)
            ScheduleIntent.ApplyClipboardToast -> currentState.clipboardToastUrl?.let(::applyLink)
            ScheduleIntent.DismissClipboardToast -> reduce { copy(clipboardToastUrl = null) }
            ScheduleIntent.DismissErrorToast -> reduce { copy(errorToast = null) }
            ScheduleIntent.GuideDelayElapsed -> {
                guideDelayElapsed = true
                updateGuideVisibility()
            }
            ScheduleIntent.SkipOnboarding -> finishOnboarding(OnboardingCompletion.SKIPPED_IN_TUTORIAL)
            ScheduleIntent.ConfirmOnboardingSchedule -> finishOnboarding(OnboardingCompletion.TUTORIAL_FINISHED)
        }
    }

    private fun observeTutorialStep() {
        viewModelScope.launch {
            onboardingRepository.observeTutorialStep().collect { step ->
                container.mviContext.reduce { copy(tutorialStep = step) }
                updateGuideVisibility()
            }
        }
    }

    /** 3단계 코치마크는 단계가 `COPY_LINK`이고 추천 영상이 있고 진입 지연이 지났을 때만 보인다 (FR-020). */
    private fun updateGuideVisibility() {
        container.mviContext.reduce {
            copy(
                isGuideVisible = guideDelayElapsed &&
                    tutorialStep == TutorialStep.COPY_LINK &&
                    !isLoadingRecommendedVideos && recommendedVideosError == null && recommendedVideos.isNotEmpty(),
            )
        }
    }

    private fun advanceTutorialStep(from: TutorialStep, to: TutorialStep) {
        if (container.uiState.value.tutorialStep != from) return
        viewModelScope.launch {
            runCatching { onboardingRepository.setTutorialStep(to) }
        }
    }

    /** 붙여넣기 칩·클립보드 토스트로 링크를 채운다. 튜토리얼 4단계면 자유 조작 단계로 넘어간다 (FR-023). */
    private fun applyLink(text: String) {
        if (uiState.value.isSubmittingVideoLink || submissionJob?.isActive == true) return
        container.mviContext.reduce {
            copy(
                videoLink = text,
                videoLinkError = null,
                isSubmittingVideoLink = false,
                existingSchedule = null,
                clipboardToastUrl = null,
            )
        }
        advanceTutorialStep(from = TutorialStep.PASTE_LINK, to = TutorialStep.FREE)
    }

    /** 건너뛰기·`생성된 일정 확인하기`: 완료 기록 뒤 Activity 를 닫아 메인으로 돌아간다 (FR-013, FR-029). */
    private fun finishOnboarding(reason: OnboardingCompletion) {
        if (container.uiState.value.tutorialStep == null) return
        viewModelScope.launch {
            runCatching { completeOnboarding(reason) }
            container.mviContext.reduce { copy(isGuideVisible = false) }
            container.mviContext.postSideEffect(ScheduleSideEffect.FinishOnboarding)
        }
    }

    /** 온보딩 일정 생성(FR-024 ~ FR-026): 추천 영상 링크만 받아 분석 중 화면 없이 완료 화면으로 간다. */
    private fun MviContext<ScheduleUiState, ScheduleSideEffect>.submitOnboardingVideoLink() {
        if (submissionJob?.isActive == true) return
        val videoLink = currentState.videoLink
        val recommendedUrls = currentState.recommendedVideoUrls

        submissionJob = viewModelScope.launch {
            container.mviContext.reduce {
                copy(isSubmittingVideoLink = true, videoLinkError = null, errorToast = null)
            }
            try {
                when (val result = createOnboardingSchedule(videoLink, recommendedUrls)) {
                    CreateOnboardingScheduleResult.InvalidFormat -> container.mviContext.reduce {
                        copy(
                            isSubmittingVideoLink = false,
                            videoLinkError = VideoLinkError.WRONG_FORMAT,
                            errorToast = ScheduleEditStrings.ToastInvalid,
                        )
                    }
                    CreateOnboardingScheduleResult.NotRecommended -> container.mviContext.reduce {
                        copy(
                            isSubmittingVideoLink = false,
                            videoLinkError = VideoLinkError.INVALID_LINK,
                            errorToast = ScheduleEditStrings.ToastNotRecommended,
                        )
                    }
                    CreateOnboardingScheduleResult.NotReady -> container.mviContext.reduce {
                        copy(isSubmittingVideoLink = false, errorToast = ScheduleEditStrings.ToastNotReady)
                    }
                    is CreateOnboardingScheduleResult.Created -> {
                        container.mviContext.reduce { copy(isSubmittingVideoLink = false) }
                        container.mviContext.postSideEffect(ScheduleSideEffect.NavigateToAnalysisComplete)
                    }
                }
            } catch (error: CancellationException) {
                throw error
            } catch (_: Throwable) {
                container.mviContext.reduce {
                    copy(isSubmittingVideoLink = false, errorToast = ScheduleEditStrings.ToastNotReady)
                }
            }
        }
    }

    private fun loadRecommendedVideos() {
        if (recommendationsJob?.isActive == true) return
        container.mviContext.reduce {
            copy(isLoadingRecommendedVideos = true, recommendedVideosError = null, isGuideVisible = false)
        }
        recommendationsJob = viewModelScope.launch {
            try {
                // Wait for persisted tutorial mode before choosing its dedicated recommendation API.
                val onboarding = onboardingRepository.observeTutorialStep().first() != null
                val videos = if (onboarding) videoRepository.getOnboardingVideos() else getExploreVideos().items
                container.mviContext.reduce {
                    copy(
                        recommendedVideos = videos,
                        isLoadingRecommendedVideos = false,
                        recommendedVideosError = if (onboarding && videos.isEmpty()) ScheduleEditStrings.RecommendedError else null,
                    )
                }
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                container.mviContext.reduce {
                    copy(
                        isLoadingRecommendedVideos = false,
                        recommendedVideosError = "추천 영상을 불러오지 못했어요.",
                    )
                }
            }
            updateGuideVisibility()
        }
    }

    private fun MviContext<ScheduleUiState, ScheduleSideEffect>.dismissTripDetailDialog() {
        if (currentState.isTripDetailActionInProgress) return
        reduce { clearTripDetailAction() }
    }

    private fun MviContext<ScheduleUiState, ScheduleSideEffect>.confirmTripDetailRename() {
        val tripPlanId = currentState.tripDetailActionTripPlanId ?: return
        val title = currentState.tripDetailNameDraft
        if (
            currentState.tripDetailDialog != TripDetailDialog.RENAME ||
            currentState.isTripDetailActionInProgress ||
            title.isBlank()
        ) {
            return
        }

        reduce { copy(isTripDetailActionInProgress = true, tripDetailActionError = null) }
        viewModelScope.launch {
            try {
                val updated = renameTripPlan(tripPlanId, title)
                container.mviContext.reduce {
                    clearTripDetailAction().copy(
                        renamedTripPlanId = tripPlanId,
                        renamedTripPlanTitle = updated.title,
                    )
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Throwable) {
                showTripDetailActionError(error, "일정 이름을 수정하지 못했어요.")
            }
        }
    }

    private fun MviContext<ScheduleUiState, ScheduleSideEffect>.confirmTripDetailDelete() {
        val tripPlanId = currentState.tripDetailActionTripPlanId ?: return
        if (
            currentState.tripDetailDialog != TripDetailDialog.DELETE ||
            currentState.isTripDetailActionInProgress
        ) {
            return
        }

        reduce { copy(isTripDetailActionInProgress = true, tripDetailActionError = null) }
        viewModelScope.launch {
            try {
                deleteTripPlan(tripPlanId)
                container.mviContext.reduce { clearTripDetailAction() }
                container.mviContext.postSideEffect(ScheduleSideEffect.TripPlanDeleted)
            } catch (error: CancellationException) {
                throw error
            } catch (error: Throwable) {
                showTripDetailActionError(error, "일정을 삭제하지 못했어요.")
            }
        }
    }

    private fun showTripDetailActionError(error: Throwable, fallback: String) {
        val message = (error as? LinkTripApiException)
            ?.message
            ?.takeIf(String::isNotBlank)
            ?: fallback
        container.mviContext.reduce {
            copy(
                isTripDetailActionInProgress = false,
                tripDetailActionError = message,
            )
        }
    }

    private fun ScheduleUiState.clearTripDetailAction() = copy(
        tripDetailMenuExpanded = false,
        tripDetailDialog = null,
        tripDetailActionTripPlanId = null,
        tripDetailNameDraft = "",
        isTripDetailActionInProgress = false,
        tripDetailActionError = null,
    )

    private fun MviContext<ScheduleUiState, ScheduleSideEffect>.submitVideoLink(
        allowDuplicate: Boolean,
    ) {
        if (submissionJob?.isActive == true) return
        val videoLink = currentState.videoLink

        submissionJob = viewModelScope.launch {
            container.mviContext.reduce {
                copy(
                    isSubmittingVideoLink = true,
                    videoLinkError = null,
                    existingSchedule = null,
                )
            }

            try {
                when (
                    val result = startVideoScheduleCreation(
                        youtubeUrl = videoLink,
                        allowDuplicate = allowDuplicate,
                    )
                ) {
                    StartVideoScheduleCreationResult.InvalidFormat -> {
                        showVideoLinkError(VideoLinkError.WRONG_FORMAT)
                    }
                    is StartVideoScheduleCreationResult.AlreadyInProgress -> {
                        showVideoLinkError(VideoLinkError.ALREADY_IN_PROGRESS)
                    }
                    is StartVideoScheduleCreationResult.ExistingSchedule -> {
                        container.mviContext.reduce {
                            copy(
                                isSubmittingVideoLink = false,
                                existingSchedule = ExistingScheduleUiModel(
                                    tripPlanId = result.tripPlanId,
                                    title = result.title,
                                ),
                            )
                        }
                    }
                    is StartVideoScheduleCreationResult.AnalysisStarted -> {
                        if (
                            result.analysis.isInProgress ||
                            result.analysis.isValid && result.analysis.status == VideoAnalysisStatus.COMPLETED
                        ) {
                            container.mviContext.reduce { copy(isSubmittingVideoLink = false) }
                            container.mviContext.postSideEffect(
                                ScheduleSideEffect.NavigateToAnalysis(
                                    videoTitle = result.metadata?.title,
                                    thumbnailUrl = result.metadata?.thumbnailUrl,
                                ),
                            )
                        } else {
                            showVideoLinkError(VideoLinkError.INVALID_LINK)
                        }
                    }
                }
            } catch (error: CancellationException) {
                throw error
            } catch (_: Throwable) {
                showVideoLinkError(VideoLinkError.INVALID_LINK)
            }
        }
    }

    private fun showVideoLinkError(error: VideoLinkError) {
        container.mviContext.reduce {
            copy(
                isSubmittingVideoLink = false,
                videoLinkError = error,
            )
        }
    }

    override fun onCleared() {
        submissionJob?.cancel()
        recommendationsJob?.cancel()
        container.close()
        super.onCleared()
    }

    private companion object {
        const val MaxTripPlanTitleLength = 20
    }
}

package com.linkit.company.feature.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.linkit.company.core.common.architecture.MviContainer
import com.linkit.company.core.common.architecture.MviContext
import com.linkit.company.domain.exception.LinkTripApiException
import com.linkit.company.domain.model.video.VideoAnalysisStatus
import com.linkit.company.domain.usecase.DeleteTripPlanUseCase
import com.linkit.company.domain.usecase.RenameTripPlanUseCase
import com.linkit.company.domain.usecase.StartVideoScheduleCreationResult
import com.linkit.company.domain.usecase.StartVideoScheduleCreationUseCase
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@ContributesIntoMap(AppScope::class)
@ViewModelKey(ScheduleViewModel::class)
@Inject
class ScheduleViewModel(
    private val startVideoScheduleCreation: StartVideoScheduleCreationUseCase,
    private val renameTripPlan: RenameTripPlanUseCase,
    private val deleteTripPlan: DeleteTripPlanUseCase,
) : ViewModel() {
    private val container = MviContainer<ScheduleIntent, ScheduleSideEffect, ScheduleUiState>(
        initialState = ScheduleUiState(),
        onIntent = { handleIntent(it) },
    )
    private var submissionJob: Job? = null

    val uiState = container.uiState
    val sideEffect = container.sideEffect

    fun onIntent(intent: ScheduleIntent) = container.intent(intent)

    private fun MviContext<ScheduleUiState, ScheduleSideEffect>.handleIntent(intent: ScheduleIntent) {
        when (intent) {
            is ScheduleIntent.UpdateVideoLink -> {
                submissionJob?.cancel()
                reduce {
                    copy(
                        videoLink = intent.link,
                        videoLinkError = null,
                        isSubmittingVideoLink = false,
                        existingSchedule = null,
                    )
                }
            }
            ScheduleIntent.SubmitVideoLink -> submitVideoLink(allowDuplicate = false)
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
                            result.analysis.isValid &&
                            result.analysis.status != VideoAnalysisStatus.INVALID &&
                            result.analysis.status != VideoAnalysisStatus.FAILED
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
        container.close()
        super.onCleared()
    }

    private companion object {
        const val MaxTripPlanTitleLength = 20
    }
}

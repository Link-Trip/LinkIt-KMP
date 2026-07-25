package com.linkit.company.feature.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.linkit.company.core.common.architecture.MviContainer
import com.linkit.company.core.common.architecture.MviContext
import com.linkit.company.domain.model.video.VideoAnalysisStatus
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
            is ScheduleIntent.CopyRecommendedLink -> reduce {
                copy(
                    copiedRecommendedIndex = intent.index,
                    videoLinkError = null,
                )
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
        }
    }

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
                            container.mviContext.postSideEffect(ScheduleSideEffect.NavigateToAnalysis)
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
}

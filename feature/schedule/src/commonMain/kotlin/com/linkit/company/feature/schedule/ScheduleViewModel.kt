package com.linkit.company.feature.schedule

import androidx.lifecycle.ViewModel
import com.linkit.company.core.common.architecture.MviContainer
import com.linkit.company.core.common.architecture.MviContext
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey

@ContributesIntoMap(AppScope::class)
@ViewModelKey(ScheduleViewModel::class)
@Inject
class ScheduleViewModel : ViewModel() {
    private val container = MviContainer<ScheduleIntent, ScheduleSideEffect, ScheduleUiState>(
        initialState = ScheduleUiState(),
        onIntent = { handleIntent(it) },
    )

    val uiState = container.uiState

    fun onIntent(intent: ScheduleIntent) = container.intent(intent)

    private fun MviContext<ScheduleUiState, ScheduleSideEffect>.handleIntent(intent: ScheduleIntent) {
        when (intent) {
            is ScheduleIntent.UpdateVideoLink -> reduce {
                copy(videoLink = intent.link, showInvalidLinkMessage = false)
            }
            is ScheduleIntent.CopyRecommendedLink -> reduce {
                copy(
                    videoLink = "https://youtu.be/pingo-travel-${intent.index + 1}",
                    copiedRecommendedIndex = intent.index,
                    showInvalidLinkMessage = false,
                )
            }
            ScheduleIntent.SubmitVideoLink -> reduce {
                copy(showInvalidLinkMessage = !videoLink.contains("youtu"))
            }
            ScheduleIntent.DismissInvalidLink -> reduce { copy(showInvalidLinkMessage = false) }
        }
    }

    override fun onCleared() {
        container.close()
        super.onCleared()
    }
}

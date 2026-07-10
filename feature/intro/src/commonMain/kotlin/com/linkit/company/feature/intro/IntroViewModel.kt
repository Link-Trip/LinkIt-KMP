package com.linkit.company.feature.intro

import androidx.lifecycle.ViewModel
import com.linkit.company.core.common.architecture.MviContainer
import com.linkit.company.core.common.architecture.MviContext
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey

@ContributesIntoMap(AppScope::class)
@ViewModelKey(IntroViewModel::class)
@Inject
class IntroViewModel : ViewModel() {
    private val container = MviContainer<IntroIntent, IntroSideEffect, IntroUiState>(
        initialState = IntroUiState(),
        onIntent = { handleIntent(it) },
    )

    val uiState = container.uiState

    fun onIntent(intent: IntroIntent) = container.intent(intent)

    private fun MviContext<IntroUiState, IntroSideEffect>.handleIntent(intent: IntroIntent) {
        when (intent) {
            IntroIntent.ZoomToSeoul -> reduce { copy(stage = IntroStage.SEOUL) }
        }
    }

    override fun onCleared() {
        container.close()
        super.onCleared()
    }
}

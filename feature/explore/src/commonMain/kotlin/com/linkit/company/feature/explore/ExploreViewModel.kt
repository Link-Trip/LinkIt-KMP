package com.linkit.company.feature.explore

import androidx.lifecycle.ViewModel
import com.linkit.company.core.common.architecture.MviContainer
import com.linkit.company.core.common.architecture.MviContext
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey

@ContributesIntoMap(AppScope::class)
@ViewModelKey(ExploreViewModel::class)
@Inject
class ExploreViewModel : ViewModel() {
    private val container = MviContainer<ExploreIntent, ExploreSideEffect, ExploreUiState>(
        initialState = ExploreUiState(),
        onIntent = { handleIntent(it) },
    )

    val uiState = container.uiState

    fun onIntent(intent: ExploreIntent) = container.intent(intent)

    private fun MviContext<ExploreUiState, ExploreSideEffect>.handleIntent(intent: ExploreIntent) {
        when (intent) {
            is ExploreIntent.SelectCountry -> reduce { copy(selectedCountry = intent.country) }
            is ExploreIntent.SelectTab -> reduce { copy(selectedTab = intent.tab) }
            is ExploreIntent.SelectTheme -> reduce { copy(selectedTheme = intent.theme) }
        }
    }

    override fun onCleared() {
        container.close()
        super.onCleared()
    }
}

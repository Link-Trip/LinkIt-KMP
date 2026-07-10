package com.linkit.company.feature.storage

import androidx.lifecycle.ViewModel
import com.linkit.company.core.common.architecture.MviContainer
import com.linkit.company.core.common.architecture.MviContext
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey

@ContributesIntoMap(AppScope::class)
@ViewModelKey(StorageViewModel::class)
@Inject
class StorageViewModel : ViewModel() {
    private val container = MviContainer<StorageIntent, StorageSideEffect, StorageUiState>(
        initialState = StorageUiState(),
        onIntent = { handleIntent(it) },
    )

    val uiState = container.uiState

    fun onIntent(intent: StorageIntent) = container.intent(intent)

    private fun MviContext<StorageUiState, StorageSideEffect>.handleIntent(intent: StorageIntent) {
        when (intent) {
            StorageIntent.ToggleAddMenu -> reduce { copy(isAddMenuVisible = !isAddMenuVisible) }
            StorageIntent.DismissAddMenu -> reduce { copy(isAddMenuVisible = false) }
        }
    }

    override fun onCleared() {
        container.close()
        super.onCleared()
    }
}

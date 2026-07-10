package com.linkit.company.feature.storage

import com.linkit.company.core.common.architecture.contract.Intent

sealed interface StorageIntent : Intent {
    data class UpdateQuery(val value: String) : StorageIntent
    data object ToggleAddMenu : StorageIntent
    data object DismissAddMenu : StorageIntent
}

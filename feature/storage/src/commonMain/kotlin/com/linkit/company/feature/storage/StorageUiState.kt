package com.linkit.company.feature.storage

import com.linkit.company.core.common.architecture.contract.UiState

data class StorageUiState(
    val isAddMenuVisible: Boolean = false,
) : UiState

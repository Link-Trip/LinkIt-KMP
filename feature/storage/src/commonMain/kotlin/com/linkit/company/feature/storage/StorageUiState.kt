package com.linkit.company.feature.storage

import com.linkit.company.core.common.architecture.contract.UiState

data class StorageUiState(
    val query: String = "일본",
    val videoUrl: String = "",
    val isAddMenuVisible: Boolean = false,
) : UiState

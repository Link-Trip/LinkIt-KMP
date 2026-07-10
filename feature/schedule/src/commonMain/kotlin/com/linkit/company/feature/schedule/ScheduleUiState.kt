package com.linkit.company.feature.schedule

import com.linkit.company.core.common.architecture.contract.UiState

data class ScheduleUiState(
    val videoLink: String = "",
    val copiedRecommendedIndex: Int? = null,
    val showInvalidLinkMessage: Boolean = false,
) : UiState {
    val canCreate: Boolean get() = videoLink.isNotBlank()
}

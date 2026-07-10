package com.linkit.company.feature.intro

import com.linkit.company.core.common.architecture.contract.UiState

enum class IntroStage {
    GLOBE,
    SEOUL,
}

data class IntroUiState(
    val stage: IntroStage = IntroStage.GLOBE,
) : UiState

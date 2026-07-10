package com.linkit.company.feature.map.mypage

import com.linkit.company.core.common.architecture.contract.UiState

enum class MyPageMapType {
    DEFAULT,
    SATELLITE,
}

data class MyPageUiState(
    val selectedMapType: MyPageMapType = MyPageMapType.DEFAULT,
    val showResetDialog: Boolean = false,
) : UiState

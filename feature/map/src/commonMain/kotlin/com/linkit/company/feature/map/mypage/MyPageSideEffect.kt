package com.linkit.company.feature.map.mypage

import com.linkit.company.core.common.architecture.contract.SideEffect

enum class MyPageToastType {
    SUCCESS,
    ERROR,
}

sealed interface MyPageSideEffect : SideEffect {
    data class ShowToast(val message: String, val type: MyPageToastType) : MyPageSideEffect
    data object NavigateToNotificationSettings : MyPageSideEffect
    data object AppResetCompleted : MyPageSideEffect
}

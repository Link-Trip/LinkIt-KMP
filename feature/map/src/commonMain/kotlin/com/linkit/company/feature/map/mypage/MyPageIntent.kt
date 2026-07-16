package com.linkit.company.feature.map.mypage

import com.linkit.company.core.common.architecture.contract.Intent

sealed interface MyPageIntent : Intent {
    data class SelectMapType(val type: MyPageMapType) : MyPageIntent
    data object ShowResetDialog : MyPageIntent
    data object DismissResetDialog : MyPageIntent
}

package com.linkit.company.feature.map.mypage

import com.linkit.company.core.common.architecture.contract.Intent
import com.linkit.company.domain.model.feedback.FeedbackType
import com.linkit.company.domain.model.settings.MapDisplayType

sealed interface MyPageIntent : Intent {
    data class SelectMapDisplayType(val type: MapDisplayType) : MyPageIntent

    /** 기기 알림 허용 여부를 새로 조회한 결과. null 은 조회 불가. */
    data class RefreshNotificationStatus(val enabled: Boolean?) : MyPageIntent
    data object OpenNotificationSettings : MyPageIntent

    data object OpenFeedbackSheet : MyPageIntent
    data object CloseFeedbackSheet : MyPageIntent
    data class SelectFeedbackType(val type: FeedbackType) : MyPageIntent
    data class ChangeFeedbackContent(val text: String) : MyPageIntent
    data object SendFeedback : MyPageIntent

    data object ShowResetDialog : MyPageIntent
    data object DismissResetDialog : MyPageIntent
    data object ConfirmReset : MyPageIntent
}

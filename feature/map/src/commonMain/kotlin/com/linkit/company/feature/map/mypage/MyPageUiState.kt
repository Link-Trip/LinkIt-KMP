package com.linkit.company.feature.map.mypage

import com.linkit.company.core.common.architecture.contract.UiState
import com.linkit.company.domain.model.feedback.FeedbackType
import com.linkit.company.domain.model.settings.MapDisplayType

/** 기기 알림 허용 상태. [UNKNOWN] 은 아직 조회하지 않았거나 조회에 실패한 경우다. */
enum class NotificationStatus {
    UNKNOWN,
    ENABLED,
    DISABLED,
}

/**
 * 의견 보내기 바텀시트 상태. `MyPageUiState.feedbackSheet` 가 null 이면 시트가 닫힌 상태다.
 *
 * @property selectedType 선택한 유형. 미선택(null)으로 전송하면 `FeedbackType.ETC` 로 기록한다.
 * @property content 입력 내용(최대 [MaxLength]자)
 * @property isSending 전송 진행 중 여부
 */
data class FeedbackSheetState(
    val selectedType: FeedbackType? = null,
    val content: String = "",
    val isSending: Boolean = false,
) {
    /** 공백을 제외한 내용이 1자 이상이고 전송 중이 아닐 때만 보낼 수 있다. */
    val canSend: Boolean get() = content.isNotBlank() && !isSending

    companion object {
        const val MaxLength = 200
    }
}

/**
 * @property notificationStatus 기기 알림 권한. 토글의 활성 여부와 안내 카드 표시를 정한다
 * @property isNotificationEnabled 앱 알림 수신 설정(서버 값의 로컬 캐시). 권한이 켜져 있을 때 토글의 on/off를 정한다
 * @property isNotificationUpdating 수신 설정을 서버에 반영하는 중. 이 동안 토글 재탭은 무시한다
 */
data class MyPageUiState(
    val mapDisplayType: MapDisplayType = MapDisplayType.DEFAULT,
    val notificationStatus: NotificationStatus = NotificationStatus.UNKNOWN,
    val isNotificationEnabled: Boolean = true,
    val isNotificationUpdating: Boolean = false,
    val isResetDialogVisible: Boolean = false,
    val isResetInProgress: Boolean = false,
    val feedbackSheet: FeedbackSheetState? = null,
) : UiState {
    /** 기기 권한이 켜져 있을 때만 토글을 조작할 수 있다. */
    val isNotificationSwitchEnabled: Boolean get() = notificationStatus == NotificationStatus.ENABLED

    /** 권한이 꺼져 있거나 조회 불가면 수신 설정과 무관하게 항상 off로 보인다. */
    val isNotificationSwitchChecked: Boolean get() = isNotificationSwitchEnabled && isNotificationEnabled
}

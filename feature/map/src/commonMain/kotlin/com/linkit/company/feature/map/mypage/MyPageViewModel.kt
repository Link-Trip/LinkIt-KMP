package com.linkit.company.feature.map.mypage

import androidx.lifecycle.ViewModel
import com.linkit.company.core.common.architecture.MviContainer
import com.linkit.company.core.common.architecture.MviContext
import com.linkit.company.domain.model.settings.MapDisplayType
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey

/**
 * 마이페이지 ViewModel.
 *
 * 화면 로컬 상태(시트·팝업·칩·입력·알림 상태)와 지도 설정 선택 토스트를 처리한다.
 * 저장소 연동(지도 설정 영속화·의견 전송·앱 초기화 실행)은 데이터 연동 이슈(#45)에서 주입한다.
 */
@ContributesIntoMap(AppScope::class)
@ViewModelKey(MyPageViewModel::class)
@Inject
class MyPageViewModel : ViewModel() {
    private val container = MviContainer<MyPageIntent, MyPageSideEffect, MyPageUiState>(
        initialState = MyPageUiState(),
        onIntent = { handleIntent(it) },
    )

    val uiState = container.uiState
    val sideEffect = container.sideEffect

    fun onIntent(intent: MyPageIntent) = container.intent(intent)

    private fun MviContext<MyPageUiState, MyPageSideEffect>.handleIntent(intent: MyPageIntent) {
        when (intent) {
            is MyPageIntent.SelectMapDisplayType -> selectMapDisplayType(intent.type)
            is MyPageIntent.RefreshNotificationStatus -> reduce {
                copy(
                    notificationStatus = when (intent.enabled) {
                        true -> NotificationStatus.ENABLED
                        false -> NotificationStatus.DISABLED
                        null -> NotificationStatus.UNKNOWN
                    },
                )
            }
            MyPageIntent.OpenNotificationSettings -> postSideEffect(MyPageSideEffect.NavigateToNotificationSettings)

            MyPageIntent.OpenFeedbackSheet -> reduce { copy(feedbackSheet = FeedbackSheetState()) }
            MyPageIntent.CloseFeedbackSheet -> reduce {
                if (feedbackSheet?.isSending == true) this else copy(feedbackSheet = null)
            }
            is MyPageIntent.SelectFeedbackType -> updateFeedbackSheet {
                copy(selectedType = if (selectedType == intent.type) null else intent.type)
            }
            is MyPageIntent.ChangeFeedbackContent -> updateFeedbackSheet {
                if (intent.text.length <= FeedbackSheetState.MaxLength) copy(content = intent.text) else this
            }
            MyPageIntent.SendFeedback -> sendFeedback()

            MyPageIntent.ShowResetDialog -> reduce { copy(isResetDialogVisible = true) }
            MyPageIntent.DismissResetDialog -> reduce {
                if (isResetInProgress) this else copy(isResetDialogVisible = false)
            }
            MyPageIntent.ConfirmReset -> confirmReset()
        }
    }

    private fun MviContext<MyPageUiState, MyPageSideEffect>.selectMapDisplayType(type: MapDisplayType) {
        if (currentState.mapDisplayType == type) return
        // TODO(#45): AppSettingsRepository.setMapDisplayType(type) 후 Flow 구독값으로 반영
        reduce { copy(mapDisplayType = type) }
        val message = when (type) {
            MapDisplayType.DEFAULT -> MyPageStrings.ToastMapDefault
            MapDisplayType.SATELLITE -> MyPageStrings.ToastMapSatellite
        }
        postSideEffect(MyPageSideEffect.ShowToast(message, MyPageToastType.SUCCESS))
    }

    private fun MviContext<MyPageUiState, MyPageSideEffect>.sendFeedback() {
        val sheet = currentState.feedbackSheet ?: return
        if (!sheet.canSend) return
        // TODO(#45): SendFeedbackUseCase(sheet.selectedType, sheet.content) 연동.
        //  성공 → 시트 닫기 + ToastFeedbackSuccess / 429 → ToastFeedbackLimit / 그 외 → ToastFeedbackFailure(시트 유지)
        reduce { copy(feedbackSheet = null) }
        postSideEffect(MyPageSideEffect.ShowToast(MyPageStrings.ToastFeedbackSuccess, MyPageToastType.SUCCESS))
    }

    private fun MviContext<MyPageUiState, MyPageSideEffect>.confirmReset() {
        if (!currentState.isResetDialogVisible || currentState.isResetInProgress) return
        // TODO(#45): ResetAppUseCase 연동. 성공 → AppResetCompleted / 실패 → 팝업 닫고 ToastResetFailure
        reduce { copy(isResetDialogVisible = false, isResetInProgress = false) }
        postSideEffect(MyPageSideEffect.AppResetCompleted)
    }

    private inline fun MviContext<MyPageUiState, MyPageSideEffect>.updateFeedbackSheet(
        crossinline action: FeedbackSheetState.() -> FeedbackSheetState,
    ) {
        reduce {
            val sheet = feedbackSheet ?: return@reduce this
            if (sheet.isSending) this else copy(feedbackSheet = sheet.action())
        }
    }

    override fun onCleared() {
        container.close()
        super.onCleared()
    }
}

package com.linkit.company.feature.map.mypage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.linkit.company.core.common.architecture.MviContainer
import com.linkit.company.core.common.architecture.MviContext
import com.linkit.company.domain.exception.LinkTripApiException
import com.linkit.company.domain.exception.LinkTripErrorCode
import com.linkit.company.domain.model.settings.MapDisplayType
import com.linkit.company.domain.repository.AppSettingsRepository
import com.linkit.company.domain.usecase.FetchNotificationSettingUseCase
import com.linkit.company.domain.usecase.ResetAppUseCase
import com.linkit.company.domain.usecase.SendFeedbackUseCase
import com.linkit.company.domain.usecase.UpdateNotificationSettingUseCase
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * 마이페이지 ViewModel.
 *
 * - 지도 설정: 저장소 Flow를 구독해 반영하고, 선택 시 저장 후 토스트를 낸다.
 * - 알림: 기기 권한은 플랫폼 조회값을 표시만 한다(서버 호출 없음). 앱 알림 수신 설정은 저장소 Flow를 구독해 표시하고,
 *   진입 시 서버 값을 한 번 읽어 로컬을 맞추며, 권한이 켜진 상태의 토글 탭은 낙관적으로 반영한 뒤 서버에 저장한다.
 * - 의견 전송·앱 초기화: UseCase 결과에 따라 토스트·SideEffect를 낸다.
 */
@ContributesIntoMap(AppScope::class)
@ViewModelKey(MyPageViewModel::class)
@Inject
class MyPageViewModel(
    private val appSettingsRepository: AppSettingsRepository,
    private val sendFeedback: SendFeedbackUseCase,
    private val resetApp: ResetAppUseCase,
    private val updateNotificationSetting: UpdateNotificationSettingUseCase,
    fetchNotificationSetting: FetchNotificationSettingUseCase,
) : ViewModel() {
    private val container = MviContainer<MyPageIntent, MyPageSideEffect, MyPageUiState>(
        initialState = MyPageUiState(),
        onIntent = { handleIntent(it) },
    )

    /** 저장 요청은 했지만 Flow에 아직 반영되지 않은 값. 연타 시 중복 저장·토스트를 막는다. */
    private var pendingMapDisplayType: MapDisplayType? = null

    /** 진입 시 서버 알림 수신 설정 조회. 사용자가 토글을 조작하면 취소해 뒤늦은 응답이 사용자 의사를 덮지 않게 한다. */
    private val fetchNotificationJob: Job

    val uiState = container.uiState
    val sideEffect = container.sideEffect

    init {
        viewModelScope.launch {
            appSettingsRepository.observeMapDisplayType().collect { type ->
                if (pendingMapDisplayType == type) pendingMapDisplayType = null
                container.mviContext.reduce { copy(mapDisplayType = type) }
            }
        }
        viewModelScope.launch {
            appSettingsRepository.observeNotificationEnabled().collect { enabled ->
                // 서버 반영 중에는 낙관적 반영값을 유지한다. 반영 결과는 완료 시점에 직접 기록한다
                container.mviContext.reduce {
                    if (isNotificationUpdating) this else copy(isNotificationEnabled = enabled)
                }
            }
        }
        fetchNotificationJob = viewModelScope.launch { fetchNotificationSetting() }
    }

    fun onIntent(intent: MyPageIntent) = container.intent(intent)

    private fun MviContext<MyPageUiState, MyPageSideEffect>.handleIntent(intent: MyPageIntent) {
        when (intent) {
            is MyPageIntent.SelectMapDisplayType -> selectMapDisplayType(intent.type)
            is MyPageIntent.RefreshNotificationStatus -> refreshNotificationStatus(intent.enabled)
            MyPageIntent.OpenNotificationSettings -> postSideEffect(MyPageSideEffect.NavigateToNotificationSettings)
            MyPageIntent.ToggleNotificationEnabled -> toggleNotificationEnabled()

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
        val effective = pendingMapDisplayType ?: currentState.mapDisplayType
        if (effective == type) return
        pendingMapDisplayType = type
        viewModelScope.launch {
            appSettingsRepository.setMapDisplayType(type)
            val message = when (type) {
                MapDisplayType.DEFAULT -> MyPageStrings.ToastMapDefault
                MapDisplayType.SATELLITE -> MyPageStrings.ToastMapSatellite
            }
            container.mviContext.postSideEffect(MyPageSideEffect.ShowToast(message, MyPageToastType.SUCCESS))
        }
    }

    private fun MviContext<MyPageUiState, MyPageSideEffect>.refreshNotificationStatus(enabled: Boolean?) {
        reduce {
            copy(
                notificationStatus = when (enabled) {
                    true -> NotificationStatus.ENABLED
                    false -> NotificationStatus.DISABLED
                    null -> NotificationStatus.UNKNOWN
                },
            )
        }
    }

    private fun MviContext<MyPageUiState, MyPageSideEffect>.toggleNotificationEnabled() {
        val state = currentState
        if (!state.isNotificationSwitchEnabled || state.isNotificationUpdating) return
        fetchNotificationJob.cancel()
        val previous = state.isNotificationEnabled
        val target = !previous
        reduce { copy(isNotificationEnabled = target, isNotificationUpdating = true) }
        viewModelScope.launch {
            try {
                updateNotificationSetting(target)
                container.mviContext.reduce { copy(isNotificationEnabled = target, isNotificationUpdating = false) }
            } catch (error: CancellationException) {
                throw error
            } catch (_: Throwable) {
                container.mviContext.reduce { copy(isNotificationEnabled = previous, isNotificationUpdating = false) }
                container.mviContext.postSideEffect(
                    MyPageSideEffect.ShowToast(MyPageStrings.ToastNotificationFailure, MyPageToastType.ERROR),
                )
            }
        }
    }

    private fun MviContext<MyPageUiState, MyPageSideEffect>.sendFeedback() {
        val sheet = currentState.feedbackSheet ?: return
        if (!sheet.canSend) return
        reduce { copy(feedbackSheet = sheet.copy(isSending = true)) }
        viewModelScope.launch {
            try {
                sendFeedback(sheet.selectedType, sheet.content)
                container.mviContext.reduce { copy(feedbackSheet = null) }
                container.mviContext.postSideEffect(
                    MyPageSideEffect.ShowToast(MyPageStrings.ToastFeedbackSuccess, MyPageToastType.SUCCESS),
                )
            } catch (error: CancellationException) {
                throw error
            } catch (error: Throwable) {
                val message = if (
                    error is LinkTripApiException &&
                    error.errorCode == LinkTripErrorCode.FEEDBACK_DAILY_LIMIT_EXCEEDED
                ) {
                    MyPageStrings.ToastFeedbackLimit
                } else {
                    MyPageStrings.ToastFeedbackFailure
                }
                // 시트와 입력 내용은 유지해 재시도할 수 있게 한다
                container.mviContext.reduce {
                    copy(feedbackSheet = feedbackSheet?.copy(isSending = false))
                }
                container.mviContext.postSideEffect(MyPageSideEffect.ShowToast(message, MyPageToastType.ERROR))
            }
        }
    }

    private fun MviContext<MyPageUiState, MyPageSideEffect>.confirmReset() {
        if (!currentState.isResetDialogVisible || currentState.isResetInProgress) return
        reduce { copy(isResetInProgress = true) }
        viewModelScope.launch {
            try {
                resetApp()
                container.mviContext.reduce { copy(isResetDialogVisible = false, isResetInProgress = false) }
                container.mviContext.postSideEffect(MyPageSideEffect.AppResetCompleted)
            } catch (error: CancellationException) {
                throw error
            } catch (_: Throwable) {
                container.mviContext.reduce { copy(isResetDialogVisible = false, isResetInProgress = false) }
                container.mviContext.postSideEffect(
                    MyPageSideEffect.ShowToast(MyPageStrings.ToastResetFailure, MyPageToastType.ERROR),
                )
            }
        }
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

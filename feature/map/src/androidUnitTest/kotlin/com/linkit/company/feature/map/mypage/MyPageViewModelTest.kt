package com.linkit.company.feature.map.mypage

import android.os.Looper
import com.linkit.company.domain.exception.LinkTripApiException
import com.linkit.company.domain.exception.LinkTripErrorCode
import com.linkit.company.domain.model.app.AppInfo
import com.linkit.company.domain.model.auth.Auth
import com.linkit.company.domain.model.feedback.FeedbackType
import com.linkit.company.domain.model.member.NotificationSetting
import com.linkit.company.domain.model.settings.MapDisplayType
import com.linkit.company.domain.repository.AppInfoRepository
import com.linkit.company.domain.repository.AuthRepository
import com.linkit.company.domain.repository.FeedbackRepository
import com.linkit.company.domain.repository.MemberRepository
import com.linkit.company.domain.usecase.EnsureAuthenticatedUseCase
import com.linkit.company.domain.usecase.FetchNotificationSettingUseCase
import com.linkit.company.domain.usecase.ResetAppUseCase
import com.linkit.company.domain.usecase.SendFeedbackUseCase
import com.linkit.company.domain.usecase.UpdateNotificationSettingUseCase
import com.linkit.company.feature.map.testing.FakeAppSettingsRepository
import com.linkit.company.domain.model.common.CursorPage
import com.linkit.company.domain.model.tripplan.TripPlanDetail
import com.linkit.company.domain.model.tripplan.TripPlanItemOrder
import com.linkit.company.domain.model.tripplan.TripPlanSummary
import com.linkit.company.domain.repository.TripPlanRepository
import com.linkit.company.feature.map.testing.FakeOnboardingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.launch
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class MyPageViewModelTest {
    private val effects = mutableListOf<MyPageSideEffect>()
    private var collectJob: Job? = null

    @After
    fun tearDown() {
        collectJob?.cancel()
    }

    // ---- US1 지도 설정 ----

    @Test
    fun initialMapDisplayTypeComesFromRepository() {
        val settings = FakeAppSettingsRepository(initial = MapDisplayType.SATELLITE)
        val viewModel = createViewModel(settings = settings)
        idle()

        assertEquals(MapDisplayType.SATELLITE, viewModel.uiState.value.mapDisplayType)
    }

    @Test
    fun selectingDifferentMapTypeSavesAndShowsToast() {
        val settings = FakeAppSettingsRepository()
        val viewModel = createViewModel(settings = settings)

        viewModel.onIntent(MyPageIntent.SelectMapDisplayType(MapDisplayType.SATELLITE))
        idle()

        assertEquals(listOf(MapDisplayType.SATELLITE), settings.savedMapDisplayTypes)
        assertEquals(MapDisplayType.SATELLITE, viewModel.uiState.value.mapDisplayType)
        assertEquals(
            listOf(MyPageSideEffect.ShowToast(MyPageStrings.ToastMapSatellite, MyPageToastType.SUCCESS)),
            effects,
        )
    }

    @Test
    fun selectingSameMapTypeIsIgnored() {
        val settings = FakeAppSettingsRepository()
        val viewModel = createViewModel(settings = settings)
        idle()

        viewModel.onIntent(MyPageIntent.SelectMapDisplayType(MapDisplayType.DEFAULT))
        idle()

        assertTrue(settings.savedMapDisplayTypes.isEmpty())
        assertTrue(effects.isEmpty())
    }

    // ---- US2 앱 초기화 ----

    @Test
    fun confirmResetRunsUseCaseAndEmitsCompleted() {
        val settings = FakeAppSettingsRepository(initial = MapDisplayType.SATELLITE)
        val member = ScriptedMemberRepository()
        val viewModel = createViewModel(settings = settings, member = member)

        viewModel.onIntent(MyPageIntent.ShowResetDialog)
        viewModel.onIntent(MyPageIntent.ConfirmReset)
        idle()

        assertEquals(listOf("withdraw"), member.events.filter { it != "getNotification" })
        assertEquals(1, settings.clearAllCount)
        assertFalse(viewModel.uiState.value.isResetInProgress)
        assertFalse(viewModel.uiState.value.isResetDialogVisible)
        assertTrue(effects.contains(MyPageSideEffect.AppResetCompleted))
    }

    @Test
    fun confirmResetFailureClosesDialogAndShowsErrorToast() {
        val settings = FakeAppSettingsRepository(initial = MapDisplayType.SATELLITE)
        val member = ScriptedMemberRepository(
            withdrawResults = listOf(apiException(LinkTripErrorCode.DUPLICATE_REQUEST, 409)),
        )
        val viewModel = createViewModel(settings = settings, member = member)

        viewModel.onIntent(MyPageIntent.ShowResetDialog)
        viewModel.onIntent(MyPageIntent.ConfirmReset)
        idle()

        assertEquals(0, settings.clearAllCount)
        assertEquals(MapDisplayType.SATELLITE, viewModel.uiState.value.mapDisplayType)
        assertFalse(viewModel.uiState.value.isResetDialogVisible)
        assertFalse(viewModel.uiState.value.isResetInProgress)
        assertEquals(
            listOf(MyPageSideEffect.ShowToast(MyPageStrings.ToastResetFailure, MyPageToastType.ERROR)),
            effects,
        )
    }

    @Test
    fun dismissIsIgnoredWhileResetInProgress() {
        // 서버 응답이 오지 않는 동안(Hang) 팝업 조작이 막혀야 한다
        val viewModel = createViewModel(member = ScriptedMemberRepository(withdrawResults = listOf(Hang)))
        viewModel.onIntent(MyPageIntent.ShowResetDialog)
        viewModel.onIntent(MyPageIntent.ConfirmReset)
        idle()
        assertTrue(viewModel.uiState.value.isResetInProgress)

        viewModel.onIntent(MyPageIntent.DismissResetDialog)
        viewModel.onIntent(MyPageIntent.ConfirmReset)

        assertTrue(viewModel.uiState.value.isResetDialogVisible)
        assertTrue(viewModel.uiState.value.isResetInProgress)
    }

    // ---- US3 의견 보내기 ----

    @Test
    fun sendFeedbackSuccessClosesSheetAndShowsSuccessToast() {
        val feedback = ScriptedFeedbackRepository()
        val viewModel = createViewModel(feedback = feedback)

        viewModel.onIntent(MyPageIntent.OpenFeedbackSheet)
        viewModel.onIntent(MyPageIntent.ChangeFeedbackContent("  좋아요 "))
        viewModel.onIntent(MyPageIntent.SendFeedback)
        idle()

        val sent = feedback.sent.single()
        assertEquals(FeedbackType.ETC, sent.type)
        assertEquals("좋아요", sent.content)
        assertNull(viewModel.uiState.value.feedbackSheet)
        assertEquals(
            listOf(MyPageSideEffect.ShowToast(MyPageStrings.ToastFeedbackSuccess, MyPageToastType.SUCCESS)),
            effects,
        )
    }

    @Test
    fun sendFeedbackDailyLimitKeepsSheetAndShowsLimitToast() {
        val feedback = ScriptedFeedbackRepository(
            results = listOf(apiException(LinkTripErrorCode.FEEDBACK_DAILY_LIMIT_EXCEEDED, 429)),
        )
        val viewModel = createViewModel(feedback = feedback)

        viewModel.onIntent(MyPageIntent.OpenFeedbackSheet)
        viewModel.onIntent(MyPageIntent.SelectFeedbackType(FeedbackType.BUG))
        viewModel.onIntent(MyPageIntent.ChangeFeedbackContent("버그"))
        viewModel.onIntent(MyPageIntent.SendFeedback)
        idle()

        val sheet = requireNotNull(viewModel.uiState.value.feedbackSheet)
        assertEquals("버그", sheet.content)
        assertEquals(FeedbackType.BUG, sheet.selectedType)
        assertFalse(sheet.isSending)
        assertEquals(
            listOf(MyPageSideEffect.ShowToast(MyPageStrings.ToastFeedbackLimit, MyPageToastType.ERROR)),
            effects,
        )
    }

    @Test
    fun sendFeedbackRateLimitIsTreatedAsGenericFailure() {
        val feedback = ScriptedFeedbackRepository(
            results = listOf(apiException(LinkTripErrorCode.TOO_MANY_REQUESTS, 429)),
        )
        val viewModel = createViewModel(feedback = feedback)

        viewModel.onIntent(MyPageIntent.OpenFeedbackSheet)
        viewModel.onIntent(MyPageIntent.ChangeFeedbackContent("내용"))
        viewModel.onIntent(MyPageIntent.SendFeedback)
        idle()

        assertEquals("내용", viewModel.uiState.value.feedbackSheet?.content)
        assertEquals(
            listOf(MyPageSideEffect.ShowToast(MyPageStrings.ToastFeedbackFailure, MyPageToastType.ERROR)),
            effects,
        )
    }

    @Test
    fun sendFeedbackIsBlockedWhileSending() {
        // 첫 전송이 응답 대기 중(Hang)일 때 재전송·닫기·입력 변경이 모두 막혀야 한다
        val feedback = ScriptedFeedbackRepository(results = listOf(Hang))
        val viewModel = createViewModel(feedback = feedback)

        viewModel.onIntent(MyPageIntent.OpenFeedbackSheet)
        viewModel.onIntent(MyPageIntent.ChangeFeedbackContent("내용"))
        viewModel.onIntent(MyPageIntent.SendFeedback)
        idle()
        assertTrue(viewModel.uiState.value.feedbackSheet?.isSending == true)

        viewModel.onIntent(MyPageIntent.SendFeedback)
        viewModel.onIntent(MyPageIntent.CloseFeedbackSheet)
        viewModel.onIntent(MyPageIntent.ChangeFeedbackContent("변경"))
        idle()

        assertEquals(1, feedback.sent.size)
        assertEquals("내용", viewModel.uiState.value.feedbackSheet?.content)
    }

    @Test
    fun feedbackContentOver200CharactersIsIgnored() {
        val viewModel = createViewModel()

        viewModel.onIntent(MyPageIntent.OpenFeedbackSheet)
        viewModel.onIntent(MyPageIntent.ChangeFeedbackContent("a".repeat(200)))
        viewModel.onIntent(MyPageIntent.ChangeFeedbackContent("a".repeat(201)))

        assertEquals(200, viewModel.uiState.value.feedbackSheet?.content?.length)
    }

    // ---- US4 알림 ----

    @Test
    fun refreshNotificationStatusOnlyUpdatesStateWithoutServerCall() {
        val member = ScriptedMemberRepository()
        val viewModel = createViewModel(member = member)

        viewModel.onIntent(MyPageIntent.RefreshNotificationStatus(false))
        idle()
        assertEquals(NotificationStatus.DISABLED, viewModel.uiState.value.notificationStatus)
        viewModel.onIntent(MyPageIntent.RefreshNotificationStatus(true))
        idle()
        assertEquals(NotificationStatus.ENABLED, viewModel.uiState.value.notificationStatus)
        viewModel.onIntent(MyPageIntent.RefreshNotificationStatus(null))
        idle()

        assertEquals(NotificationStatus.UNKNOWN, viewModel.uiState.value.notificationStatus)
        assertTrue(member.notificationCalls.isEmpty())
    }

    @Test
    fun initialNotificationEnabledComesFromRepository() {
        // 서버 조회가 응답 전(Hang)이면 로컬 캐시값이 그대로 표시된다
        val settings = FakeAppSettingsRepository(notificationEnabled = false)
        val viewModel = createViewModel(settings = settings, member = pendingFetchMember())
        idle()

        assertFalse(viewModel.uiState.value.isNotificationEnabled)
    }

    @Test
    fun switchIsDisabledAndOffUnlessPermissionEnabled() {
        val viewModel = createViewModel(settings = FakeAppSettingsRepository(notificationEnabled = true))

        viewModel.onIntent(MyPageIntent.RefreshNotificationStatus(false))
        idle()
        assertFalse(viewModel.uiState.value.isNotificationSwitchEnabled)
        assertFalse(viewModel.uiState.value.isNotificationSwitchChecked)

        viewModel.onIntent(MyPageIntent.RefreshNotificationStatus(true))
        idle()
        assertTrue(viewModel.uiState.value.isNotificationSwitchEnabled)
        assertTrue(viewModel.uiState.value.isNotificationSwitchChecked)
    }

    @Test
    fun entryFetchesServerSettingIntoLocalCache() {
        val settings = FakeAppSettingsRepository(notificationEnabled = true)
        val member = ScriptedMemberRepository(getNotificationResults = listOf(NotificationSetting(false)))
        val viewModel = createViewModel(settings = settings, member = member)
        idle()

        assertEquals(listOf("getNotification"), member.events)
        assertEquals(listOf(false), settings.savedNotificationEnabled)
        assertFalse(viewModel.uiState.value.isNotificationEnabled)
        assertTrue(effects.isEmpty())
    }

    @Test
    fun entryFetchFailureKeepsLocalValueWithoutToast() {
        val settings = FakeAppSettingsRepository(notificationEnabled = false)
        val member = ScriptedMemberRepository(
            getNotificationResults = listOf(apiException(LinkTripErrorCode.NOT_FOUND_MEMBER, 404)),
        )
        val viewModel = createViewModel(settings = settings, member = member)
        idle()

        assertTrue(settings.savedNotificationEnabled.isEmpty())
        assertFalse(viewModel.uiState.value.isNotificationEnabled)
        assertTrue(effects.isEmpty())
    }

    @Test
    fun toggleUpdatesServerThenSavesLocally() {
        val settings = FakeAppSettingsRepository(notificationEnabled = true)
        val member = pendingFetchMember()
        val viewModel = createViewModel(settings = settings, member = member)
        viewModel.onIntent(MyPageIntent.RefreshNotificationStatus(true))
        idle()

        viewModel.onIntent(MyPageIntent.ToggleNotificationEnabled)
        idle()

        assertEquals(listOf(false), member.notificationCalls)
        assertEquals(listOf(false), settings.savedNotificationEnabled)
        assertFalse(viewModel.uiState.value.isNotificationEnabled)
        assertFalse(viewModel.uiState.value.isNotificationUpdating)
        assertTrue(effects.isEmpty())
    }

    @Test
    fun toggleFailureRevertsAndShowsErrorToast() {
        val settings = FakeAppSettingsRepository(notificationEnabled = true)
        val member = pendingFetchMember(
            notificationResults = listOf(apiException(LinkTripErrorCode.TOO_MANY_REQUESTS, 429)),
        )
        val viewModel = createViewModel(settings = settings, member = member)
        viewModel.onIntent(MyPageIntent.RefreshNotificationStatus(true))
        idle()

        viewModel.onIntent(MyPageIntent.ToggleNotificationEnabled)
        idle()

        assertEquals(listOf(false), member.notificationCalls)
        assertTrue(settings.savedNotificationEnabled.isEmpty())
        assertTrue(viewModel.uiState.value.isNotificationEnabled)
        assertFalse(viewModel.uiState.value.isNotificationUpdating)
        assertEquals(
            listOf(MyPageSideEffect.ShowToast(MyPageStrings.ToastNotificationFailure, MyPageToastType.ERROR)),
            effects,
        )
    }

    @Test
    fun toggleIsIgnoredWhileUpdating() {
        // 첫 반영이 응답 대기 중(Hang)일 때 재탭은 무시되고 낙관적 반영값이 유지된다
        val member = ScriptedMemberRepository(notificationResults = listOf(Hang))
        val viewModel = createViewModel(member = member)
        viewModel.onIntent(MyPageIntent.RefreshNotificationStatus(true))
        idle()

        viewModel.onIntent(MyPageIntent.ToggleNotificationEnabled)
        idle()
        assertTrue(viewModel.uiState.value.isNotificationUpdating)
        assertFalse(viewModel.uiState.value.isNotificationEnabled)

        viewModel.onIntent(MyPageIntent.ToggleNotificationEnabled)
        idle()

        assertEquals(listOf(false), member.notificationCalls)
        assertFalse(viewModel.uiState.value.isNotificationEnabled)
    }

    @Test
    fun toggleIsIgnoredWhenPermissionIsNotEnabled() {
        val member = ScriptedMemberRepository()
        val viewModel = createViewModel(member = member)

        viewModel.onIntent(MyPageIntent.ToggleNotificationEnabled)
        viewModel.onIntent(MyPageIntent.RefreshNotificationStatus(false))
        viewModel.onIntent(MyPageIntent.ToggleNotificationEnabled)
        idle()

        assertTrue(member.notificationCalls.isEmpty())
        assertTrue(viewModel.uiState.value.isNotificationEnabled)
        assertTrue(effects.isEmpty())
    }

    @Test
    fun toggleDuringEntryFetchDiscardsFetchResult() {
        // 서버 조회가 응답 전인 상태에서 토글하면 조회를 취소하고, 뒤늦게 도착한 값은 반영하지 않는다
        val settings = FakeAppSettingsRepository(notificationEnabled = true)
        val gate = CompletableDeferred<NotificationSetting>()
        val member = ScriptedMemberRepository(getNotificationGate = gate)
        val viewModel = createViewModel(settings = settings, member = member)
        viewModel.onIntent(MyPageIntent.RefreshNotificationStatus(true))
        idle()

        viewModel.onIntent(MyPageIntent.ToggleNotificationEnabled)
        idle()
        gate.complete(NotificationSetting(true))
        idle()

        assertEquals(listOf(false), member.notificationCalls)
        assertEquals(listOf(false), settings.savedNotificationEnabled)
        assertFalse(viewModel.uiState.value.isNotificationEnabled)
        assertTrue(effects.isEmpty())
    }

    @Test
    fun openNotificationSettingsEmitsNavigation() {
        val viewModel = createViewModel()

        viewModel.onIntent(MyPageIntent.OpenNotificationSettings)
        idle()

        assertEquals(listOf(MyPageSideEffect.NavigateToNotificationSettings), effects)
    }

    // ---- helpers ----

    private fun createViewModel(
        settings: FakeAppSettingsRepository = FakeAppSettingsRepository(),
        member: ScriptedMemberRepository = ScriptedMemberRepository(),
        feedback: ScriptedFeedbackRepository = ScriptedFeedbackRepository(),
    ): MyPageViewModel {
        val auth = AlwaysLoggedInAuthRepository()
        val ensureAuthenticated = EnsureAuthenticatedUseCase(auth)
        val viewModel = MyPageViewModel(
            appSettingsRepository = settings,
            sendFeedback = SendFeedbackUseCase(ensureAuthenticated, feedback, FixedAppInfoRepository()),
            resetApp = ResetAppUseCase(
                ensureAuthenticated = ensureAuthenticated,
                memberRepository = member,
                appSettingsRepository = settings,
                onboardingRepository = FakeOnboardingRepository(),
                tripPlanRepository = NoopTripPlanRepository(),
                authRepository = auth,
            ),
            updateNotificationSetting = UpdateNotificationSettingUseCase(ensureAuthenticated, member, settings),
            fetchNotificationSetting = FetchNotificationSettingUseCase(ensureAuthenticated, member, settings),
        )
        collectJob = CoroutineScope(Dispatchers.Main.immediate).launch {
            viewModel.sideEffect.collect { effects += it }
        }
        return viewModel
    }

    private fun idle() = shadowOf(Looper.getMainLooper()).idle()

    /** 진입 시 서버 조회가 응답 전(Hang)인 회원 저장소. 조회 결과가 로컬 캐시를 덮지 않는 상태를 만든다. */
    private fun pendingFetchMember(notificationResults: List<Any> = listOf(Unit)) =
        ScriptedMemberRepository(notificationResults = notificationResults, getNotificationResults = listOf(Hang))

    private fun apiException(code: LinkTripErrorCode, status: Int) =
        LinkTripApiException(errorCode = code, httpStatus = status, message = code.name)
}

/** 스크립트 결과에 넣으면 해당 호출이 취소될 때까지 대기한다. */
private object Hang

private class NoopTripPlanRepository : TripPlanRepository {
    override suspend fun getTripPlans(cursor: String?): CursorPage<TripPlanSummary> =
        CursorPage(items = emptyList(), nextCursor = null, hasNext = false)

    override suspend fun getTripPlan(tripPlanId: String): TripPlanDetail = error("Not used")

    override suspend fun updateTripPlan(
        tripPlanId: String,
        title: String?,
        items: List<TripPlanItemOrder>?,
    ): TripPlanDetail = error("Not used")

    override suspend fun deleteTripPlan(tripPlanId: String) = Unit

    override fun observeUncheckedTripPlanIds(): Flow<Set<String>> = MutableStateFlow(emptySet())

    override suspend fun markTripPlanUnchecked(tripPlanId: String) = Unit

    override suspend fun markTripPlanChecked(tripPlanId: String) = Unit

    override suspend fun clearUncheckedTripPlans() = Unit
}

private class AlwaysLoggedInAuthRepository : AuthRepository {
    override suspend fun login(): Auth = Auth("member-id", "access-token")
    override suspend fun isLoggedIn(): Boolean = true
    override suspend fun logout() = Unit
}

private class FixedAppInfoRepository : AppInfoRepository {
    override suspend fun getAppInfo() = AppInfo("1.0", "ANDROID", "15", "Pixel 8")
}

private class ScriptedMemberRepository(
    withdrawResults: List<Any> = listOf(0),
    notificationResults: List<Any> = listOf(Unit),
    getNotificationResults: List<Any> = listOf(NotificationSetting(true)),
    /** 지정하면 GET 응답을 이 Deferred가 완료될 때까지 보류한다. */
    private val getNotificationGate: CompletableDeferred<NotificationSetting>? = null,
) : MemberRepository {
    val events = mutableListOf<String>()
    val notificationCalls = mutableListOf<Boolean>()
    private val withdrawQueue = ArrayDeque(withdrawResults)
    private val notificationQueue = ArrayDeque(notificationResults)
    private val getNotificationQueue = ArrayDeque(getNotificationResults)

    override suspend fun getNotificationSetting(): NotificationSetting {
        events += "getNotification"
        if (getNotificationGate != null) return getNotificationGate.await()
        val result = getNotificationQueue.removeFirstOrNull() ?: NotificationSetting(true)
        if (result === Hang) awaitCancellation()
        if (result is Throwable) throw result
        return result as NotificationSetting
    }

    override suspend fun updateNotificationSetting(enabled: Boolean): NotificationSetting {
        events += "notification"
        notificationCalls += enabled
        val result = notificationQueue.removeFirstOrNull() ?: Unit
        if (result === Hang) awaitCancellation()
        if (result is Throwable) throw result
        return NotificationSetting(enabled)
    }

    override suspend fun withdraw(): Int {
        events += "withdraw"
        val result = withdrawQueue.removeFirstOrNull() ?: 0
        if (result === Hang) awaitCancellation()
        if (result is Throwable) throw result
        return result as Int
    }
}

private class ScriptedFeedbackRepository(
    results: List<Any> = listOf(Unit),
) : FeedbackRepository {
    data class Sent(val type: FeedbackType, val content: String)

    val sent = mutableListOf<Sent>()
    private val queue = ArrayDeque(results)

    override suspend fun sendFeedback(type: FeedbackType, content: String, appInfo: AppInfo) {
        sent += Sent(type, content)
        val result = queue.removeFirstOrNull() ?: Unit
        if (result === Hang) awaitCancellation()
        if (result is Throwable) throw result
    }
}

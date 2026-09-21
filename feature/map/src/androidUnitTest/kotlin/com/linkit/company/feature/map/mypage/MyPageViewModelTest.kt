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
import com.linkit.company.domain.usecase.ResetAppUseCase
import com.linkit.company.domain.usecase.SendFeedbackUseCase
import com.linkit.company.domain.usecase.SyncNotificationSettingUseCase
import com.linkit.company.feature.map.testing.FakeAppSettingsRepository
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

        assertEquals(listOf("withdraw"), member.events)
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
    fun notificationStatusSyncsToServerOnlyWhenValueChanges() {
        val member = ScriptedMemberRepository()
        val viewModel = createViewModel(member = member)

        viewModel.onIntent(MyPageIntent.RefreshNotificationStatus(false))
        viewModel.onIntent(MyPageIntent.RefreshNotificationStatus(false))
        viewModel.onIntent(MyPageIntent.RefreshNotificationStatus(true))
        viewModel.onIntent(MyPageIntent.RefreshNotificationStatus(null))
        idle()

        assertEquals(NotificationStatus.UNKNOWN, viewModel.uiState.value.notificationStatus)
        assertEquals(listOf(false, true), member.notificationCalls)
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
            resetApp = ResetAppUseCase(ensureAuthenticated, member, settings, auth),
            syncNotificationSetting = SyncNotificationSettingUseCase(ensureAuthenticated, member),
        )
        collectJob = CoroutineScope(Dispatchers.Main.immediate).launch {
            viewModel.sideEffect.collect { effects += it }
        }
        return viewModel
    }

    private fun idle() = shadowOf(Looper.getMainLooper()).idle()

    private fun apiException(code: LinkTripErrorCode, status: Int) =
        LinkTripApiException(errorCode = code, httpStatus = status, message = code.name)
}

/** 스크립트 결과에 넣으면 해당 호출이 취소될 때까지 대기한다. */
private object Hang

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
) : MemberRepository {
    override suspend fun registerFcmToken(fcmToken: String, platform: String) = error("Not used in this test")
    val events = mutableListOf<String>()
    val notificationCalls = mutableListOf<Boolean>()
    private val withdrawQueue = ArrayDeque(withdrawResults)

    override suspend fun updateNotificationSetting(enabled: Boolean): NotificationSetting {
        notificationCalls += enabled
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

package com.linkit.company.feature.map.mypage

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.captureRoboImage
import com.linkit.company.core.designsystem.theme.LinkItTheme
import com.linkit.company.domain.model.feedback.FeedbackType
import com.linkit.company.domain.model.settings.MapDisplayType
import org.jetbrains.compose.resources.PreviewContextConfigurationEffect
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Figma Pingo v3.0.3 마이페이지 섹션(17789-52610) 골든. 각 케이스 주석의 노드와 대조한다. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w375dp-h744dp-mdpi")
class MyPageScreenshotTest {
    @get:Rule
    val composeRule = createComposeRule()

    /** 18197:40381 — 기기 알림 꺼짐: 안내 카드 + 토글 disabled·off */
    @Test
    fun myPage_notificationDisabled() = captureScreen(
        MyPageUiState(notificationStatus = NotificationStatus.DISABLED),
    )

    /** 18197:41176 — 기기 알림 켜짐: 카드 없음 + 토글 on */
    @Test
    fun myPage_notificationEnabled() = captureScreen(
        MyPageUiState(notificationStatus = NotificationStatus.ENABLED),
    )

    /** 기기 알림 켜짐 + 앱 수신 설정 off: 카드 없음 + 토글 enabled·off (Figma 없음, 18197:41176의 off 변형) */
    @Test
    fun myPage_notificationReceiveOff() = captureScreen(
        MyPageUiState(notificationStatus = NotificationStatus.ENABLED, isNotificationEnabled = false),
    )

    /** 위성 선택 상태 */
    @Test
    fun myPage_satelliteSelected() = captureScreen(
        MyPageUiState(
            notificationStatus = NotificationStatus.ENABLED,
            mapDisplayType = MapDisplayType.SATELLITE,
        ),
    )

    /** 알림 상태 미확인: 카드 숨김 + 토글 off */
    @Test
    fun myPage_notificationUnknown() = captureScreen(MyPageUiState())

    /** 18212:35261 — 앱 초기화 확인 팝업 */
    @Test
    fun myPage_resetDialog() = captureScreen(
        MyPageUiState(
            notificationStatus = NotificationStatus.ENABLED,
            isResetDialogVisible = true,
        ),
    )

    /** 18197:41365 — 하단 토스트 */
    @Test
    fun myPage_toast() = captureScreen(
        MyPageUiState(notificationStatus = NotificationStatus.ENABLED),
        toast = MyPageToast(1, MyPageStrings.ToastFeedbackSuccess, MyPageToastType.SUCCESS),
    )

    /** 18197:38784 — 의견 보내기 시트 초기 상태(미선택, 빈 입력, 보내기 비활성) */
    @Test
    fun feedbackSheet_initial() = captureSheet(FeedbackSheetState())

    /** 시트 입력 상태(`제안` 선택, 내용 입력, 보내기 활성) */
    @Test
    fun feedbackSheet_filled() = captureSheet(
        FeedbackSheetState(
            selectedType = FeedbackType.SUGGESTION,
            content = "지도에서 일정 순서를 드래그로 바꿀 수 있으면 좋겠어요.",
        ),
    )

    private fun captureScreen(state: MyPageUiState, toast: MyPageToast? = null) {
        setContent {
            Box(Modifier.requiredSize(375.dp, 744.dp)) {
                MyPageContent(uiState = state, toast = toast, onIntent = {})
            }
        }
        composeRule.onRoot().captureRoboImage()
    }

    private fun captureSheet(state: FeedbackSheetState) {
        setContent {
            Box(
                Modifier
                    .requiredWidth(375.dp)
                    .background(LinkItTheme.color.semantic.material.dimmer),
            ) {
                FeedbackSheetContent(state = state, onIntent = {})
            }
        }
        composeRule.onRoot().captureRoboImage()
    }

    private fun setContent(content: @Composable () -> Unit) {
        composeRule.setContent {
            CompositionLocalProvider(LocalInspectionMode provides true) {
                PreviewContextConfigurationEffect()
                LinkItTheme { content() }
            }
        }
    }
}

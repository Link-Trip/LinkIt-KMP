package com.linkit.company.core.designsystem.screenshot

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.captureRoboImage
import com.linkit.company.core.designsystem.component.button.ButtonSize
import com.linkit.company.core.designsystem.component.button.LinkItButton
import com.linkit.company.core.designsystem.component.coachmark.CoachMarkDefaults
import com.linkit.company.core.designsystem.component.coachmark.CoachMarkPointer
import com.linkit.company.core.designsystem.component.coachmark.LinkItCoachMark
import com.linkit.company.core.designsystem.theme.LinkItTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(manifest = Config.NONE, sdk = [35], qualifiers = "w375dp-h400dp-mdpi")
class CoachMarkScreenshotTest {
    @get:Rule
    val composeRule = createComposeRule()

    /** 대상 위에 공간이 있으면 말풍선은 대상 위 */
    @Test
    fun coachMark_bubbleAbove() {
        setContent(targetTop = 300.dp, pointer = null, message = "일정 생성 버튼을 선택해보세요")
        composeRule.onNodeWithTag(CoachMarkDefaults.BubbleTestTag).assertIsDisplayed()
        composeRule.onRoot().captureRoboImage()
    }

    /** 대상이 위쪽에 붙어 있으면 말풍선은 대상 아래 + 커서 */
    @Test
    fun coachMark_bubbleBelowWithPointer() {
        setContent(targetTop = 24.dp, pointer = CoachMarkPointer.Leading, message = "링크를 복사해요")
        composeRule.onNodeWithText("링크를 복사해요").assertIsDisplayed()
        composeRule.onRoot().captureRoboImage()
    }

    /** 대상 없이 스크림만 */
    @Test
    fun coachMark_scrimOnly() {
        composeRule.setContent {
            LinkItTheme {
                Box(Modifier.requiredSize(375.dp, 400.dp).background(LinkItTheme.color.semantic.background.normal.normal)) {
                    LinkItCoachMark(targetBounds = null, message = "안내", onTargetClick = {})
                }
            }
        }
        composeRule.onRoot().captureRoboImage()
    }

    @Test
    fun coachMark_targetTapDelegatesAndOutsideTapIsConsumed() {
        var targetClicks = 0
        var underlyingClicks = 0
        setContent(
            targetTop = 300.dp,
            pointer = null,
            message = "일정 생성 버튼을 선택해보세요",
            onTargetClick = { targetClicks++ },
            onUnderlyingClick = { underlyingClicks++ },
        )

        // 대상 밖(상단) 탭은 소비되어 아무 콜백도 없다
        composeRule.onNodeWithTag(CoachMarkDefaults.OverlayTestTag).performTouchInput {
            click(position = Offset(centerX, 40f))
        }
        // 대상 영역(200,300 + 120x40 dp, mdpi = px) 탭은 onTargetClick 으로 위임된다
        composeRule.onNodeWithTag(CoachMarkDefaults.OverlayTestTag).performTouchInput {
            click(position = Offset(260f, 320f))
        }

        composeRule.runOnIdle {
            assertEquals(1, targetClicks)
            assertEquals(0, underlyingClicks)
        }
    }

    private fun setContent(
        targetTop: Dp,
        pointer: CoachMarkPointer?,
        message: String,
        onTargetClick: () -> Unit = {},
        onUnderlyingClick: () -> Unit = {},
    ) {
        composeRule.setContent {
            LinkItTheme {
                Host(
                    targetTop = targetTop,
                    pointer = pointer,
                    message = message,
                    onTargetClick = onTargetClick,
                    onUnderlyingClick = onUnderlyingClick,
                )
            }
        }
    }

    @Composable
    private fun Host(
        targetTop: Dp,
        pointer: CoachMarkPointer?,
        message: String,
        onTargetClick: () -> Unit,
        onUnderlyingClick: () -> Unit,
    ) {
        val density = LocalDensity.current
        val targetLeft = 200.dp
        val bounds = Rect(
            offset = Offset(with(density) { targetLeft.toPx() }, with(density) { targetTop.toPx() }),
            size = Size(with(density) { 120.dp.toPx() }, with(density) { 40.dp.toPx() }),
        )
        Box(
            Modifier
                .requiredSize(375.dp, 400.dp)
                .background(LinkItTheme.color.semantic.background.normal.normal),
        ) {
            Box(
                Modifier
                    .offset(x = targetLeft, y = targetTop)
                    .size(120.dp, 40.dp),
            ) {
                LinkItButton(
                    onClick = onUnderlyingClick,
                    text = "일정 생성",
                    size = ButtonSize.Medium,
                    modifier = Modifier.fillMaxSize(),
                )
            }
            LinkItCoachMark(
                targetBounds = bounds,
                message = message,
                pointer = pointer,
                onTargetClick = onTargetClick,
            )
        }
    }
}

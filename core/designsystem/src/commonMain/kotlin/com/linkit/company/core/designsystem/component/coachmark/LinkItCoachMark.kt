package com.linkit.company.core.designsystem.component.coachmark

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.linkit.company.core.designsystem.foundation.icon.addPath
import com.linkit.company.core.designsystem.foundation.icon.linkItIcon
import com.linkit.company.core.designsystem.foundation.typography.rememberNanumSquareFontFamily
import com.linkit.company.core.designsystem.theme.LinkItTheme
import kotlin.math.roundToInt

/**
 * 말풍선 옆에 붙는 커서(포인터) 아이콘의 위치. 대상 컴포저블 기준이 아니라 말풍선 기준이다.
 *
 * - [Leading]: 말풍선 왼쪽에 커서를 둔다. (Figma 튜토리얼 3·4단계)
 * - [Trailing]: 말풍선 오른쪽에 커서를 둔다.
 */
enum class CoachMarkPointer {
    Leading,
    Trailing,
}

/**
 * 체험형 튜토리얼용 코치마크 오버레이.
 *
 * Figma "Pingo v3.0.3 - 인트로 / 최종진입"의 딤 + 말풍선(`intro Information`) 연출을 구현한다.
 * 전체 화면을 반투명 스크림으로 덮고 [targetBounds] 영역만 컷아웃해 대상을 밝게 남긴다.
 * 말풍선은 대상 위에 공간이 있으면 위, 없으면 아래에 자동 배치한다.
 *
 * 터치 규칙(스펙 FR-012·SC-005):
 * - 대상 영역 안 터치 → [onTargetClick] 으로 위임한다(대상 컴포저블은 다시 그리지 않는다).
 * - 그 밖의 터치는 모두 소비한다.
 * - 시스템 뒤로가기는 무시한다(Android `BackHandler`, iOS no-op).
 *
 * `건너뛰기`처럼 항상 눌려야 하는 요소는 이 오버레이 **위** 레이어에 호출부가 따로 그린다.
 *
 * @param targetBounds 대상 영역(루트 좌표계, `boundsInRoot`). `null`이면 스크림만 그린다.
 * @param message 말풍선 문구.
 * @param pointer 커서 아이콘 위치. `null`이면 표시하지 않는다.
 * @param onTargetClick 대상 영역 터치 콜백.
 */
@Composable
fun LinkItCoachMark(
    targetBounds: Rect?,
    message: String,
    onTargetClick: () -> Unit,
    modifier: Modifier = Modifier,
    pointer: CoachMarkPointer? = null,
    scrimColor: Color = CoachMarkDefaults.scrimColor,
    targetCornerRadius: Dp = CoachMarkDefaults.TargetCornerRadius,
) {
    CoachMarkBackHandler()

    // 오버레이 자신의 루트 오프셋을 빼서, 호출부가 어디에 두어도 루트 좌표계 bounds 를 그대로 쓸 수 있게 한다.
    var overlayOrigin by remember { mutableStateOf(Offset.Zero) }
    val localTarget = targetBounds?.translate(-overlayOrigin)
    val latestOnTargetClick by rememberUpdatedState(onTargetClick)
    val latestTarget by rememberUpdatedState(localTarget)

    Box(
        modifier = modifier
            .fillMaxSize()
            .onGloballyPositioned { overlayOrigin = it.positionInRoot() }
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent(PointerEventPass.Initial)
                        val target = latestTarget
                        val change = event.changes.firstOrNull() ?: continue
                        val insideTarget = target != null && target.contains(change.position)
                        if (!insideTarget) {
                            event.changes.forEach { it.consume() }
                        } else if (event.type == PointerEventType.Release) {
                            event.changes.forEach { it.consume() }
                            latestOnTargetClick()
                        }
                    }
                }
            }
            .testTag(CoachMarkDefaults.OverlayTestTag)
            .semantics { contentDescription = message },
    ) {
        CoachMarkScrim(
            target = localTarget,
            scrimColor = scrimColor,
            cornerRadius = targetCornerRadius,
        )
        if (localTarget != null) {
            CoachMarkBubbleLayout(target = localTarget) {
                CoachMarkBubble(message = message, pointer = pointer)
            }
        }
    }
}

@Composable
private fun CoachMarkScrim(
    target: Rect?,
    scrimColor: Color,
    cornerRadius: Dp,
) {
    val radiusPx = with(LocalDensity.current) { cornerRadius.toPx() }
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen },
    ) {
        drawRect(scrimColor)
        if (target != null) {
            drawRoundRect(
                color = Color.Black,
                topLeft = Offset(target.left, target.top),
                size = Size(target.width, target.height),
                cornerRadius = CornerRadius(radiusPx, radiusPx),
                blendMode = BlendMode.Clear,
            )
        }
    }
}

/**
 * 말풍선을 대상 위(기본) 또는 아래에 두고, 가로는 대상 중앙에 맞추되 화면 안으로 클램프한다.
 */
@Composable
private fun CoachMarkBubbleLayout(
    target: Rect,
    content: @Composable () -> Unit,
) {
    val gapPx = with(LocalDensity.current) { CoachMarkDefaults.BubbleGap.toPx() }
    val edgePx = with(LocalDensity.current) { CoachMarkDefaults.EdgeMargin.toPx() }
    Layout(
        content = content,
        modifier = Modifier.fillMaxSize(),
    ) { measurables, constraints ->
        val placeable = measurables.firstOrNull()?.measure(
            constraints.copy(minWidth = 0, minHeight = 0),
        )
        layout(constraints.maxWidth, constraints.maxHeight) {
            placeable ?: return@layout
            val fitsAbove = target.top - gapPx - placeable.height >= edgePx
            val y = if (fitsAbove) {
                target.top - gapPx - placeable.height
            } else {
                target.bottom + gapPx
            }
            val centeredX = target.center.x - placeable.width / 2f
            val maxX = (constraints.maxWidth - placeable.width - edgePx).coerceAtLeast(edgePx)
            val x = centeredX.coerceIn(edgePx, maxX)
            placeable.place(IntOffset(x.roundToInt(), y.roundToInt()))
        }
    }
}

@Composable
private fun CoachMarkBubble(
    message: String,
    pointer: CoachMarkPointer?,
) {
    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier.testTag(CoachMarkDefaults.BubbleTestTag),
    ) {
        if (pointer == CoachMarkPointer.Leading) {
            CoachMarkPointerIcon()
        }
        Text(
            text = message,
            style = CoachMarkDefaults.bubbleTextStyle(),
            color = LinkItTheme.color.semantic.label.strong,
            maxLines = 1,
            softWrap = false,
            modifier = Modifier
                .clip(LinkItTheme.shape.rounded)
                .background(LinkItTheme.color.semantic.background.normal.normal)
                .padding(CoachMarkDefaults.BubblePadding),
        )
        if (pointer == CoachMarkPointer.Trailing) {
            CoachMarkPointerIcon()
        }
    }
}

@Composable
private fun CoachMarkPointerIcon() {
    val painter = rememberVectorPainter(CoachMarkDefaults.PointerIcon)
    androidx.compose.foundation.Image(
        painter = painter,
        contentDescription = null,
        modifier = Modifier.size(CoachMarkDefaults.PointerSize),
    )
}

object CoachMarkDefaults {
    const val OverlayTestTag = "coach-mark-overlay"
    const val BubbleTestTag = "coach-mark-bubble"

    /** 대상 컷아웃의 모서리 반경. 대상이 pill 버튼이어도 자연스럽게 보이도록 넉넉히 둔다. */
    val TargetCornerRadius: Dp = 12.dp

    /** 대상과 말풍선 사이 간격(Figma 8). */
    val BubbleGap: Dp = 8.dp

    /** 말풍선이 화면 가장자리에서 떨어지는 최소 여백. */
    val EdgeMargin: Dp = 16.dp

    /** 말풍선 내부 패딩(Figma `intro Information`: 20/12). */
    val BubblePadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp, vertical = 12.dp)

    /** 말풍선 문구(Figma `intro Information`: NanumSquare Neo Regular 14, 행간 1.45, 그림자 없음). */
    @Composable
    fun bubbleTextStyle(): TextStyle = LinkItTheme.typography.label1NormalRegular.copy(
        fontFamily = rememberNanumSquareFontFamily(),
        fontSize = BubbleFontSize,
        lineHeight = BubbleLineHeight,
        letterSpacing = 0.sp,
    )

    private val BubbleFontSize = 14.sp
    private val BubbleLineHeight = 20.3.sp

    /** 커서 아이콘 크기(Figma `Arrow` 32). */
    val PointerSize: Dp = 32.dp

    val scrimColor: Color
        @Composable
        get() = LinkItTheme.color.semantic.material.dimmer

    /** macOS 스타일 커서 화살표(Figma `Arrow`). 흰 외곽선 + 검은 본체라 tint 없이 그린다. */
    val PointerIcon: ImageVector by lazy {
        linkItIcon(
            name = "CoachMarkPointer",
            widthDp = 32f,
            heightDp = 32f,
            viewportWidth = 32f,
            viewportHeight = 32f,
        ) {
            addPath(
                pathData = "M24.957 18.6667L9.33333 3.51647V25.4488L13.6777 20.6701L16.0811 25.477C16.4434 26.2015 " +
                    "17.3244 26.4952 18.0489 26.1329L19.8104 25.2522C20.5349 24.8899 20.8285 24.009 20.4663 " +
                    "23.2845L18.1574 18.6667H24.957ZM19.2737 23.8807C19.3066 23.9466 19.2799 24.0267 19.2141 " +
                    "24.0596L17.4526 24.9404C17.3867 24.9733 17.3066 24.9466 17.2737 24.8807L14 18.3333L10.6667 " +
                    "22V6.66667L21.6667 17.3333H16L19.2737 23.8807Z",
                pathFillType = PathFillType.EvenOdd,
                fill = Color.White,
            )
            addPath(
                pathData = "M21.6667 17.3333L10.6667 6.66667V22L14 18.3333L17.2737 24.8807C17.3066 24.9466 17.3867 " +
                    "24.9733 17.4526 24.9404L19.2141 24.0596C19.2799 24.0267 19.3066 23.9466 19.2737 23.8807L16 " +
                    "17.3333H21.6667Z",
                fill = Color.Black,
            )
        }
    }
}

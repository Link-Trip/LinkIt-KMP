package com.linkit.company.core.designsystem.component.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.linkit.company.core.designsystem.component.button.ButtonSize
import com.linkit.company.core.designsystem.component.button.LinkItButton
import com.linkit.company.core.designsystem.component.coachmark.CoachMarkPointer
import com.linkit.company.core.designsystem.component.coachmark.LinkItCoachMark
import com.linkit.company.core.designsystem.theme.LinkItTheme

@Preview(showBackground = true, widthDp = 375, heightDp = 400)
@Composable
private fun LinkItCoachMarkAbovePreview() {
    LinkItTheme {
        CoachMarkPreviewHost(targetTop = 300.dp, pointer = null, message = "일정 생성 버튼을 선택해보세요")
    }
}

@Preview(showBackground = true, widthDp = 375, heightDp = 400)
@Composable
private fun LinkItCoachMarkBelowWithPointerPreview() {
    LinkItTheme {
        CoachMarkPreviewHost(targetTop = 24.dp, pointer = CoachMarkPointer.Leading, message = "링크를 복사해요")
    }
}

@Composable
private fun CoachMarkPreviewHost(
    targetTop: androidx.compose.ui.unit.Dp,
    pointer: CoachMarkPointer?,
    message: String,
) {
    val density = LocalDensity.current
    val targetLeft = 200.dp
    val targetSize = Size(with(density) { 120.dp.toPx() }, with(density) { 40.dp.toPx() })
    val bounds = Rect(
        offset = Offset(with(density) { targetLeft.toPx() }, with(density) { targetTop.toPx() }),
        size = targetSize,
    )
    Box(
        Modifier
            .fillMaxSize()
            .background(LinkItTheme.color.semantic.background.normal.normal),
    ) {
        Box(
            Modifier
                .offset(x = targetLeft, y = targetTop)
                .size(120.dp, 40.dp),
        ) {
            LinkItButton(onClick = {}, text = "일정 생성", size = ButtonSize.Medium)
        }
        LinkItCoachMark(
            targetBounds = bounds,
            message = message,
            pointer = pointer,
            onTargetClick = {},
        )
    }
}

package com.linkit.company.core.designsystem.component.sheet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.linkit.company.core.designsystem.foundation.color.token.PaletteTokens
import com.linkit.company.core.designsystem.foundation.shape.token.ShapeTokens
import com.linkit.company.core.designsystem.theme.LinkItTheme

/**
 * Material3 `ModalBottomSheet` 를 디자인 토큰으로 감싼 모달 바텀시트.
 *
 * Figma "Pingo v3.0.3 - 약관 동의 바텀시트" 의 컨테이너(상단 20dp 라운드, 흰 배경, 드래그 핸들)를 따른다.
 * 끌어 내리기·바깥 탭은 Material3 가 처리해 [onDismissRequest] 를 호출한다.
 *
 * @param onDismissRequest 시트를 닫아야 할 때(끌어 내림·스크림 탭·뒤로가기) 콜백.
 * @param sheetState 시트 상태. 기본은 부분 확장을 건너뛰어 항상 콘텐츠 높이만큼 연다.
 * @param content 시트 본문. [LinkItModalBottomSheetDefaults.Handle] 아래에 세로로 배치된다.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LinkItModalBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    shape: Shape = LinkItModalBottomSheetDefaults.shape,
    containerColor: Color = LinkItModalBottomSheetDefaults.containerColor,
    scrimColor: Color = LinkItModalBottomSheetDefaults.scrimColor,
    dragHandle: @Composable (() -> Unit)? = { LinkItModalBottomSheetDefaults.Handle() },
    content: @Composable ColumnScope.() -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        sheetState = sheetState,
        shape = shape,
        containerColor = containerColor,
        contentColor = LinkItTheme.color.semantic.label.normal,
        scrimColor = scrimColor,
        dragHandle = dragHandle,
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) },
        content = content,
    )
}

object LinkItModalBottomSheetDefaults {

    /** 시트 상단 라운드(Figma 20dp). */
    val CornerRadius: Dp = 20.dp

    /** 드래그 핸들 크기(48 x 3). */
    val HandleWidth: Dp = 48.dp
    val HandleHeight: Dp = 3.dp

    val shape: Shape
        get() = androidx.compose.foundation.shape.RoundedCornerShape(
            topStart = CornerRadius,
            topEnd = CornerRadius,
        )

    val containerColor: Color
        @Composable
        get() = LinkItTheme.color.semantic.background.normal.normal

    val scrimColor: Color
        @Composable
        get() = LinkItTheme.color.semantic.material.dimmer

    /**
     * 시트 상단 드래그 핸들(Figma `Handle`: 위 8 / 아래 4 패딩, 48x3 pill, neutral 300 20%).
     * 화면 골든이 [LinkItModalBottomSheet] 대신 본문을 직접 호출할 때도 같은 모양을 재현할 수 있도록 공개한다.
     */
    @Composable
    fun Handle(modifier: Modifier = Modifier) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 4.dp),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .width(HandleWidth)
                    .height(HandleHeight)
                    .clip(ShapeTokens.CornerRounded)
                    .background(PaletteTokens.PingoNeutral300.copy(alpha = HandleAlpha)),
            )
        }
    }

    private const val HandleAlpha = 0.2f
}

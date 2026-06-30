package com.linkit.company.core.designsystem.component.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp

/**
 * 선택 가능한 항목을 세로로 나열하는 메뉴(드롭다운) 카드.
 *
 * Figma "Pingo v3.0.3 - Presentation / Menu" 를 구현한 것으로, [content] 에
 * [LinkItMenuItem] 을 배치해 사용한다. 스크롤은 지원하지 않으므로 항목 수에 맞춰 높이가 늘어난다.
 *
 * @param shape 카드 모서리 모양. 기본은 [MenuDefaults.ContainerShape].
 * @param containerColor 카드 배경 색.
 * @param borderColor 카드 외곽선 색.
 * @param borderWidth 카드 외곽선 두께.
 * @param shadowElevation 카드 그림자 높이.
 * @param contentPadding 카드 안쪽 여백.
 * @param content 메뉴 항목. 보통 [LinkItMenuItem] 을 나열한다.
 */
@Composable
fun LinkItMenu(
    modifier: Modifier = Modifier,
    shape: Shape = MenuDefaults.ContainerShape,
    containerColor: Color = MenuDefaults.containerColor,
    borderColor: Color = MenuDefaults.borderColor,
    borderWidth: Dp = MenuDefaults.BorderWidth,
    shadowElevation: Dp = MenuDefaults.ShadowElevation,
    contentPadding: PaddingValues = MenuDefaults.ContainerPadding,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .widthIn(min = MenuDefaults.MinWidth)
            .shadow(shadowElevation, shape)
            .clip(shape)
            .background(containerColor)
            .border(borderWidth, borderColor, shape)
            .padding(contentPadding),
        verticalArrangement = Arrangement.spacedBy(MenuDefaults.ItemSpacing),
        content = content,
    )
}

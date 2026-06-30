package com.linkit.company.core.designsystem.component.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.linkit.company.core.designsystem.foundation.interaction.InteractionDefaults

/**
 * 콘텐츠를 목적별로 구분해 전환하는 텍스트 탭 묶음.
 *
 * Figma "Pingo v3.0.3 - Navigation / Tab" 을 구현한 것으로, 하단에 전체 너비 구분선을 그리고
 * 선택된 [LinkItTab] 은 그 위에 언더라인 인디케이터를 표시한다.
 *
 * @param size 탭 높이 규격. 기본값은 [TabSize.Medium] (48dp).
 * @param horizontalPadding `true` 면 탭 묶음 좌우에 [TabDefaults.HorizontalPadding] 여백을 둔다.
 * @param content [RowScope] 에서 [LinkItTab] 들을 가로로 균등 배치한다.
 */
@Composable
fun LinkItTabRow(
    modifier: Modifier = Modifier,
    size: TabSize = TabSize.Medium,
    horizontalPadding: Boolean = false,
    containerColor: Color = TabDefaults.containerColor,
    dividerColor: Color = TabDefaults.dividerColor,
    content: @Composable RowScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(containerColor)
            .then(
                if (horizontalPadding) {
                    Modifier.padding(horizontal = TabDefaults.HorizontalPadding)
                } else {
                    Modifier
                },
            )
            .height(TabDefaults.height(size)),
    ) {
        HorizontalDivider(
            modifier = Modifier.align(Alignment.BottomCenter),
            color = dividerColor,
        )
        Row(
            modifier = Modifier.fillMaxWidth().fillMaxHeight(),
            verticalAlignment = Alignment.CenterVertically,
            content = content,
        )
    }
}

/**
 * [LinkItTabRow] 안에 배치하는 탭 하나.
 *
 * @param selected 선택 여부. 선택 시 [selectedColor] 텍스트와 하단 언더라인 인디케이터를 표시한다.
 */
@Composable
fun RowScope.LinkItTab(
    selected: Boolean,
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    selectedColor: Color = TabDefaults.selectedColor,
    unselectedColor: Color = TabDefaults.unselectedColor,
    disabledColor: Color = TabDefaults.disabledColor,
    indicatorColor: Color = TabDefaults.selectedColor,
) {
    val textColor = when {
        !enabled -> disabledColor
        selected -> selectedColor
        else -> unselectedColor
    }
    Box(
        modifier = modifier
            .weight(1f)
            .widthIn(min = TabDefaults.MinTabWidth)
            .fillMaxHeight()
            .selectable(
                selected = selected,
                interactionSource = null,
                indication = InteractionDefaults.indication(),
                onClick = onClick,
                enabled = enabled,
                role = Role.Tab,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            color = textColor,
            style = TabDefaults.textStyle,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (selected && enabled) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(TabDefaults.IndicatorThickness)
                    .background(indicatorColor),
            )
        }
    }
}

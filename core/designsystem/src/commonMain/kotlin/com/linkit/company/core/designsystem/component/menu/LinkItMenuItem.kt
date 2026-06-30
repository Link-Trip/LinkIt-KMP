package com.linkit.company.core.designsystem.component.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import com.linkit.company.core.designsystem.foundation.icon.LinkItIcon
import com.linkit.company.core.designsystem.foundation.interaction.InteractionDefaults

/**
 * [LinkItMenu] 안에 놓이는 한 줄짜리 메뉴 항목(셀).
 *
 * @param text 항목에 표시할 문구.
 * @param onClick 클릭(토글) 콜백.
 * @param variant 시각적 스타일. ([MenuItemVariant.Normal] 기본)
 * @param selected 선택 여부. [MenuItemVariant.Normal] 은 강조 색, [MenuItemVariant.Checkbox] 는 체크 상태로 표현한다.
 * @param enabled `false` 면 비활성 색을 쓰고 클릭을 막는다.
 * @param padding 상하 여백 규격. ([MenuItemPadding.Regular] 기본)
 * @param colors 색상 묶음. 기본은 [MenuDefaults.itemColors].
 * @param textStyle 텍스트 타이포. 기본은 [MenuDefaults.textStyle].
 */
@Composable
fun LinkItMenuItem(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: MenuItemVariant = MenuItemVariant.Normal,
    selected: Boolean = false,
    enabled: Boolean = true,
    padding: MenuItemPadding = MenuItemPadding.Regular,
    colors: MenuItemColors = MenuDefaults.itemColors(),
    textStyle: TextStyle = MenuDefaults.textStyle,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(MenuDefaults.ItemShape)
            .selectable(
                selected = selected,
                interactionSource = null,
                indication = InteractionDefaults.indication(),
                enabled = enabled,
                role = if (variant == MenuItemVariant.Checkbox) Role.Checkbox else Role.Button,
                onClick = onClick,
            )
            .alpha(if (enabled) 1f else MenuDefaults.DisabledAlpha)
            .padding(
                horizontal = MenuDefaults.ItemHorizontalPadding,
                vertical = MenuDefaults.itemVerticalPadding(padding),
            ),
        horizontalArrangement = Arrangement.spacedBy(MenuDefaults.ItemContentSpacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (variant == MenuItemVariant.Checkbox) {
            MenuItemCheckbox(checked = selected, colors = colors)
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .heightIn(min = MenuDefaults.ItemMinContentHeight),
            contentAlignment = Alignment.CenterStart,
        ) {
            Text(
                text = text,
                style = textStyle,
                color = colors.textColor(variant, selected, enabled),
            )
        }
    }
}

@Composable
private fun MenuItemCheckbox(
    checked: Boolean,
    colors: MenuItemColors,
) {
    Box(
        modifier = Modifier
            .size(MenuDefaults.CheckboxSize)
            .clip(MenuDefaults.CheckboxShape)
            .then(
                if (checked) {
                    Modifier.background(colors.checkboxCheckedColor)
                } else {
                    Modifier.border(
                        MenuDefaults.CheckboxBorderWidth,
                        colors.checkboxBorderColor,
                        MenuDefaults.CheckboxShape,
                    )
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (checked) {
            Icon(
                imageVector = LinkItIcon.Utility.Check,
                contentDescription = null,
                modifier = Modifier.size(MenuDefaults.CheckIconSize),
                tint = colors.checkboxCheckColor,
            )
        }
    }
}

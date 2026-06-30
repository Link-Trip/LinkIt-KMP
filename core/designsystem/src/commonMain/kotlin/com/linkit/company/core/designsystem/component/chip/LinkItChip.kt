package com.linkit.company.core.designsystem.component.chip

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import com.linkit.company.core.designsystem.foundation.interaction.InteractionDefaults

/**
 * 항목을 제어하거나 상태를 표시할 때 쓰는 칩. 낮은 시각 위계를 가진다.
 *
 * Figma "Pingo v3.0.3 - Action / Action Chip" 을 구현한 것으로, [active] 로 선택 상태를,
 * [variant]·[size] 로 시각 스타일을 표현한다.
 *
 * @param onClick 클릭(토글) 콜백.
 * @param text 칩 라벨.
 * @param active 선택(활성) 여부. `true` 면 강조 색으로 표시한다.
 * @param variant 면 채움([ChipVariant.Solid]) / 외곽선([ChipVariant.Outlined]).
 * @param size 크기 규격. 텍스트 스타일·패딩·라운드가 함께 바뀐다.
 * @param enabled `false` 면 비활성 색을 쓰고 클릭을 막는다.
 * @param leadingIcon 텍스트 앞 아이콘.
 * @param trailingIcon 텍스트 뒤 아이콘.
 */
@Composable
fun LinkItChip(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    active: Boolean = false,
    variant: ChipVariant = ChipVariant.Solid,
    size: ChipSize = ChipSize.Medium,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    colors: ChipColors = ChipDefaults.colors(variant, active),
    shape: Shape = ChipDefaults.shape(size),
    contentPadding: PaddingValues = ChipDefaults.contentPadding(size),
    textStyle: TextStyle = ChipDefaults.textStyle(size),
) {
    val contentColor = colors.contentColor(enabled)
    val borderColor = colors.borderColor(enabled)
    Row(
        modifier = modifier
            .clip(shape)
            .background(colors.containerColor(enabled))
            .then(if (borderColor.isSpecified) Modifier.border(ChipDefaults.BorderWidth, borderColor, shape) else Modifier)
            .selectable(
                selected = active,
                interactionSource = null,
                indication = InteractionDefaults.indication(),
                enabled = enabled,
                role = Role.Button,
                onClick = onClick,
            )
            .padding(contentPadding),
        horizontalArrangement = Arrangement.spacedBy(ChipDefaults.contentSpacing(size), Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leadingIcon != null) {
            ChipIcon(leadingIcon, contentColor)
        }
        Text(
            text = text,
            style = textStyle,
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (trailingIcon != null) {
            ChipIcon(trailingIcon, contentColor)
        }
    }
}

@Composable
private fun ChipIcon(icon: ImageVector, tint: Color) {
    Icon(
        imageVector = icon,
        contentDescription = null,
        modifier = Modifier.size(ChipDefaults.IconSize),
        tint = tint,
    )
}

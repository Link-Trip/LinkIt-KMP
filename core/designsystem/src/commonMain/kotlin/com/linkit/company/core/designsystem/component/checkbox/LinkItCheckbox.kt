package com.linkit.company.core.designsystem.component.checkbox

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.linkit.company.core.designsystem.foundation.icon.LinkItIcon
import com.linkit.company.core.designsystem.foundation.interaction.InteractionDefaults
import com.linkit.company.core.designsystem.theme.LinkItTheme

/**
 * 원형 체크박스. 약관 동의 항목처럼 "선택/해제" 를 표시·제어할 때 쓴다.
 *
 * Figma "Pingo v3.0.3 - 약관 동의 바텀시트" 의 항목 아이콘을 구현한 것으로, 체크 시
 * `Utility.CircleCheckFill`(primary), 해제 시 `Utility.Circle`(assistive) 아이콘으로 토글된다.
 *
 * @param checked 체크 여부
 * @param onCheckedChange 탭 시 토글 콜백. `null` 이면 읽기 전용 표시(상위 행이 탭을 처리)
 * @param enabled 활성 여부. `false` 면 비활성 색을 쓰고 입력을 받지 않는다
 */
@Composable
fun LinkItCheckbox(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    size: Dp = CheckboxDefaults.Size,
    colors: CheckboxColors = CheckboxDefaults.colors(),
) {
    val interaction = if (onCheckedChange != null) {
        Modifier.toggleable(
            value = checked,
            enabled = enabled,
            role = Role.Checkbox,
            interactionSource = null,
            indication = InteractionDefaults.indication(),
            onValueChange = onCheckedChange,
        )
    } else {
        Modifier
    }
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .then(interaction),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = if (checked) LinkItIcon.Utility.CircleCheckFill else LinkItIcon.Utility.Circle,
            contentDescription = null,
            tint = colors.iconColor(checked, enabled),
            modifier = Modifier.size(size),
        )
    }
}

@Immutable
data class CheckboxColors(
    val checkedColor: Color,
    val uncheckedColor: Color,
    val disabledColor: Color,
) {
    fun iconColor(checked: Boolean, enabled: Boolean): Color = when {
        !enabled -> disabledColor
        checked -> checkedColor
        else -> uncheckedColor
    }
}

object CheckboxDefaults {

    /** 아이콘 크기(Figma Icon 20). */
    val Size: Dp = 20.dp

    @Composable
    @ReadOnlyComposable
    fun colors(
        checkedColor: Color = LinkItTheme.color.semantic.primary.normal,
        uncheckedColor: Color = LinkItTheme.color.semantic.label.assistive,
        disabledColor: Color = LinkItTheme.color.semantic.label.disable,
    ): CheckboxColors = CheckboxColors(
        checkedColor = checkedColor,
        uncheckedColor = uncheckedColor,
        disabledColor = disabledColor,
    )
}

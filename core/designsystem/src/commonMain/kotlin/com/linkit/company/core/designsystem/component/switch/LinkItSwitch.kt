package com.linkit.company.core.designsystem.component.switch

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.toggleableState
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.dp
import com.linkit.company.core.designsystem.foundation.interaction.InteractionDefaults

/**
 * 활성화 여부를 표시·제어하는 스위치(토글).
 *
 * Figma "Pingo v3.0.3 - Switch / Switch" 를 구현한다. [onCheckedChange] 가 null 이면 사용자 입력을 받지
 * 않는 읽기 전용 표시로 동작하며, 이때는 상위 요소(예: 설정 행 전체)가 탭을 처리한다.
 *
 * @param checked 켜짐 여부
 * @param onCheckedChange 탭 시 토글 콜백. null 이면 읽기 전용
 * @param enabled 활성 여부. false 면 비활성 색을 사용하고 입력을 받지 않는다
 */
@Composable
fun LinkItSwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: SwitchColors = SwitchDefaults.colors(),
) {
    val trackColor by animateColorAsState(colors.trackColor(checked, enabled), label = "switchTrack")
    val thumbOffset by animateDpAsState(
        targetValue = if (checked) {
            SwitchDefaults.TrackWidth - SwitchDefaults.TrackPadding * 2 - SwitchDefaults.ThumbSize
        } else {
            0.dp
        },
        label = "switchThumb",
    )

    val interaction = if (onCheckedChange != null) {
        Modifier.toggleable(
            value = checked,
            enabled = enabled,
            role = Role.Switch,
            interactionSource = null,
            indication = InteractionDefaults.indication(),
            onValueChange = onCheckedChange,
        )
    } else {
        Modifier.semantics {
            role = Role.Switch
            toggleableState = if (checked) ToggleableState.On else ToggleableState.Off
        }
    }

    Box(
        modifier = modifier
            .size(width = SwitchDefaults.TrackWidth, height = SwitchDefaults.TrackHeight)
            .clip(CircleShape)
            .background(trackColor)
            .then(interaction)
            .padding(SwitchDefaults.TrackPadding),
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(
            modifier = Modifier
                .offset(x = thumbOffset)
                .size(SwitchDefaults.ThumbSize)
                .clip(CircleShape)
                .background(colors.thumbColor(enabled)),
        )
    }
}

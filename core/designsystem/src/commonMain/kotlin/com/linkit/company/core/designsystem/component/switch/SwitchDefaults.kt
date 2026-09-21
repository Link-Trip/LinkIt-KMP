package com.linkit.company.core.designsystem.component.switch

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.linkit.company.core.designsystem.theme.LinkItTheme

/**
 * [LinkItSwitch] 의 치수와 색 규격.
 *
 * Figma "Pingo v3.0.3 - Switch / Switch" 인스턴스(43.33 × 26.67, 썸 20)를 그대로 따른다.
 */
object SwitchDefaults {

    /** 트랙 너비. */
    val TrackWidth: Dp = 43.33.dp

    /** 트랙 높이. */
    val TrackHeight: Dp = 26.67.dp

    /** 트랙 안쪽 여백(썸과 트랙 사이). */
    val TrackPadding: Dp = 3.33.dp

    /** 썸 지름. */
    val ThumbSize: Dp = 20.dp

    @Composable
    @ReadOnlyComposable
    fun colors(
        checkedTrackColor: Color = LinkItTheme.color.semantic.primary.normal,
        uncheckedTrackColor: Color = LinkItTheme.color.semantic.fill.strong,
        thumbColor: Color = LinkItTheme.color.semantic.static.white,
        disabledCheckedTrackColor: Color = LinkItTheme.color.semantic.interaction.disable,
        disabledUncheckedTrackColor: Color = LinkItTheme.color.semantic.interaction.disable,
        disabledThumbColor: Color = LinkItTheme.color.semantic.static.white,
    ): SwitchColors = SwitchColors(
        checkedTrackColor = checkedTrackColor,
        uncheckedTrackColor = uncheckedTrackColor,
        thumbColor = thumbColor,
        disabledCheckedTrackColor = disabledCheckedTrackColor,
        disabledUncheckedTrackColor = disabledUncheckedTrackColor,
        disabledThumbColor = disabledThumbColor,
    )
}

/** [LinkItSwitch] 의 상태별 색 묶음. */
@Immutable
data class SwitchColors(
    val checkedTrackColor: Color,
    val uncheckedTrackColor: Color,
    val thumbColor: Color,
    val disabledCheckedTrackColor: Color,
    val disabledUncheckedTrackColor: Color,
    val disabledThumbColor: Color,
) {
    fun trackColor(checked: Boolean, enabled: Boolean): Color = when {
        enabled && checked -> checkedTrackColor
        enabled -> uncheckedTrackColor
        checked -> disabledCheckedTrackColor
        else -> disabledUncheckedTrackColor
    }

    fun thumbColor(enabled: Boolean): Color = if (enabled) thumbColor else disabledThumbColor
}

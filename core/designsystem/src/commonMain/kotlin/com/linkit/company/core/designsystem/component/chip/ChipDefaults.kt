package com.linkit.company.core.designsystem.component.chip

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.linkit.company.core.designsystem.foundation.color.token.PaletteTokens
import com.linkit.company.core.designsystem.theme.LinkItTheme

/**
 * [LinkItChip] 의 기본 값을 제공한다. Material3 의 `FilterChipDefaults` 와 동일한 역할.
 *
 * Figma "Pingo v3.0.3 - Action / Action Chip" 의 스펙을 따른다.
 */
object ChipDefaults {

    /** [ChipVariant.Outlined] 외곽선 두께. */
    val BorderWidth: Dp
        @Composable
        @ReadOnlyComposable
        get() = LinkItTheme.borderWidth.sm

    /** 리딩/트레일링 아이콘 크기. 모든 사이즈 공통. */
    val IconSize: Dp = 16.dp

    fun contentPadding(size: ChipSize): PaddingValues = when (size) {
        ChipSize.XSmall -> PaddingValues(horizontal = 7.dp, vertical = 4.dp)
        ChipSize.Small -> PaddingValues(horizontal = 8.dp, vertical = 6.dp)
        ChipSize.Medium -> PaddingValues(horizontal = 11.dp, vertical = 7.dp)
        ChipSize.Large -> PaddingValues(horizontal = 12.dp, vertical = 9.dp)
    }

    fun contentSpacing(size: ChipSize): Dp = when (size) {
        ChipSize.XSmall, ChipSize.Small -> 2.dp
        ChipSize.Medium, ChipSize.Large -> 3.dp
    }

    fun shape(size: ChipSize): Shape = when (size) {
        ChipSize.XSmall -> CornerXSmall // 6dp
        ChipSize.Small -> CornerSmall // 8dp
        ChipSize.Medium, ChipSize.Large -> CornerMedium // 10dp
    }

    @Composable
    @ReadOnlyComposable
    fun textStyle(size: ChipSize): TextStyle {
        val typography = LinkItTheme.typography
        return when (size) {
            ChipSize.XSmall -> typography.caption1Medium
            ChipSize.Small -> typography.label1NormalMedium
            ChipSize.Medium, ChipSize.Large -> typography.body2NormalMedium
        }
    }

    /** [variant]·[active] 조합별 색 묶음을 만든다. */
    @Composable
    @ReadOnlyComposable
    fun colors(
        variant: ChipVariant,
        active: Boolean,
    ): ChipColors {
        val semantic = LinkItTheme.color.semantic
        val coolNeutral25 = LinkItTheme.color.atomic.CoolNeutral25
        val disabledContent = semantic.label.disable
        return when (variant) {
            ChipVariant.Solid -> ChipColors(
                containerColor = if (active) coolNeutral25 else semantic.fill.alternative,
                contentColor = if (active) semantic.inverse.label else semantic.label.alternative,
                borderColor = Color.Unspecified,
                disabledContainerColor = semantic.interaction.disable,
                disabledContentColor = disabledContent,
                disabledBorderColor = Color.Unspecified,
            )

            ChipVariant.Outlined -> ChipColors(
                containerColor = if (active) coolNeutral25.copy(alpha = ActiveFillAlpha) else Color.Transparent,
                contentColor = if (active) coolNeutral25 else semantic.label.alternative,
                borderColor = if (active) coolNeutral25.copy(alpha = ActiveBorderAlpha) else semantic.line.normal.neutral,
                disabledContainerColor = Color.Transparent,
                disabledContentColor = disabledContent,
                disabledBorderColor = semantic.line.normal.neutral,
            )
        }
    }

    // Figma 칩 라운드(6/8/10dp)는 토큰에 없어 컴포넌트 전용으로 정의한다.
    private val CornerXSmall = RoundedCornerShape(6.dp)
    private val CornerSmall = RoundedCornerShape(8.dp)
    private val CornerMedium = RoundedCornerShape(10.dp)

    // Outlined + Active 상태의 옅은 면/외곽선 불투명도.
    private const val ActiveFillAlpha = PaletteTokens.Opacity5
    private const val ActiveBorderAlpha = PaletteTokens.Opacity43
}

/**
 * [LinkItChip] 의 상태별 색을 담는 불변 홀더.
 *
 * 면/외곽선이 없는 경우 해당 색은 [Color.Unspecified] 또는 [Color.Transparent] 로 둔다.
 */
@Immutable
data class ChipColors(
    val containerColor: Color,
    val contentColor: Color,
    val borderColor: Color,
    val disabledContainerColor: Color,
    val disabledContentColor: Color,
    val disabledBorderColor: Color,
) {
    fun containerColor(enabled: Boolean): Color = if (enabled) containerColor else disabledContainerColor

    fun contentColor(enabled: Boolean): Color = if (enabled) contentColor else disabledContentColor

    fun borderColor(enabled: Boolean): Color = if (enabled) borderColor else disabledBorderColor
}

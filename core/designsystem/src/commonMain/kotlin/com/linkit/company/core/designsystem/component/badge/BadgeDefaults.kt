package com.linkit.company.core.designsystem.component.badge

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.linkit.company.core.designsystem.foundation.color.token.PaletteTokens
import com.linkit.company.core.designsystem.theme.LinkItTheme

/**
 * [LinkItBadge] 의 기본 값을 제공한다. Material3 의 컴포넌트 `Defaults` 와 동일한 역할.
 */
object BadgeDefaults {

    /** 외곽선 두께. */
    val BorderWidth: Dp
        @Composable
        @ReadOnlyComposable
        get() = LinkItTheme.borderWidth.sm

    /** [BadgeColor.Accent] 의 기본 배경(채움·외곽선) 기준 색. */
    val AccentBackgroundColor: Color
        @Composable
        @ReadOnlyComposable
        get() = LinkItTheme.color.semantic.primary.normal

    /** [BadgeColor.Accent] 의 기본 텍스트 색. */
    val AccentContentColor: Color
        @Composable
        @ReadOnlyComposable
        get() = LinkItTheme.color.semantic.primary.normal

    /** [size] 에 맞는 모서리 모양. */
    fun shape(size: BadgeSize): Shape = when (size) {
        BadgeSize.XSmall, BadgeSize.Small -> RoundedCornerShape(SmallCornerRadius)
        BadgeSize.Medium -> RoundedCornerShape(MediumCornerRadius)
    }

    /** [size] 에 맞는 내부 여백. */
    fun contentPadding(size: BadgeSize): PaddingValues = when (size) {
        BadgeSize.XSmall -> PaddingValues(horizontal = 6.dp, vertical = 3.dp)
        BadgeSize.Small -> PaddingValues(horizontal = 6.dp, vertical = 5.dp)
        BadgeSize.Medium -> PaddingValues(horizontal = 8.dp, vertical = 6.dp)
    }

    /** [size] 에 맞는 타이포. */
    @Composable
    @ReadOnlyComposable
    fun textStyle(size: BadgeSize): TextStyle = when (size) {
        BadgeSize.XSmall -> LinkItTheme.typography.caption3Medium
        BadgeSize.Small -> LinkItTheme.typography.caption2Medium
        BadgeSize.Medium -> LinkItTheme.typography.caption1Medium
    }

    /**
     * [variant]·[color] 조합에 맞는 색상 묶음을 만든다.
     *
     * @param accentBackgroundColor [BadgeColor.Accent] 의 배경·외곽선 기준 색.
     * @param accentContentColor [BadgeColor.Accent] 의 텍스트 색. ([BadgeVariant.Transparent] 는 제외)
     */
    @Composable
    @ReadOnlyComposable
    fun colors(
        variant: BadgeVariant,
        color: BadgeColor,
        accentBackgroundColor: Color = AccentBackgroundColor,
        accentContentColor: Color = AccentContentColor,
    ): BadgeColors = when (color) {
        BadgeColor.Neutral -> neutralColors(variant)
        BadgeColor.Accent -> accentColors(variant, accentBackgroundColor, accentContentColor)
    }

    @Composable
    @ReadOnlyComposable
    private fun neutralColors(variant: BadgeVariant): BadgeColors {
        val contentColor = LinkItTheme.color.semantic.label.alternative
        return when (variant) {
            BadgeVariant.Solid -> BadgeColors(
                containerColor = LinkItTheme.color.semantic.fill.normal,
                contentColor = contentColor,
                borderColor = Color.Unspecified,
            )

            BadgeVariant.Outlined -> BadgeColors(
                containerColor = Color.Transparent,
                contentColor = contentColor,
                borderColor = LinkItTheme.color.semantic.line.normal.neutral,
            )

            BadgeVariant.Transparent -> BadgeColors(
                containerColor = LinkItTheme.color.semantic.static.black.copy(alpha = TransparentOverlayAlpha),
                contentColor = LinkItTheme.color.semantic.background.normal.normal,
                borderColor = Color.Unspecified,
            )
        }
    }

    @Composable
    @ReadOnlyComposable
    private fun accentColors(
        variant: BadgeVariant,
        backgroundColor: Color,
        contentColor: Color,
    ): BadgeColors = when (variant) {
        BadgeVariant.Solid -> BadgeColors(
            containerColor = backgroundColor.copy(alpha = AccentFillAlpha),
            contentColor = contentColor,
            borderColor = Color.Unspecified,
        )

        BadgeVariant.Outlined -> BadgeColors(
            containerColor = Color.Transparent,
            contentColor = contentColor,
            borderColor = backgroundColor.copy(alpha = AccentBorderAlpha),
        )

        BadgeVariant.Transparent -> BadgeColors(
            containerColor = backgroundColor.copy(alpha = AccentFillAlpha),
            contentColor = LinkItTheme.color.semantic.label.alternative,
            borderColor = Color.Unspecified,
        )
    }

    private val SmallCornerRadius: Dp = 6.dp
    private val MediumCornerRadius: Dp = 8.dp

    private const val TransparentOverlayAlpha = 0.20f
    private const val AccentFillAlpha = PaletteTokens.Opacity8
    private const val AccentBorderAlpha = PaletteTokens.Opacity43
}

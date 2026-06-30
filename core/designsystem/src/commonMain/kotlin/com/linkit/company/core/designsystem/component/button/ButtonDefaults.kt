package com.linkit.company.core.designsystem.component.button

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
import com.linkit.company.core.designsystem.foundation.shape.token.ShapeTokens
import com.linkit.company.core.designsystem.theme.LinkItTheme

/**
 * [LinkItButton]·[LinkItIconButton] 의 기본 값을 제공한다.
 * Material3 의 `ButtonDefaults` 와 동일한 역할로, 컴포넌트의 토큰을 한 곳에 모은다.
 *
 * Figma "Pingo v3.0.3 - Action / Button" 의 스펙을 따른다.
 */
object ButtonDefaults {

    /** [ButtonVariant.Outlined] 외곽선 두께. */
    val BorderWidth: Dp
        @Composable
        @ReadOnlyComposable
        get() = LinkItTheme.borderWidth.sm

    /** `shadow = true` 일 때 적용하는 그림자 높이. (Shadow/Normal/Small) */
    val ShadowElevation: Dp = 4.dp

    /** 텍스트 버튼의 좌우/상하 패딩. */
    fun contentPadding(size: ButtonSize): PaddingValues = when (size) {
        ButtonSize.Large -> PaddingValues(horizontal = 28.dp, vertical = 12.dp)
        ButtonSize.Medium -> PaddingValues(horizontal = 20.dp, vertical = 10.dp)
        ButtonSize.Small -> PaddingValues(horizontal = 14.dp, vertical = 7.dp)
    }

    /** 아이콘 전용 버튼의 사방 패딩. (정사각 48 / 40 / 32dp 를 만든다) */
    fun iconButtonPadding(size: ButtonSize): Dp = when (size) {
        ButtonSize.Large -> 12.dp
        ButtonSize.Medium -> 10.dp
        ButtonSize.Small -> 8.dp
    }

    /** 텍스트와 아이콘 사이 간격. */
    fun contentSpacing(size: ButtonSize): Dp = when (size) {
        ButtonSize.Large -> 6.dp
        ButtonSize.Medium -> 5.dp
        ButtonSize.Small -> 4.dp
    }

    /** 리딩/트레일링 아이콘 및 아이콘 전용 버튼의 아이콘 크기. */
    fun iconSize(size: ButtonSize): Dp = when (size) {
        ButtonSize.Large -> 24.dp
        ButtonSize.Medium -> 20.dp
        ButtonSize.Small -> 16.dp
    }

    fun shape(size: ButtonSize): Shape = when (size) {
        ButtonSize.Large -> ShapeTokens.CornerXl // 12dp
        ButtonSize.Medium -> CornerMedium // 10dp
        ButtonSize.Small -> ShapeTokens.CornerLg // 8dp
    }

    /**
     * [color] 에 따라 굵기가, [size] 에 따라 크기가 달라지는 라벨 텍스트 스타일.
     * Primary 는 Bold, Assistive 는 Regular 를 사용한다.
     */
    @Composable
    @ReadOnlyComposable
    fun textStyle(size: ButtonSize, color: ButtonColor): TextStyle {
        val typography = LinkItTheme.typography
        return when (size) {
            ButtonSize.Large ->
                if (color == ButtonColor.Primary) typography.body2NormalBold else typography.body2NormalRegular
            ButtonSize.Medium ->
                if (color == ButtonColor.Primary) typography.label1NormalBold else typography.label1NormalRegular
            ButtonSize.Small ->
                if (color == ButtonColor.Primary) typography.caption1Bold else typography.caption1Regular
        }
    }

    /** [variant]·[color] 조합별 색 묶음을 만든다. */
    @Composable
    @ReadOnlyComposable
    fun colors(
        variant: ButtonVariant,
        color: ButtonColor,
    ): ButtonColors {
        val semantic = LinkItTheme.color.semantic
        val disabledContainer = semantic.interaction.disable
        val disabledContent = semantic.label.disable
        return when (variant) {
            ButtonVariant.Solid -> when (color) {
                ButtonColor.Primary -> ButtonColors(
                    containerColor = LinkItTheme.color.atomic.CoolNeutral25,
                    contentColor = semantic.static.white,
                    borderColor = Color.Unspecified,
                    disabledContainerColor = disabledContainer,
                    disabledContentColor = disabledContent,
                    disabledBorderColor = Color.Unspecified,
                )

                ButtonColor.Assistive -> ButtonColors(
                    containerColor = semantic.fill.normal,
                    contentColor = semantic.label.neutral,
                    borderColor = Color.Unspecified,
                    disabledContainerColor = disabledContainer,
                    disabledContentColor = disabledContent,
                    disabledBorderColor = Color.Unspecified,
                )
            }

            ButtonVariant.Outlined -> ButtonColors(
                containerColor = semantic.background.normal.normal,
                contentColor = if (color == ButtonColor.Primary) semantic.primary.normal else semantic.label.neutral,
                borderColor = semantic.line.normal.neutral,
                disabledContainerColor = disabledContainer,
                disabledContentColor = disabledContent,
                disabledBorderColor = Color.Unspecified,
            )
        }
    }

    // Figma Medium 버튼의 라운드(10dp)는 토큰에 없어 컴포넌트 전용으로 정의한다.
    private val CornerMedium = RoundedCornerShape(10.dp)
}

/**
 * [LinkItButton] 의 상태별 색을 담는 불변 홀더. Material3 의 `ButtonColors` 와 동일한 역할.
 *
 * 외곽선이 없는 경우 [borderColor]·[disabledBorderColor] 는 [Color.Unspecified] 로 둔다.
 */
@Immutable
data class ButtonColors(
    val containerColor: Color,
    val contentColor: Color,
    val borderColor: Color,
    val disabledContainerColor: Color,
    val disabledContentColor: Color,
    val disabledBorderColor: Color,
) {
    /** 활성 여부에 따른 컨테이너 색. */
    fun containerColor(enabled: Boolean): Color = if (enabled) containerColor else disabledContainerColor

    /** 활성 여부에 따른 콘텐츠(텍스트·아이콘) 색. */
    fun contentColor(enabled: Boolean): Color = if (enabled) contentColor else disabledContentColor

    /** 활성 여부에 따른 외곽선 색. */
    fun borderColor(enabled: Boolean): Color = if (enabled) borderColor else disabledBorderColor
}

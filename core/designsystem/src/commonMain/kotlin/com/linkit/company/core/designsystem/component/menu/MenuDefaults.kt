package com.linkit.company.core.designsystem.component.menu

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
import com.linkit.company.core.designsystem.theme.LinkItTheme

/**
 * [LinkItMenu]·[LinkItMenuItem] 의 기본 값을 제공한다. Material3 의 `MenuDefaults` 와 동일한 역할.
 *
 * Figma "Pingo v3.0.3 - Presentation / Menu" 의 스펙을 따른다.
 */
object MenuDefaults {

    /** 메뉴 카드 최소 너비. (Figma `min-w-140`) */
    val MinWidth: Dp = 140.dp

    /** 메뉴 카드 그림자 높이. (Shadow/Normal/Small) */
    val ShadowElevation: Dp = 4.dp

    /** 메뉴 카드 외곽선 두께. */
    val BorderWidth: Dp
        @Composable
        @ReadOnlyComposable
        get() = LinkItTheme.borderWidth.sm

    /** 메뉴 카드 모서리 모양. (Corner Radius 16) */
    val ContainerShape: Shape
        @Composable
        @ReadOnlyComposable
        get() = LinkItTheme.shape.xxl

    /** 메뉴 카드 안쪽 여백. 좌우 8dp 안쪽에서 항목 하이라이트가 그려진다. */
    val ContainerPadding: PaddingValues = PaddingValues(horizontal = 8.dp, vertical = 8.dp)

    /** 항목 사이 세로 간격. */
    val ItemSpacing: Dp = 4.dp

    /** 항목 하이라이트(상태 레이어) 모서리 모양. (Corner Radius 12) */
    val ItemShape: Shape = RoundedCornerShape(12.dp)

    /** 항목 좌우 여백. [ContainerPadding] 와 합쳐 텍스트가 카드 가장자리에서 20dp 떨어진다. */
    val ItemHorizontalPadding: Dp = 12.dp

    /** 항목 내부 요소(체크박스·텍스트) 사이 간격. */
    val ItemContentSpacing: Dp = 8.dp

    /** 항목 앞 아이콘 크기. */
    val LeadingIconSize: Dp = 24.dp

    /** 항목 콘텐츠 최소 높이. (Figma `min-h-24`) */
    val ItemMinContentHeight: Dp = 24.dp

    /** 체크박스 한 변 크기. */
    val CheckboxSize: Dp = 18.dp

    /** 체크박스 모서리 모양. */
    val CheckboxShape: Shape = RoundedCornerShape(5.dp)

    /** 체크박스 외곽선 두께. */
    val CheckboxBorderWidth: Dp = 1.5.dp

    /** 체크박스 안 체크 아이콘 크기. */
    val CheckIconSize: Dp = 16.dp

    /** 비활성 항목에 적용하는 불투명도. (Opacity/43) */
    const val DisabledAlpha: Float = 0.43f

    /** [padding] 규격에 맞는 항목 상하 여백. */
    fun itemVerticalPadding(padding: MenuItemPadding): Dp = when (padding) {
        MenuItemPadding.Compact -> 8.dp
        MenuItemPadding.Regular -> 12.dp
    }

    /** 항목 텍스트 타이포. */
    val textStyle: TextStyle
        @Composable
        @ReadOnlyComposable
        get() = LinkItTheme.typography.label1NormalMedium

    /** 메뉴 카드 배경 색. */
    val containerColor: Color
        @Composable
        @ReadOnlyComposable
        get() = LinkItTheme.color.semantic.background.elevated.normal

    /** 메뉴 카드 외곽선 색. */
    val borderColor: Color
        @Composable
        @ReadOnlyComposable
        get() = LinkItTheme.color.semantic.line.solid.neutral

    /** 항목 상태별 색 묶음을 만든다. */
    @Composable
    @ReadOnlyComposable
    fun itemColors(): MenuItemColors {
        val semantic = LinkItTheme.color.semantic
        return MenuItemColors(
            textColor = semantic.label.normal,
            selectedTextColor = semantic.primary.normal,
            disabledTextColor = semantic.label.alternative,
            checkboxBorderColor = semantic.line.normal.normal,
            checkboxCheckedColor = semantic.primary.normal,
            checkboxCheckColor = semantic.static.white,
        )
    }
}

/**
 * [LinkItMenuItem] 의 상태별 색을 담는 불변 홀더. [MenuDefaults.itemColors] 로 생성한다.
 *
 * @property textColor 기본 텍스트 색.
 * @property selectedTextColor [MenuItemVariant.Normal] 선택 시 텍스트 색.
 * @property disabledTextColor 비활성 시 텍스트 색.
 * @property checkboxBorderColor 미선택 체크박스 외곽선 색.
 * @property checkboxCheckedColor 선택 체크박스 채움 색.
 * @property checkboxCheckColor 체크 아이콘 색.
 */
@Immutable
data class MenuItemColors(
    val textColor: Color,
    val selectedTextColor: Color,
    val disabledTextColor: Color,
    val checkboxBorderColor: Color,
    val checkboxCheckedColor: Color,
    val checkboxCheckColor: Color,
) {
    /** [variant]·[selected]·[enabled] 조합에 맞는 텍스트 색. */
    fun textColor(variant: MenuItemVariant, selected: Boolean, enabled: Boolean): Color = when {
        !enabled -> disabledTextColor
        variant == MenuItemVariant.Normal && selected -> selectedTextColor
        else -> textColor
    }
}

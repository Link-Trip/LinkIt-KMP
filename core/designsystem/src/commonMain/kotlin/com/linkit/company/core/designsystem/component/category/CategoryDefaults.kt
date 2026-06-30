package com.linkit.company.core.designsystem.component.category

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
 * [LinkItCategory]·[LinkItCategoryItem] 의 기본 값을 제공한다. Material3 의 `FilterChipDefaults` 와 동일한 역할.
 *
 * Figma "Pingo v3.0.3 - Navigation / Category" 의 스펙을 따른다.
 */
object CategoryDefaults {

    /** 선택되지 않은 아이템의 외곽선 두께. */
    val BorderWidth: Dp
        @Composable
        @ReadOnlyComposable
        get() = LinkItTheme.borderWidth.sm

    /** 아이템(칩) 사이 간격. */
    val ItemSpacing: Dp = 6.dp

    /** 트레일링 슬롯 앞 간격. */
    val TrailingSpacing: Dp = 20.dp

    /** `horizontalPadding = true` 일 때 묶음 좌우에 적용하는 여백. */
    val HorizontalPadding: Dp = 20.dp

    /** 리딩/트레일링 아이콘 크기. 모든 사이즈 공통. */
    val IconSize: Dp = 16.dp

    fun contentPadding(size: CategorySize): PaddingValues = when (size) {
        CategorySize.Small -> PaddingValues(horizontal = 7.dp, vertical = 4.dp)
        CategorySize.Medium -> PaddingValues(horizontal = 8.dp, vertical = 6.dp)
        CategorySize.Large -> PaddingValues(horizontal = 11.dp, vertical = 7.dp)
    }

    fun contentSpacing(size: CategorySize): Dp = when (size) {
        CategorySize.Small, CategorySize.Medium -> 2.dp
        CategorySize.Large -> 3.dp
    }

    fun shape(size: CategorySize): Shape = when (size) {
        CategorySize.Small -> CornerSmall // 6dp
        CategorySize.Medium -> CornerMedium // 8dp
        CategorySize.Large -> CornerLarge // 10dp
    }

    @Composable
    @ReadOnlyComposable
    fun textStyle(size: CategorySize, selected: Boolean): TextStyle {
        val typography = LinkItTheme.typography
        return when (size) {
            CategorySize.Small -> if (selected) typography.caption1Semibold else typography.caption1Medium
            CategorySize.Medium -> if (selected) typography.label1NormalSemibold else typography.label1NormalMedium
            CategorySize.Large -> if (selected) typography.body2NormalSemibold else typography.body2NormalMedium
        }
    }

    /** 선택/비선택 상태별 색 묶음을 만든다. */
    @Composable
    @ReadOnlyComposable
    fun colors(): CategoryColors {
        val semantic = LinkItTheme.color.semantic
        return CategoryColors(
            selectedContainerColor = LinkItTheme.color.atomic.CoolNeutral25,
            selectedContentColor = semantic.inverse.label,
            unselectedContainerColor = Color.Transparent,
            unselectedContentColor = semantic.label.alternative,
            unselectedBorderColor = semantic.line.normal.neutral,
            disabledContentColor = semantic.label.disable,
        )
    }

    // Figma 칩 라운드(6/8/10dp)는 토큰에 없어 컴포넌트 전용으로 정의한다.
    private val CornerSmall = RoundedCornerShape(6.dp)
    private val CornerMedium = RoundedCornerShape(8.dp)
    private val CornerLarge = RoundedCornerShape(10.dp)
}

/**
 * [LinkItCategoryItem] 의 상태별 색을 담는 불변 홀더.
 *
 * 선택 시 면을 채우고, 비선택 시 외곽선만 그린다.
 */
@Immutable
data class CategoryColors(
    val selectedContainerColor: Color,
    val selectedContentColor: Color,
    val unselectedContainerColor: Color,
    val unselectedContentColor: Color,
    val unselectedBorderColor: Color,
    val disabledContentColor: Color,
) {
    fun containerColor(selected: Boolean): Color =
        if (selected) selectedContainerColor else unselectedContainerColor

    fun contentColor(selected: Boolean, enabled: Boolean): Color = when {
        !enabled -> disabledContentColor
        selected -> selectedContentColor
        else -> unselectedContentColor
    }

    /** 선택 상태에서는 면을 채우므로 외곽선을 그리지 않는다([Color.Unspecified]). */
    fun borderColor(selected: Boolean): Color =
        if (selected) Color.Unspecified else unselectedBorderColor
}

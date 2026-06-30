package com.linkit.company.core.designsystem.component.textarea

import androidx.compose.foundation.layout.PaddingValues
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
 * [LinkItTextArea]의 기본값을 제공한다. Material3의 `TextFieldDefaults`와 동일한 방식으로
 * 치수/모양/타이포그래피와 [colors] 팩토리를 노출한다.
 */
object TextAreaDefaults {

    /** 입력 영역의 최소 높이 (Figma 82px). */
    val MinHeight: Dp = 82.dp

    /** 입력 영역 내부 여백 (좌우 16, 상하 12). */
    val ContentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 12.dp)

    /** 입력 영역에 적용되는 드롭 섀도우 (Shadow/Normal/Xsmall). */
    val ContainerShadowElevation: Dp = 1.dp

    val shape: Shape
        @Composable
        @ReadOnlyComposable
        get() = LinkItTheme.shape.xl

    val textStyle: TextStyle
        @Composable
        @ReadOnlyComposable
        get() = LinkItTheme.typography.body2NormalRegular

    val labelTextStyle: TextStyle
        @Composable
        @ReadOnlyComposable
        get() = LinkItTheme.typography.label2Bold

    val supportingTextStyle: TextStyle
        @Composable
        @ReadOnlyComposable
        get() = LinkItTheme.typography.caption1Medium

    /** 비포커스 상태 테두리 두께. */
    val BorderWidth: Dp
        @Composable
        @ReadOnlyComposable
        get() = LinkItTheme.borderWidth.sm

    /** 포커스 상태 테두리 두께. */
    val FocusedBorderWidth: Dp
        @Composable
        @ReadOnlyComposable
        get() = LinkItTheme.borderWidth.md

    @Composable
    @ReadOnlyComposable
    fun colors(
        textColor: Color = LinkItTheme.color.semantic.label.normal,
        disabledTextColor: Color = LinkItTheme.color.semantic.label.disable,
        placeholderColor: Color = LinkItTheme.color.semantic.label.assistive,
        labelColor: Color = LinkItTheme.color.semantic.label.neutral,
        disabledLabelColor: Color = LinkItTheme.color.semantic.label.disable,
        cursorColor: Color = LinkItTheme.color.semantic.primary.normal,
        containerColor: Color = LinkItTheme.color.semantic.background.normal.normal,
        disabledContainerColor: Color = LinkItTheme.color.semantic.interaction.disable,
        unfocusedBorderColor: Color = LinkItTheme.color.semantic.line.normal.neutral,
        focusedBorderColor: Color = LinkItTheme.color.semantic.primary.normal,
        errorBorderColor: Color = LinkItTheme.color.semantic.status.negative,
        errorFocusedBorderColor: Color = LinkItTheme.color.semantic.status.negative,
        disabledBorderColor: Color = LinkItTheme.color.semantic.line.normal.alternative,
        supportingTextColor: Color = LinkItTheme.color.semantic.label.alternative,
        errorSupportingTextColor: Color = LinkItTheme.color.semantic.status.negative,
        disabledSupportingTextColor: Color = LinkItTheme.color.semantic.label.disable,
    ): TextAreaColors = TextAreaColors(
        textColor = textColor,
        disabledTextColor = disabledTextColor,
        placeholderColor = placeholderColor,
        labelColor = labelColor,
        disabledLabelColor = disabledLabelColor,
        cursorColor = cursorColor,
        containerColor = containerColor,
        disabledContainerColor = disabledContainerColor,
        unfocusedBorderColor = unfocusedBorderColor,
        focusedBorderColor = focusedBorderColor,
        errorBorderColor = errorBorderColor,
        errorFocusedBorderColor = errorFocusedBorderColor,
        disabledBorderColor = disabledBorderColor,
        supportingTextColor = supportingTextColor,
        errorSupportingTextColor = errorSupportingTextColor,
        disabledSupportingTextColor = disabledSupportingTextColor,
    )
}

/**
 * [LinkItTextArea]의 각 상태별 색상 집합. 상태(enabled / isError / focused)에 따른 실제 색상은
 * resolver 메서드를 통해 계산한다.
 */
@Immutable
data class TextAreaColors(
    val textColor: Color,
    val disabledTextColor: Color,
    val placeholderColor: Color,
    val labelColor: Color,
    val disabledLabelColor: Color,
    val cursorColor: Color,
    val containerColor: Color,
    val disabledContainerColor: Color,
    val unfocusedBorderColor: Color,
    val focusedBorderColor: Color,
    val errorBorderColor: Color,
    val errorFocusedBorderColor: Color,
    val disabledBorderColor: Color,
    val supportingTextColor: Color,
    val errorSupportingTextColor: Color,
    val disabledSupportingTextColor: Color,
) {
    internal fun resolveTextColor(enabled: Boolean): Color =
        if (enabled) textColor else disabledTextColor

    internal fun resolveLabelColor(enabled: Boolean): Color =
        if (enabled) labelColor else disabledLabelColor

    internal fun resolveContainerColor(enabled: Boolean): Color =
        if (enabled) containerColor else disabledContainerColor

    internal fun resolveBorderColor(enabled: Boolean, isError: Boolean, focused: Boolean): Color =
        when {
            !enabled -> disabledBorderColor
            isError && focused -> errorFocusedBorderColor
            isError -> errorBorderColor
            focused -> focusedBorderColor
            else -> unfocusedBorderColor
        }

    internal fun resolveSupportingTextColor(enabled: Boolean, isError: Boolean): Color =
        when {
            !enabled -> disabledSupportingTextColor
            isError -> errorSupportingTextColor
            else -> supportingTextColor
        }
}

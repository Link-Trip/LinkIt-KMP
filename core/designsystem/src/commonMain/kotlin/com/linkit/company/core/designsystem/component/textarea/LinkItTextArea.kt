package com.linkit.company.core.designsystem.component.textarea

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import com.linkit.company.core.designsystem.theme.LinkItTheme

/**
 * 긴 텍스트를 입력하거나 편집할 때 사용하는 멀티라인 입력 영역(Textarea).
 *
 * Figma "Selection and Input / Textinput / Textarea" 디자인을 구현한다. 포커스 여부는
 * [interactionSource]에서, 입력 여부(active)는 [value]에서 자동으로 파생되며, 호출부는
 * 오류 여부([isError])와 보조 문구([supportingText])만 제어하면 된다.
 *
 * @param value 현재 입력 값
 * @param onValueChange 입력 값 변경 콜백
 * @param label 입력 영역 상단에 노출되는 제목. null이면 표시하지 않는다.
 * @param placeholder 값이 비어있을 때 노출되는 안내 문구
 * @param supportingText 입력 영역 하단의 보조 문구. null이면 표시하지 않는다.
 * @param enabled 입력 가능 여부
 * @param isError 오류 상태(Negative) 여부
 * @param maxLength 입력 가능한 최대 글자 수. null이면 제한하지 않는다.
 * @param colors [TextAreaDefaults.colors]로 생성하는 상태별 색상
 */
@Composable
fun LinkItTextArea(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    supportingText: String? = null,
    enabled: Boolean = true,
    isError: Boolean = false,
    maxLength: Int? = null,
    textStyle: TextStyle = TextAreaDefaults.textStyle,
    shape: Shape = TextAreaDefaults.shape,
    colors: TextAreaColors = TextAreaDefaults.colors(),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    val focused by interactionSource.collectIsFocusedAsState()

    val borderColor = colors.resolveBorderColor(enabled = enabled, isError = isError, focused = focused)
    val borderWidth = if (focused) TextAreaDefaults.FocusedBorderWidth else TextAreaDefaults.BorderWidth

    val resolvedTextColor = colors.resolveTextColor(enabled)
    val mergedTextStyle = remember(textStyle, resolvedTextColor) {
        textStyle.copy(color = resolvedTextColor)
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(LinkItTheme.spacing.space8),
    ) {
        if (label != null) {
            Text(
                text = label,
                style = TextAreaDefaults.labelTextStyle,
                color = colors.resolveLabelColor(enabled),
            )
        }

        BasicTextField(
            value = value,
            onValueChange = { newValue ->
                if (maxLength == null || newValue.length <= maxLength) {
                    onValueChange(newValue)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = TextAreaDefaults.MinHeight),
            enabled = enabled,
            textStyle = mergedTextStyle,
            cursorBrush = SolidColor(colors.cursorColor),
            interactionSource = interactionSource,
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = TextAreaDefaults.MinHeight)
                        .shadow(TextAreaDefaults.ContainerShadowElevation, shape)
                        .background(colors.resolveContainerColor(enabled), shape)
                        .border(borderWidth, borderColor, shape)
                        .padding(TextAreaDefaults.ContentPadding),
                ) {
                    if (value.isEmpty() && placeholder != null) {
                        Text(
                            text = placeholder,
                            style = textStyle,
                            color = colors.placeholderColor,
                        )
                    }
                    innerTextField()
                }
            },
        )

        if (supportingText != null) {
            Text(
                text = supportingText,
                style = TextAreaDefaults.supportingTextStyle,
                color = colors.resolveSupportingTextColor(enabled = enabled, isError = isError),
            )
        }
    }
}

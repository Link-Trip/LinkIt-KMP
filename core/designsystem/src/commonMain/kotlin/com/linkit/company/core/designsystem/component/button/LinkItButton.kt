package com.linkit.company.core.designsystem.component.button

import androidx.compose.foundation.Indication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import com.linkit.company.core.designsystem.foundation.interaction.InteractionDefaults

/**
 * 사용자가 원하는 동작을 수행하도록 돕는 기본 버튼.
 *
 * Figma "Pingo v3.0.3 - Action / Button" 을 구현한 것으로, [variant]·[color]·[size] 조합으로
 * 시각 스타일을, [leadingIcon]·[trailingIcon] 으로 아이콘을 함께 표현한다.
 * 아이콘만 있는 버튼은 [LinkItIconButton] 을 사용한다.
 *
 * @param onClick 클릭 콜백.
 * @param text 버튼 라벨.
 * @param variant 면 채움([ButtonVariant.Solid]) / 외곽선([ButtonVariant.Outlined]).
 * @param color 색 역할. Primary 는 강조·Bold, Assistive 는 중립·Regular.
 * @param size 크기 규격. 텍스트 스타일·패딩·라운드가 함께 바뀐다.
 * @param enabled `false` 면 비활성 색을 쓰고 클릭을 막는다.
 * @param shadow `true` 면 떠 있는 느낌의 그림자를 더한다.
 * @param leadingIcon 텍스트 앞 아이콘.
 * @param trailingIcon 텍스트 뒤 아이콘.
 */
@Composable
fun LinkItButton(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    variant: ButtonVariant = ButtonVariant.Solid,
    color: ButtonColor = ButtonColor.Primary,
    size: ButtonSize = ButtonSize.Large,
    enabled: Boolean = true,
    shadow: Boolean = false,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    colors: ButtonColors = ButtonDefaults.colors(variant, color),
    shape: Shape = ButtonDefaults.shape(size),
    contentPadding: PaddingValues = ButtonDefaults.contentPadding(size),
    textStyle: TextStyle = ButtonDefaults.textStyle(size, color),
) {
    val contentColor = colors.contentColor(enabled)
    Row(
        modifier = modifier
            .buttonContainer(
                shape = shape,
                containerColor = colors.containerColor(enabled),
                borderColor = colors.borderColor(enabled),
                borderWidth = ButtonDefaults.BorderWidth,
                shadow = shadow,
                shadowElevation = ButtonDefaults.ShadowElevation,
                enabled = enabled,
                indication = InteractionDefaults.indication(),
                onClick = onClick,
            )
            .padding(contentPadding),
        horizontalArrangement = Arrangement.spacedBy(ButtonDefaults.contentSpacing(size), Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leadingIcon != null) {
            ButtonIcon(leadingIcon, ButtonDefaults.iconSize(size), contentColor)
        }
        Text(
            text = text,
            style = textStyle,
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (trailingIcon != null) {
            ButtonIcon(trailingIcon, ButtonDefaults.iconSize(size), contentColor)
        }
    }
}

/**
 * 아이콘 하나만 담는 정사각 버튼. 색·크기·상태 규칙은 [LinkItButton] 과 동일하다.
 *
 * @param icon 표시할 아이콘.
 * @param contentDescription 접근성 설명. 장식용이면 `null`.
 */
@Composable
fun LinkItIconButton(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    variant: ButtonVariant = ButtonVariant.Solid,
    color: ButtonColor = ButtonColor.Primary,
    size: ButtonSize = ButtonSize.Large,
    enabled: Boolean = true,
    shadow: Boolean = false,
    colors: ButtonColors = ButtonDefaults.colors(variant, color),
    shape: Shape = ButtonDefaults.shape(size),
) {
    Box(
        modifier = modifier
            .buttonContainer(
                shape = shape,
                containerColor = colors.containerColor(enabled),
                borderColor = colors.borderColor(enabled),
                borderWidth = ButtonDefaults.BorderWidth,
                shadow = shadow,
                shadowElevation = ButtonDefaults.ShadowElevation,
                enabled = enabled,
                indication = InteractionDefaults.indication(),
                onClick = onClick,
            )
            .padding(ButtonDefaults.iconButtonPadding(size)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(ButtonDefaults.iconSize(size)),
            tint = colors.contentColor(enabled),
        )
    }
}

@Composable
private fun ButtonIcon(icon: ImageVector, iconSize: Dp, tint: Color) {
    Icon(
        imageVector = icon,
        contentDescription = null,
        modifier = Modifier.size(iconSize),
        tint = tint,
    )
}

/**
 * 그림자 → 클립 → 배경 → 외곽선 → 클릭 → (호출부에서) 패딩 순으로 버튼 컨테이너를 구성한다.
 * [LinkItButton] 과 [LinkItIconButton] 의 공통 모양/상호작용 로직.
 */
private fun Modifier.buttonContainer(
    shape: Shape,
    containerColor: Color,
    borderColor: Color,
    borderWidth: Dp,
    shadow: Boolean,
    shadowElevation: Dp,
    enabled: Boolean,
    indication: Indication,
    onClick: () -> Unit,
): Modifier = this
    .then(if (shadow) Modifier.shadow(shadowElevation, shape) else Modifier)
    .clip(shape)
    .background(containerColor)
    .then(if (borderColor.isSpecified) Modifier.border(borderWidth, borderColor, shape) else Modifier)
    .clickable(
        interactionSource = null,
        indication = indication,
        enabled = enabled,
        role = Role.Button,
        onClick = onClick,
    )

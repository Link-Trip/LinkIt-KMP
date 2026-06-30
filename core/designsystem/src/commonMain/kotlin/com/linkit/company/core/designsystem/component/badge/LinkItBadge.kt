package com.linkit.company.core.designsystem.component.badge

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.text.TextStyle

/**
 * 콘텐츠의 상태나 속성을 시각적으로 강조하는 콘텐츠 뱃지.
 *
 * 낮은 시각 위계를 가지며, 정보를 항목별로 분류할 때 사용한다.
 *
 * @param text 뱃지에 표시할 문구.
 * @param variant 시각적 스타일. ([BadgeVariant.Solid] 기본)
 * @param size 크기 규격. ([BadgeSize.Small] 기본)
 * @param color 색상 속성. ([BadgeColor.Neutral] 기본)
 * @param accentBackgroundColor [BadgeColor.Accent] 의 배경·외곽선 기준 색.
 * @param accentContentColor [BadgeColor.Accent] 의 텍스트 색.
 * @param colors 색상 묶음. 기본은 [variant]·[color] 조합으로 계산한다.
 * @param shape 모서리 모양. 기본은 [size] 에 따른다.
 * @param contentPadding 내부 여백. 기본은 [size] 에 따른다.
 * @param textStyle 텍스트 타이포. 기본은 [size] 에 따른다.
 */
@Composable
fun LinkItBadge(
    text: String,
    modifier: Modifier = Modifier,
    variant: BadgeVariant = BadgeVariant.Solid,
    size: BadgeSize = BadgeSize.Small,
    color: BadgeColor = BadgeColor.Neutral,
    accentBackgroundColor: Color = BadgeDefaults.AccentBackgroundColor,
    accentContentColor: Color = BadgeDefaults.AccentContentColor,
    colors: BadgeColors = BadgeDefaults.colors(
        variant = variant,
        color = color,
        accentBackgroundColor = accentBackgroundColor,
        accentContentColor = accentContentColor,
    ),
    shape: Shape = BadgeDefaults.shape(size),
    contentPadding: PaddingValues = BadgeDefaults.contentPadding(size),
    textStyle: TextStyle = BadgeDefaults.textStyle(size),
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(colors.containerColor)
            .then(
                if (colors.borderColor.isSpecified) {
                    Modifier.border(BadgeDefaults.BorderWidth, colors.borderColor, shape)
                } else {
                    Modifier
                },
            )
            .padding(contentPadding),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = textStyle,
            color = colors.contentColor,
            maxLines = 1,
        )
    }
}

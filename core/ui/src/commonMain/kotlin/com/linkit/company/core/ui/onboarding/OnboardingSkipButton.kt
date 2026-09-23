package com.linkit.company.core.ui.onboarding

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.linkit.company.core.designsystem.component.button.ButtonColor
import com.linkit.company.core.designsystem.component.button.ButtonColors
import com.linkit.company.core.designsystem.component.button.ButtonSize
import com.linkit.company.core.designsystem.component.button.ButtonVariant
import com.linkit.company.core.designsystem.component.button.LinkItButton
import com.linkit.company.core.designsystem.theme.LinkItTheme

/**
 * 튜토리얼 화면 우상단 `건너뛰기` 버튼(Figma `18058:273713`, FR-013).
 *
 * 지도·영상 링크로 만들기 화면이 같은 모양을 쓰도록 여기서 한 번만 정의한다.
 * 흰 배경 + `line.normal.neutral` 1px 테두리, radius 10, 높이 38, 패딩 20/9, `label1NormalMedium`.
 * 호출부는 상태바 아래 10dp, 오른쪽 20dp 위치에 둔다.
 */
@Composable
fun OnboardingSkipButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val semantic = LinkItTheme.color.semantic
    LinkItButton(
        onClick = onClick,
        text = OnboardingSkipButtonDefaults.Text,
        variant = ButtonVariant.Outlined,
        color = ButtonColor.Assistive,
        size = ButtonSize.Medium,
        colors = ButtonColors(
            containerColor = semantic.background.normal.normal,
            contentColor = semantic.label.normal,
            borderColor = semantic.line.normal.neutral,
            disabledContainerColor = semantic.interaction.disable,
            disabledContentColor = semantic.label.disable,
            disabledBorderColor = Color.Unspecified,
        ),
        shape = RoundedCornerShape(OnboardingSkipButtonDefaults.CornerRadius),
        contentPadding = OnboardingSkipButtonDefaults.ContentPadding,
        textStyle = LinkItTheme.typography.label1NormalMedium,
        modifier = modifier.height(OnboardingSkipButtonDefaults.Height),
    )
}

object OnboardingSkipButtonDefaults {
    const val Text = "건너뛰기"
    val Height = 38.dp
    val CornerRadius = 10.dp
    val ContentPadding = PaddingValues(horizontal = 20.dp, vertical = 9.dp)

    /** 화면 상단(상태바 제외)에서 버튼까지의 여백 */
    val TopMargin = 10.dp

    /** 화면 오른쪽 가장자리에서 버튼까지의 여백 */
    val EndMargin = 20.dp
}

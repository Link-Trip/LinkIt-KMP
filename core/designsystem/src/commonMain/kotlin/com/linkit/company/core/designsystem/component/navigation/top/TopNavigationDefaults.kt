package com.linkit.company.core.designsystem.component.navigation

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.linkit.company.core.designsystem.foundation.icon.LinkItIcon
import com.linkit.company.core.designsystem.theme.LinkItTheme

/**
 * [LinkItTopNavigation]의 기본 값과 슬롯에 채워 넣을 표준 요소를 제공한다.
 *
 * Material3의 `TopAppBarDefaults`와 동일한 역할로, 컴포넌트 사용처에서 색·치수·기본 아이콘 버튼을
 * 일관되게 사용할 수 있도록 한다.
 */
object TopNavigationDefaults {

    /** 바의 고정 높이. */
    val Height: Dp = 56.dp

    /** 바 콘텐츠의 좌우 여백. [IconButtonSize] - [IconSize] 의 절반(8dp)을 더하면 16dp 콘텐츠 마진에 정렬된다. */
    val HorizontalPadding: Dp = 8.dp

    /** Leading/Trailing 아이콘 버튼의 터치/인터랙션 영역 크기. */
    val IconButtonSize: Dp = 40.dp

    /** 아이콘 자체의 크기. */
    val IconSize: Dp = 24.dp

    /** Leading 과 제목 사이 간격. */
    val LeadingTitleSpacing: Dp = 4.dp

    /** 제목 좌우 여백. */
    val TitleHorizontalPadding: Dp = 4.dp

    val containerColor: Color
        @Composable
        @ReadOnlyComposable
        get() = Color.Transparent

    val titleColor: Color
        @Composable
        @ReadOnlyComposable
        get() = LinkItTheme.color.semantic.label.strong

    val iconColor: Color
        @Composable
        @ReadOnlyComposable
        get() = LinkItTheme.color.semantic.label.normal

    @Composable
    @ReadOnlyComposable
    fun titleStyle(variant: TopNavigationVariant): TextStyle = when (variant) {
        TopNavigationVariant.Normal -> LinkItTheme.typography.headline2Bold
        TopNavigationVariant.Extended -> LinkItTheme.typography.heading2Bold
        TopNavigationVariant.Reduced -> LinkItTheme.typography.label1NormalBold
        TopNavigationVariant.Floating -> LinkItTheme.typography.headline2Bold
    }

    /** 가장 흔한 Leading 요소인 뒤로 가기(chevron-left) 버튼. */
    @Composable
    fun BackButton(
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
        contentDescription: String? = "뒤로 가기",
        enabled: Boolean = true,
        tint: Color = iconColor,
    ) {
        IconButton(
            icon = LinkItIcon.Arrow.ChevronLeft,
            onClick = onClick,
            modifier = modifier,
            contentDescription = contentDescription,
            enabled = enabled,
            tint = tint,
        )
    }

    /** Leading/Trailing 슬롯에 넣는 표준 아이콘 버튼. 24dp 아이콘을 40dp 원형 인터랙션 영역에 담는다. */
    @Composable
    fun IconButton(
        icon: ImageVector,
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
        contentDescription: String? = null,
        enabled: Boolean = true,
        tint: Color = iconColor,
    ) {
        IconButton(
            onClick = onClick,
            modifier = modifier.size(IconButtonSize).clip(CircleShape),
            enabled = enabled,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                modifier = Modifier.size(IconSize),
                tint = tint,
            )
        }
    }
}

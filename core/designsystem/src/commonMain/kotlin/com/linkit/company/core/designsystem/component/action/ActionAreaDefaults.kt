package com.linkit.company.core.designsystem.component.action

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.linkit.company.core.designsystem.theme.LinkItTheme

/**
 * [LinkItActionArea] 의 기본 값을 제공한다. Material3 의 컴포넌트 `Defaults` 와 동일한 역할.
 *
 * Figma "Pingo v3.0.3 - Action / Action Area" 의 스펙을 따른다.
 */
object ActionAreaDefaults {

    /** 액션 버튼 묶음의 좌우/상하 패딩. */
    val ActionsPadding: PaddingValues = PaddingValues(horizontal = 20.dp, vertical = 16.dp)

    /** 여러 액션 버튼을 세로로 쌓을 때의 간격. */
    val ActionSpacing: Dp = 8.dp

    /** 설명(Extra) 영역의 패딩. 상단·좌우 20dp, 하단 4dp. */
    val DescriptionPadding: PaddingValues =
        PaddingValues(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 4.dp)

    /** 설명 영역의 헤딩과 캡션 사이 간격. */
    val HeadingSpacing: Dp = 8.dp

    /** 헤딩 텍스트와 아이콘 사이 간격. */
    val HeadingIconSpacing: Dp = 4.dp

    /** 헤딩 아이콘 크기. */
    val HeadingIconSize: Dp = 20.dp

    /** iOS 홈 인디케이터 등을 고려한 하단 안전 영역 높이. */
    val BottomSafeAreaHeight: Dp = 34.dp

    /** Extra 영역 상단 구분선 색. */
    val dividerColor: Color
        @Composable
        @ReadOnlyComposable
        get() = LinkItTheme.color.semantic.line.normal.neutral

    /** 액션 영역 배경색. */
    val containerColor: Color
        @Composable
        @ReadOnlyComposable
        get() = LinkItTheme.color.semantic.background.normal.normal

    val headingColor: Color
        @Composable
        @ReadOnlyComposable
        get() = LinkItTheme.color.semantic.label.strong

    val descriptionColor: Color
        @Composable
        @ReadOnlyComposable
        get() = LinkItTheme.color.semantic.label.alternative

    val headingTextStyle: TextStyle
        @Composable
        @ReadOnlyComposable
        get() = LinkItTheme.typography.body1NormalBold

    val descriptionTextStyle: TextStyle
        @Composable
        @ReadOnlyComposable
        get() = LinkItTheme.typography.label2Medium
}

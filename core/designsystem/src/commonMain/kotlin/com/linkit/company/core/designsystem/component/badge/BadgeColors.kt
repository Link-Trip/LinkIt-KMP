package com.linkit.company.core.designsystem.component.badge

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * [LinkItBadge] 에 적용되는 색상 묶음. [BadgeDefaults.colors] 로 생성한다.
 *
 * @property containerColor 배경 채움 색. 채움이 없으면 [Color.Transparent].
 * @property contentColor 텍스트 색.
 * @property borderColor 외곽선 색. 외곽선이 없으면 [Color.Unspecified].
 */
@Immutable
data class BadgeColors(
    val containerColor: Color,
    val contentColor: Color,
    val borderColor: Color,
)

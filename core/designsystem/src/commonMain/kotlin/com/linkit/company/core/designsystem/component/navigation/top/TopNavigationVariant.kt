package com.linkit.company.core.designsystem.component.navigation

/**
 * [LinkItTopNavigation]의 표시 형태.
 *
 * - [Normal]: 제목을 가운데 정렬한다. (Headline 2 / 16sp)
 * - [Extended]: Leading 옆에 제목을 왼쪽 정렬하고 크게 강조한다. (Heading 2 / 20sp)
 * - [Reduced]: Leading 옆에 제목을 왼쪽 정렬하되 작게 표시한다. (Label 1 / 14sp)
 * - [Floating]: 제목 없이 Leading/Trailing 액션만 표시한다.
 */
enum class TopNavigationVariant {
    Normal,
    Extended,
    Reduced,
    Floating,
}

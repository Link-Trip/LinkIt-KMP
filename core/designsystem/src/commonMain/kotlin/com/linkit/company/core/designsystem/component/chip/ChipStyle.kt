package com.linkit.company.core.designsystem.component.chip

/**
 * [LinkItChip] 의 시각 스타일.
 *
 * - [Solid] : 면을 옅게 채운 칩.
 * - [Outlined] : 외곽선만 가진 칩.
 */
enum class ChipVariant {
    Solid,
    Outlined,
}

/**
 * [LinkItChip] 의 크기 규격. 텍스트 스타일·패딩·라운드가 함께 바뀐다.
 *
 * - [XSmall] : 24dp 높이 / Caption 1 (12sp)
 * - [Small] : 32dp 높이 / Label 1 (14sp)
 * - [Medium] : 36dp 높이 / Body 2 (15sp)
 * - [Large] : 40dp 높이 / Body 2 (15sp)
 */
enum class ChipSize {
    XSmall,
    Small,
    Medium,
    Large,
}

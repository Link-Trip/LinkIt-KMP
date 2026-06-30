package com.linkit.company.core.designsystem.component.button

/**
 * [LinkItButton] 의 시각 스타일.
 *
 * - [Solid] : 면을 채운 버튼. 가장 높은 시각 위계를 가진다.
 * - [Outlined] : 외곽선만 가진 버튼. 보조 행동에 사용한다.
 */
enum class ButtonVariant {
    Solid,
    Outlined,
}

/**
 * [LinkItButton] 의 색 역할.
 *
 * - [Primary] : 핵심 행동(CTA). 강조 색과 Bold 텍스트를 사용한다.
 * - [Assistive] : 보조 행동. 중립 색과 Regular 텍스트를 사용한다.
 */
enum class ButtonColor {
    Primary,
    Assistive,
}

/**
 * [LinkItButton] 의 크기 규격. 텍스트 스타일·패딩·라운드가 함께 바뀐다.
 *
 * - [Large] : 48dp 높이 / Body 2 (15sp)
 * - [Medium] : 40dp 높이 / Label 1 (14sp)
 * - [Small] : 32dp 높이 / Caption 1 (12sp)
 */
enum class ButtonSize {
    Large,
    Medium,
    Small,
}

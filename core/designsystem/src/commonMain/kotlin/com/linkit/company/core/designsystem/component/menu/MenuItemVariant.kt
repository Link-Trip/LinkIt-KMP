package com.linkit.company.core.designsystem.component.menu

/**
 * [LinkItMenuItem] 의 시각적 스타일.
 *
 * - [Normal]: 텍스트만 표시한다. 선택([LinkItMenuItem] 의 `selected`) 시 강조 색으로 바뀐다.
 * - [Checkbox]: 텍스트 앞에 체크박스를 둔다. 선택 시 체크박스가 채워진다.
 */
enum class MenuItemVariant {
    Normal,
    Checkbox,
}

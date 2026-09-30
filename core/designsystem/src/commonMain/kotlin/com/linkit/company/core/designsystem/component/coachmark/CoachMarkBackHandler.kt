package com.linkit.company.core.designsystem.component.coachmark

import androidx.compose.runtime.Composable

/**
 * 코치마크가 떠 있는 동안 시스템 뒤로가기를 무시한다.
 *
 * Android는 `BackHandler(enabled = true) {}` 로 이벤트를 삼키고, iOS는 시스템 뒤로가기가 없어 no-op 이다.
 */
@Composable
internal expect fun CoachMarkBackHandler()

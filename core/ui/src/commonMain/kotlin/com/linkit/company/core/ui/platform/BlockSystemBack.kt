package com.linkit.company.core.ui.platform

import androidx.compose.runtime.Composable

/**
 * 화면이 떠 있는 동안 시스템 뒤로가기를 무시한다(예: 분석 완료 화면, FR-027).
 *
 * Android는 `BackHandler(enabled) {}` 로 이벤트를 삼키고, iOS는 시스템 뒤로가기가 없어 no-op 이다.
 */
@Composable
expect fun BlockSystemBack(enabled: Boolean = true)

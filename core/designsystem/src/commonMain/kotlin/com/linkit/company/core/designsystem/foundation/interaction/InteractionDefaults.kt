package com.linkit.company.core.designsystem.foundation.interaction

import androidx.compose.foundation.IndicationNodeFactory
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import com.linkit.company.core.designsystem.theme.LinkItTheme

/**
 * [LinkItInteractionIndication] 의 기본값과 생성을 모은 헬퍼.
 *
 * `clickable`/`selectable` 의 `indication` 인자에 [indication] 을 넘겨 상태 레이어를 입힌다.
 */
object InteractionDefaults {

    /** 오버레이 기본 색. Figma 기본값인 Label/Normal 을 따른다. */
    val color: Color
        @Composable
        @ReadOnlyComposable
        get() = LinkItTheme.color.semantic.label.normal

    /**
     * 상태 레이어 indication 을 생성해 [color] 키로 기억한다.
     *
     * @param color 오버레이 색. 기본값은 [InteractionDefaults.color].
     */
    @Composable
    fun indication(color: Color = this.color): IndicationNodeFactory =
        remember(color) { LinkItInteractionIndication(color) }
}

package com.linkit.company.core.designsystem.foundation.interaction

import androidx.compose.foundation.IndicationNodeFactory
import androidx.compose.foundation.interaction.FocusInteraction
import androidx.compose.foundation.interaction.HoverInteraction
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.node.DelegatableNode
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.invalidateDraw
import com.linkit.company.core.designsystem.foundation.color.token.PaletteTokens
import kotlinx.coroutines.launch

/**
 * 요소 최상단에 반투명 상태 레이어를 덮어 상호작용을 표현하는 [androidx.compose.foundation.Indication].
 *
 * Figma "Pingo v3.0.3 - Decorate / Interaction" 을 구현한 것으로, hovered/focused/pressed 상태에
 * 따라 [color] 를 규칙적인 투명도(5%/8%/12%)로 올린다. normal 상태에서는 아무것도 그리지 않는다.
 * 오버레이 크기·라운드는 이 indication 을 적용하는 호스트의 영역과 `clip` 을 그대로 따른다.
 *
 * [androidx.compose.foundation.clickable]·[androidx.compose.foundation.selection.selectable] 의
 * `indication` 인자로 넘겨 사용한다. 기본 색은 [InteractionDefaults.indication] 을 통해 주입한다.
 *
 * @param color 오버레이 색. 동일 색에서 투명도만 상태별로 달라진다.
 */
class LinkItInteractionIndication(
    private val color: Color,
) : IndicationNodeFactory {

    override fun create(interactionSource: InteractionSource): DelegatableNode =
        InteractionNode(interactionSource, color)

    override fun equals(other: Any?): Boolean =
        other is LinkItInteractionIndication && color == other.color

    override fun hashCode(): Int = color.hashCode()

    private class InteractionNode(
        private val interactionSource: InteractionSource,
        private val color: Color,
    ) : Modifier.Node(), DrawModifierNode {

        private var isPressed = false
        private var isFocused = false
        private var isHovered = false

        override fun onAttach() {
            coroutineScope.launch {
                val pressInteractions = mutableListOf<PressInteraction.Press>()
                val focusInteractions = mutableListOf<FocusInteraction.Focus>()
                val hoverInteractions = mutableListOf<HoverInteraction.Enter>()
                interactionSource.interactions.collect { interaction ->
                    when (interaction) {
                        is PressInteraction.Press -> pressInteractions.add(interaction)
                        is PressInteraction.Release -> pressInteractions.remove(interaction.press)
                        is PressInteraction.Cancel -> pressInteractions.remove(interaction.press)
                        is FocusInteraction.Focus -> focusInteractions.add(interaction)
                        is FocusInteraction.Unfocus -> focusInteractions.remove(interaction.focus)
                        is HoverInteraction.Enter -> hoverInteractions.add(interaction)
                        is HoverInteraction.Exit -> hoverInteractions.remove(interaction.enter)
                    }
                    val pressed = pressInteractions.isNotEmpty()
                    val focused = focusInteractions.isNotEmpty()
                    val hovered = hoverInteractions.isNotEmpty()
                    if (pressed != isPressed || focused != isFocused || hovered != isHovered) {
                        isPressed = pressed
                        isFocused = focused
                        isHovered = hovered
                        invalidateDraw()
                    }
                }
            }
        }

        override fun ContentDrawScope.draw() {
            drawContent()
            // 우선순위: pressed > focused > hovered. normal 은 그리지 않는다.
            val alpha = when {
                isPressed -> PaletteTokens.Opacity12
                isFocused -> PaletteTokens.Opacity8
                isHovered -> PaletteTokens.Opacity5
                else -> PaletteTokens.Opacity0
            }
            if (alpha > 0f) {
                drawRect(color = color, alpha = alpha)
            }
        }
    }
}

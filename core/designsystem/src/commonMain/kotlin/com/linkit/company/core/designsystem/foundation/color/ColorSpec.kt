package com.linkit.company.core.designsystem.foundation.color

import androidx.compose.ui.graphics.Color

data class SemanticColorSpec(
    val static: StaticColorSpec,
    val primary: PrimaryColorSpec,
    val secondary: SecondaryColorSpec,
    val label: LabelColorSpec,
    val background: BackgroundColorSpec,
    val interaction: InteractionColorSpec,
    val line: LineColorSpec,
    val fill: FillColorSpec,
    val status: StatusColorSpec,
    val accent: AccentColorSpec,
    val material: MaterialColorSpec,
    val inverse: InverseColorSpec,
)

data class StaticColorSpec(
    val white: Color,
    val black: Color,
)

data class PrimaryColorSpec(
    val light: Color,
    val normal: Color,
    val strong: Color,
    val heavy: Color,
)

data class SecondaryColorSpec(
    val light: Color,
    val normal: Color,
    val strong: Color,
    val heavy: Color,
)

data class LabelColorSpec(
    val normal: Color,
    val strong: Color,
    val neutral: Color,
    val alternative: Color,
    val assistive: Color,
    val disable: Color,
    val faint: Color,
)

data class BackgroundColorSpec(
    val normal: Normal,
    val elevated: Elevated,
) {
    data class Normal(
        val normal: Color,
        val alternative: Color,
    )

    data class Elevated(
        val normal: Color,
        val alternative: Color,
    )
}

data class InteractionColorSpec(
    val inactive: Color,
    val disable: Color,
)

data class LineColorSpec(
    val normal: Normal,
    val solid: Solid,
) {
    data class Normal(
        val normal: Color,
        val neutral: Color,
        val alternative: Color,
        val strong: Color,
    )

    data class Solid(
        val normal: Color,
        val neutral: Color,
        val alternative: Color,
        val strong: Color,
    )
}

data class StatusColorSpec(
    val positive: Color,
    val cautionary: Color,
    val negative: Color,
)

data class AccentColorSpec(
    val background: Background,
    val foreground: Foreground,
) {
    data class Background(
        val redOrange: Color,
        val lime: Color,
        val cyan: Color,
        val lightBlue: Color,
        val navy: Color,
        val violet: Color,
        val purple: Color,
        val pink: Color,
    )

    data class Foreground(
        val red: Color,
        val redOrange: Color,
        val orange: Color,
        val lime: Color,
        val green: Color,
        val cyan: Color,
        val lightBlue: Color,
        val blue: Color,
        val navy: Color,
        val violet: Color,
        val purple: Color,
        val pink: Color,
    )
}

data class InverseColorSpec(
    val primary: Color,
    val background: Color,
    val label: Color,
)

data class FillColorSpec(
    val normal: Color,
    val strong: Color,
    val alternative: Color,
)

data class MaterialColorSpec(
    val dimmer: Color,
)

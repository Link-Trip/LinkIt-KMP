package com.linkit.company.core.designsystem.foundation.color

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.linkit.company.core.designsystem.foundation.color.token.ColorAccessKeyToken
import com.linkit.company.core.designsystem.foundation.color.token.ColorLightTokens
import com.linkit.company.core.designsystem.foundation.color.token.PaletteTokens
import com.linkit.company.core.designsystem.theme.LinkItTheme

@Immutable
class ColorScheme(
    val semantic: SemanticColorSpec,
) {
    val atomic: PaletteTokens get() = PaletteTokens

    fun copy(
        semantic: SemanticColorSpec = this.semantic,
    ): ColorScheme = ColorScheme(
        semantic = semantic,
    )

    override fun toString(): String {
        return "ColorScheme(semantic=$semantic)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ColorScheme) return false

        if (semantic != other.semantic) return false

        return true
    }

    override fun hashCode(): Int {
        return semantic.hashCode()
    }
}

fun lightColorScheme(
    semantic: SemanticColorSpec = ColorLightTokens.semantic,
): ColorScheme = ColorScheme(
    semantic = semantic,
)

fun darkColorScheme(
    semantic: SemanticColorSpec = ColorLightTokens.semantic,
): ColorScheme = ColorScheme(
    semantic = semantic,
)

internal fun ColorScheme.fromToken(value: ColorAccessKeyToken): Color {
    return when (value) {
        ColorAccessKeyToken.SemanticStaticWhite -> this.semantic.static.white
        ColorAccessKeyToken.SemanticStaticBlack -> this.semantic.static.black
        ColorAccessKeyToken.SemanticPrimaryLight -> this.semantic.primary.light
        ColorAccessKeyToken.SemanticPrimaryNormal -> this.semantic.primary.normal
        ColorAccessKeyToken.SemanticPrimaryStrong -> this.semantic.primary.strong
        ColorAccessKeyToken.SemanticPrimaryHeavy -> this.semantic.primary.heavy
        ColorAccessKeyToken.SemanticSecondaryLight -> this.semantic.secondary.light
        ColorAccessKeyToken.SemanticSecondaryNormal -> this.semantic.secondary.normal
        ColorAccessKeyToken.SemanticSecondaryStrong -> this.semantic.secondary.strong
        ColorAccessKeyToken.SemanticSecondaryHeavy -> this.semantic.secondary.heavy
        ColorAccessKeyToken.SemanticLabelNormal -> this.semantic.label.normal
        ColorAccessKeyToken.SemanticLabelStrong -> this.semantic.label.strong
        ColorAccessKeyToken.SemanticLabelNeutral -> this.semantic.label.neutral
        ColorAccessKeyToken.SemanticLabelAlternative -> this.semantic.label.alternative
        ColorAccessKeyToken.SemanticLabelAssistive -> this.semantic.label.assistive
        ColorAccessKeyToken.SemanticLabelDisable -> this.semantic.label.disable
        ColorAccessKeyToken.SemanticLabelFaint -> this.semantic.label.faint
        ColorAccessKeyToken.SemanticBackgroundNormalNormal -> this.semantic.background.normal.normal
        ColorAccessKeyToken.SemanticBackgroundNormalAlternative -> this.semantic.background.normal.alternative
        ColorAccessKeyToken.SemanticBackgroundElevatedNormal -> this.semantic.background.elevated.normal
        ColorAccessKeyToken.SemanticBackgroundElevatedAlternative -> this.semantic.background.elevated.alternative
        ColorAccessKeyToken.SemanticInteractionInactive -> this.semantic.interaction.inactive
        ColorAccessKeyToken.SemanticInteractionDisable -> this.semantic.interaction.disable
        ColorAccessKeyToken.SemanticLineNormalNormal -> this.semantic.line.normal.normal
        ColorAccessKeyToken.SemanticLineNormalNeutral -> this.semantic.line.normal.neutral
        ColorAccessKeyToken.SemanticLineNormalAlternative -> this.semantic.line.normal.alternative
        ColorAccessKeyToken.SemanticLineNormalStrong -> this.semantic.line.normal.strong
        ColorAccessKeyToken.SemanticLineSolidNormal -> this.semantic.line.solid.normal
        ColorAccessKeyToken.SemanticLineSolidNeutral -> this.semantic.line.solid.neutral
        ColorAccessKeyToken.SemanticLineSolidAlternative -> this.semantic.line.solid.alternative
        ColorAccessKeyToken.SemanticLineSolidStrong -> this.semantic.line.solid.strong
        ColorAccessKeyToken.SemanticFillNormal -> this.semantic.fill.normal
        ColorAccessKeyToken.SemanticFillStrong -> this.semantic.fill.strong
        ColorAccessKeyToken.SemanticFillAlternative -> this.semantic.fill.alternative
        ColorAccessKeyToken.SemanticStatusPositive -> this.semantic.status.positive
        ColorAccessKeyToken.SemanticStatusCautionary -> this.semantic.status.cautionary
        ColorAccessKeyToken.SemanticStatusNegative -> this.semantic.status.negative
        ColorAccessKeyToken.SemanticAccentBackgroundRedOrange -> this.semantic.accent.background.redOrange
        ColorAccessKeyToken.SemanticAccentBackgroundLime -> this.semantic.accent.background.lime
        ColorAccessKeyToken.SemanticAccentBackgroundCyan -> this.semantic.accent.background.cyan
        ColorAccessKeyToken.SemanticAccentBackgroundLightBlue -> this.semantic.accent.background.lightBlue
        ColorAccessKeyToken.SemanticAccentBackgroundNavy -> this.semantic.accent.background.navy
        ColorAccessKeyToken.SemanticAccentBackgroundViolet -> this.semantic.accent.background.violet
        ColorAccessKeyToken.SemanticAccentBackgroundPurple -> this.semantic.accent.background.purple
        ColorAccessKeyToken.SemanticAccentBackgroundPink -> this.semantic.accent.background.pink
        ColorAccessKeyToken.SemanticAccentForegroundRed -> this.semantic.accent.foreground.red
        ColorAccessKeyToken.SemanticAccentForegroundRedOrange -> this.semantic.accent.foreground.redOrange
        ColorAccessKeyToken.SemanticAccentForegroundOrange -> this.semantic.accent.foreground.orange
        ColorAccessKeyToken.SemanticAccentForegroundLime -> this.semantic.accent.foreground.lime
        ColorAccessKeyToken.SemanticAccentForegroundGreen -> this.semantic.accent.foreground.green
        ColorAccessKeyToken.SemanticAccentForegroundCyan -> this.semantic.accent.foreground.cyan
        ColorAccessKeyToken.SemanticAccentForegroundLightBlue -> this.semantic.accent.foreground.lightBlue
        ColorAccessKeyToken.SemanticAccentForegroundBlue -> this.semantic.accent.foreground.blue
        ColorAccessKeyToken.SemanticAccentForegroundNavy -> this.semantic.accent.foreground.navy
        ColorAccessKeyToken.SemanticAccentForegroundViolet -> this.semantic.accent.foreground.violet
        ColorAccessKeyToken.SemanticAccentForegroundPurple -> this.semantic.accent.foreground.purple
        ColorAccessKeyToken.SemanticAccentForegroundPink -> this.semantic.accent.foreground.pink
        ColorAccessKeyToken.SemanticMaterialDimmer -> this.semantic.material.dimmer
        ColorAccessKeyToken.SemanticInversePrimary -> this.semantic.inverse.primary
        ColorAccessKeyToken.SemanticInverseBackground -> this.semantic.inverse.background
        ColorAccessKeyToken.SemanticInverseLabel -> this.semantic.inverse.label
    }
}

internal val ColorAccessKeyToken.value: Color
    @Composable
    @ReadOnlyComposable
    get() = LinkItTheme.color.fromToken(this)

internal val LocalColorScheme = staticCompositionLocalOf { lightColorScheme() }

package com.linkit.company.core.designsystem.foundation.color.token

import androidx.compose.ui.graphics.Color
import com.linkit.company.core.designsystem.foundation.color.AccentColorSpec
import com.linkit.company.core.designsystem.foundation.color.BackgroundColorSpec
import com.linkit.company.core.designsystem.foundation.color.FillColorSpec
import com.linkit.company.core.designsystem.foundation.color.InteractionColorSpec
import com.linkit.company.core.designsystem.foundation.color.InverseColorSpec
import com.linkit.company.core.designsystem.foundation.color.LabelColorSpec
import com.linkit.company.core.designsystem.foundation.color.LineColorSpec
import com.linkit.company.core.designsystem.foundation.color.MaterialColorSpec
import com.linkit.company.core.designsystem.foundation.color.PrimaryColorSpec
import com.linkit.company.core.designsystem.foundation.color.SecondaryColorSpec
import com.linkit.company.core.designsystem.foundation.color.SemanticColorSpec
import com.linkit.company.core.designsystem.foundation.color.StaticColorSpec
import com.linkit.company.core.designsystem.foundation.color.StatusColorSpec

internal object ColorLightTokens {
    val semantic: SemanticColorSpec = SemanticColorSpec(
        static = StaticColorSpec(
            white = PaletteTokens.White,
            black = PaletteTokens.Black,
        ),
        primary = PrimaryColorSpec(
            light = PaletteTokens.PaleBlue80,
            normal = PaletteTokens.PaleBlue50,
            strong = PaletteTokens.PaleBlue40,
            heavy = PaletteTokens.PaleBlue30,
        ),
        secondary = SecondaryColorSpec(
            light = PaletteTokens.Lavender80,
            normal = PaletteTokens.Lavender60,
            strong = PaletteTokens.Lavender50,
            heavy = PaletteTokens.Lavender40,
        ),
        label = LabelColorSpec(
            normal = PaletteTokens.CoolNeutral10,
            strong = PaletteTokens.Black,
            neutral = PaletteTokens.CoolNeutral22.copy(alpha = PaletteTokens.Opacity88),
            alternative = PaletteTokens.CoolNeutral25.copy(alpha = PaletteTokens.Opacity61),
            assistive = PaletteTokens.CoolNeutral25.copy(alpha = PaletteTokens.Opacity28),
            disable = PaletteTokens.CoolNeutral25.copy(alpha = PaletteTokens.Opacity16),
            faint = PaletteTokens.CoolNeutral25.copy(alpha = PaletteTokens.Opacity8),
        ),
        background = BackgroundColorSpec(
            normal = BackgroundColorSpec.Normal(
                normal = PaletteTokens.White,
                alternative = PaletteTokens.CoolNeutral99,
            ),
            elevated = BackgroundColorSpec.Elevated(
                normal = PaletteTokens.White,
                alternative = PaletteTokens.CoolNeutral99,
            ),
        ),
        interaction = InteractionColorSpec(
            inactive = PaletteTokens.CoolNeutral70,
            disable = PaletteTokens.CoolNeutral98,
        ),
        line = LineColorSpec(
            normal = LineColorSpec.Normal(
                normal = PaletteTokens.CoolNeutral50.copy(alpha = PaletteTokens.Opacity22),
                neutral = PaletteTokens.CoolNeutral50.copy(alpha = PaletteTokens.Opacity16),
                alternative = PaletteTokens.CoolNeutral50.copy(alpha = PaletteTokens.Opacity8),
                strong = PaletteTokens.CoolNeutral50.copy(alpha = PaletteTokens.Opacity52),
            ),
            solid = LineColorSpec.Solid(
                normal = PaletteTokens.CoolNeutral96,
                neutral = PaletteTokens.CoolNeutral97,
                alternative = PaletteTokens.CoolNeutral98,
                strong = PaletteTokens.CoolNeutral80,
            ),
        ),
        fill = FillColorSpec(
            normal = PaletteTokens.CoolNeutral50.copy(alpha = PaletteTokens.Opacity8),
            strong = PaletteTokens.CoolNeutral50.copy(alpha = PaletteTokens.Opacity16),
            alternative = PaletteTokens.CoolNeutral50.copy(alpha = PaletteTokens.Opacity5),
        ),
        status = StatusColorSpec(
            positive = PaletteTokens.Green50,
            cautionary = PaletteTokens.Orange50,
            negative = PaletteTokens.Red50,
        ),
        accent = AccentColorSpec(
            background = AccentColorSpec.Background(
                redOrange = PaletteTokens.RedOrange50,
                lime = PaletteTokens.Lime50,
                cyan = PaletteTokens.Cyan50,
                lightBlue = PaletteTokens.LightBlue50,
                navy = Color.Unspecified,
                violet = PaletteTokens.Violet50,
                purple = PaletteTokens.Purple50,
                pink = PaletteTokens.Pink50,
            ),
            foreground = AccentColorSpec.Foreground(
                red = PaletteTokens.Red40,
                redOrange = PaletteTokens.RedOrange48,
                orange = PaletteTokens.Orange39,
                lime = PaletteTokens.Lime37,
                green = PaletteTokens.Green40,
                cyan = PaletteTokens.Cyan40,
                lightBlue = PaletteTokens.LightBlue40,
                blue = PaletteTokens.Blue45,
                navy = Color.Unspecified,
                violet = PaletteTokens.Violet45,
                purple = PaletteTokens.Purple40,
                pink = PaletteTokens.Pink46,
            ),
        ),
        material = MaterialColorSpec(
            dimmer = PaletteTokens.CoolNeutral10.copy(alpha = PaletteTokens.Opacity52),
        ),
        inverse = InverseColorSpec(
            primary = PaletteTokens.Blue60,
            background = PaletteTokens.CoolNeutral15,
            label = PaletteTokens.CoolNeutral99,
        ),
    )
}

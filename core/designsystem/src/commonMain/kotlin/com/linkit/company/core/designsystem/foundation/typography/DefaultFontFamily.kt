package com.linkit.company.core.designsystem.foundation.typography

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import linkitcompany.core.designsystem.generated.resources.DesignRes
import linkitcompany.core.designsystem.generated.resources.nanum_400
import linkitcompany.core.designsystem.generated.resources.nanum_500
import linkitcompany.core.designsystem.generated.resources.nanum_700
import org.jetbrains.compose.resources.Font

@Composable
internal fun rememberDefaultFontFamily(): FontFamily {
    val defaultFontFamily: FontFamily = FontFamily(
        Font(DesignRes.font.nanum_700, FontWeight.W700),
        Font(DesignRes.font.nanum_500, FontWeight.W500),
        Font(DesignRes.font.nanum_400, FontWeight.W400),
    )
    return remember { defaultFontFamily }
}

@Composable
internal fun LinkitTypography.provideDefaultFontFamily() {
    val defaultFontFamily = rememberDefaultFontFamily()
    return remember(defaultFontFamily) {
        LinkitTypography(
            headline3Bold = headline3Bold.copy(fontFamily = defaultFontFamily),
            headline3Medium = headline3Medium.copy(fontFamily = defaultFontFamily),
            headline3Normal = headline3Normal.copy(fontFamily = defaultFontFamily),
        )
    }
}
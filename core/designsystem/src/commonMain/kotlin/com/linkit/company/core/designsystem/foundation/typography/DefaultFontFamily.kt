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
internal fun LinkItTypography.provideDefaultFontFamily(): LinkItTypography {
    val defaultFontFamily = rememberDefaultFontFamily()
    return remember(defaultFontFamily) {
        this.copy(
            headline3Bold = headline3Bold.copy(fontFamily = defaultFontFamily),
            headline3Medium = headline3Medium.copy(fontFamily = defaultFontFamily),
            headline3Normal = headline3Normal.copy(fontFamily = defaultFontFamily),
            headline4Bold = headline4Bold.copy(fontFamily = defaultFontFamily),
            headline4Medium = headline4Medium.copy(fontFamily = defaultFontFamily),
            headline4Normal = headline4Normal.copy(fontFamily = defaultFontFamily),
            headline5Bold = headline5Bold.copy(fontFamily = defaultFontFamily),
            headline5Medium = headline5Medium.copy(fontFamily = defaultFontFamily),
            headline5Normal = headline5Normal.copy(fontFamily = defaultFontFamily),
            headline6Bold = headline6Bold.copy(fontFamily = defaultFontFamily),
            headline6Medium = headline6Medium.copy(fontFamily = defaultFontFamily),
            headline6Normal = headline6Normal.copy(fontFamily = defaultFontFamily),
            titleBold = titleBold.copy(fontFamily = defaultFontFamily),
            titleMedium = titleMedium.copy(fontFamily = defaultFontFamily),
            titleNormal = titleNormal.copy(fontFamily = defaultFontFamily),
            subtitle1Bold = subtitle1Bold.copy(fontFamily = defaultFontFamily),
            subtitle1Medium = subtitle1Medium.copy(fontFamily = defaultFontFamily),
            subtitle1Normal = subtitle1Normal.copy(fontFamily = defaultFontFamily),
            subtitle2Bold = subtitle2Bold.copy(fontFamily = defaultFontFamily),
            subtitle2Medium = subtitle2Medium.copy(fontFamily = defaultFontFamily),
            subtitle2Normal = subtitle2Normal.copy(fontFamily = defaultFontFamily),
            base1Bold = base1Bold.copy(fontFamily = defaultFontFamily),
            base1Medium = base1Medium.copy(fontFamily = defaultFontFamily),
            base1Normal = base1Normal.copy(fontFamily = defaultFontFamily),
            base2Bold = base2Bold.copy(fontFamily = defaultFontFamily),
            base2Medium = base2Medium.copy(fontFamily = defaultFontFamily),
            base2Normal = base2Normal.copy(fontFamily = defaultFontFamily),
            caption3Bold = caption3Bold.copy(fontFamily = defaultFontFamily),
            caption3Medium = caption3Medium.copy(fontFamily = defaultFontFamily),
            caption3Normal = caption3Normal.copy(fontFamily = defaultFontFamily),
            caption2Bold = caption2Bold.copy(fontFamily = defaultFontFamily),
            caption2Medium = caption2Medium.copy(fontFamily = defaultFontFamily),
            caption2Normal = caption2Normal.copy(fontFamily = defaultFontFamily),
            caption1Bold = caption1Bold.copy(fontFamily = defaultFontFamily),
            caption1Medium = caption1Medium.copy(fontFamily = defaultFontFamily),
            caption1Normal = caption1Normal.copy(fontFamily = defaultFontFamily),
            sBold = sBold.copy(fontFamily = defaultFontFamily),
            sMedium = sMedium.copy(fontFamily = defaultFontFamily),
            sNormal = sNormal.copy(fontFamily = defaultFontFamily),
            xsBold = xsBold.copy(fontFamily = defaultFontFamily),
            xsMedium = xsMedium.copy(fontFamily = defaultFontFamily),
            xsNormal = xsNormal.copy(fontFamily = defaultFontFamily),
        )
    }
}

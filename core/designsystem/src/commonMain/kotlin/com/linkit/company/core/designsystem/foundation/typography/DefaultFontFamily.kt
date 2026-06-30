package com.linkit.company.core.designsystem.foundation.typography

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import linkitcompany.core.designsystem.generated.resources.DesignRes
import linkitcompany.core.designsystem.generated.resources.wanted_sans_400
import linkitcompany.core.designsystem.generated.resources.wanted_sans_500
import linkitcompany.core.designsystem.generated.resources.wanted_sans_600
import linkitcompany.core.designsystem.generated.resources.wanted_sans_700
import org.jetbrains.compose.resources.Font

@Composable
internal fun rememberDefaultFontFamily(): FontFamily {
    val defaultFontFamily: FontFamily = FontFamily(
        Font(DesignRes.font.wanted_sans_700, FontWeight.W700),
        Font(DesignRes.font.wanted_sans_600, FontWeight.W600),
        Font(DesignRes.font.wanted_sans_500, FontWeight.W500),
        Font(DesignRes.font.wanted_sans_400, FontWeight.W400),
    )
    return remember { defaultFontFamily }
}

@Composable
internal fun LinkItTypography.provideDefaultFontFamily(): LinkItTypography {
    val defaultFontFamily = rememberDefaultFontFamily()
    return remember(defaultFontFamily) {
        this.copy(
            display1Regular = display1Regular.copy(fontFamily = defaultFontFamily),
            display1Medium = display1Medium.copy(fontFamily = defaultFontFamily),
            display1Semibold = display1Semibold.copy(fontFamily = defaultFontFamily),
            display1Bold = display1Bold.copy(fontFamily = defaultFontFamily),
            display2Regular = display2Regular.copy(fontFamily = defaultFontFamily),
            display2Medium = display2Medium.copy(fontFamily = defaultFontFamily),
            display2Semibold = display2Semibold.copy(fontFamily = defaultFontFamily),
            display2Bold = display2Bold.copy(fontFamily = defaultFontFamily),
            title1Regular = title1Regular.copy(fontFamily = defaultFontFamily),
            title1Medium = title1Medium.copy(fontFamily = defaultFontFamily),
            title1Semibold = title1Semibold.copy(fontFamily = defaultFontFamily),
            title1Bold = title1Bold.copy(fontFamily = defaultFontFamily),
            title2Regular = title2Regular.copy(fontFamily = defaultFontFamily),
            title2Medium = title2Medium.copy(fontFamily = defaultFontFamily),
            title2Semibold = title2Semibold.copy(fontFamily = defaultFontFamily),
            title2Bold = title2Bold.copy(fontFamily = defaultFontFamily),
            title3Regular = title3Regular.copy(fontFamily = defaultFontFamily),
            title3Medium = title3Medium.copy(fontFamily = defaultFontFamily),
            title3Semibold = title3Semibold.copy(fontFamily = defaultFontFamily),
            title3Bold = title3Bold.copy(fontFamily = defaultFontFamily),
            heading1Regular = heading1Regular.copy(fontFamily = defaultFontFamily),
            heading1Medium = heading1Medium.copy(fontFamily = defaultFontFamily),
            heading1Semibold = heading1Semibold.copy(fontFamily = defaultFontFamily),
            heading1Bold = heading1Bold.copy(fontFamily = defaultFontFamily),
            heading2Regular = heading2Regular.copy(fontFamily = defaultFontFamily),
            heading2Medium = heading2Medium.copy(fontFamily = defaultFontFamily),
            heading2Semibold = heading2Semibold.copy(fontFamily = defaultFontFamily),
            heading2Bold = heading2Bold.copy(fontFamily = defaultFontFamily),
            headline1Regular = headline1Regular.copy(fontFamily = defaultFontFamily),
            headline1Medium = headline1Medium.copy(fontFamily = defaultFontFamily),
            headline1Semibold = headline1Semibold.copy(fontFamily = defaultFontFamily),
            headline1Bold = headline1Bold.copy(fontFamily = defaultFontFamily),
            headline2Regular = headline2Regular.copy(fontFamily = defaultFontFamily),
            headline2Medium = headline2Medium.copy(fontFamily = defaultFontFamily),
            headline2Semibold = headline2Semibold.copy(fontFamily = defaultFontFamily),
            headline2Bold = headline2Bold.copy(fontFamily = defaultFontFamily),
            body1NormalRegular = body1NormalRegular.copy(fontFamily = defaultFontFamily),
            body1NormalMedium = body1NormalMedium.copy(fontFamily = defaultFontFamily),
            body1NormalSemibold = body1NormalSemibold.copy(fontFamily = defaultFontFamily),
            body1NormalBold = body1NormalBold.copy(fontFamily = defaultFontFamily),
            body1ReadingRegular = body1ReadingRegular.copy(fontFamily = defaultFontFamily),
            body1ReadingMedium = body1ReadingMedium.copy(fontFamily = defaultFontFamily),
            body1ReadingSemibold = body1ReadingSemibold.copy(fontFamily = defaultFontFamily),
            body1ReadingBold = body1ReadingBold.copy(fontFamily = defaultFontFamily),
            body2NormalRegular = body2NormalRegular.copy(fontFamily = defaultFontFamily),
            body2NormalMedium = body2NormalMedium.copy(fontFamily = defaultFontFamily),
            body2NormalSemibold = body2NormalSemibold.copy(fontFamily = defaultFontFamily),
            body2NormalBold = body2NormalBold.copy(fontFamily = defaultFontFamily),
            body2ReadingRegular = body2ReadingRegular.copy(fontFamily = defaultFontFamily),
            body2ReadingMedium = body2ReadingMedium.copy(fontFamily = defaultFontFamily),
            body2ReadingSemibold = body2ReadingSemibold.copy(fontFamily = defaultFontFamily),
            body2ReadingBold = body2ReadingBold.copy(fontFamily = defaultFontFamily),
            label1NormalRegular = label1NormalRegular.copy(fontFamily = defaultFontFamily),
            label1NormalMedium = label1NormalMedium.copy(fontFamily = defaultFontFamily),
            label1NormalSemibold = label1NormalSemibold.copy(fontFamily = defaultFontFamily),
            label1NormalBold = label1NormalBold.copy(fontFamily = defaultFontFamily),
            label1ReadingRegular = label1ReadingRegular.copy(fontFamily = defaultFontFamily),
            label1ReadingMedium = label1ReadingMedium.copy(fontFamily = defaultFontFamily),
            label1ReadingSemibold = label1ReadingSemibold.copy(fontFamily = defaultFontFamily),
            label1ReadingBold = label1ReadingBold.copy(fontFamily = defaultFontFamily),
            label2Regular = label2Regular.copy(fontFamily = defaultFontFamily),
            label2Medium = label2Medium.copy(fontFamily = defaultFontFamily),
            label2Semibold = label2Semibold.copy(fontFamily = defaultFontFamily),
            label2Bold = label2Bold.copy(fontFamily = defaultFontFamily),
            caption1Regular = caption1Regular.copy(fontFamily = defaultFontFamily),
            caption1Medium = caption1Medium.copy(fontFamily = defaultFontFamily),
            caption1Semibold = caption1Semibold.copy(fontFamily = defaultFontFamily),
            caption1Bold = caption1Bold.copy(fontFamily = defaultFontFamily),
            caption2Regular = caption2Regular.copy(fontFamily = defaultFontFamily),
            caption2Medium = caption2Medium.copy(fontFamily = defaultFontFamily),
            caption2Semibold = caption2Semibold.copy(fontFamily = defaultFontFamily),
            caption2Bold = caption2Bold.copy(fontFamily = defaultFontFamily),
            caption3Regular = caption3Regular.copy(fontFamily = defaultFontFamily),
            caption3Medium = caption3Medium.copy(fontFamily = defaultFontFamily),
            caption3Semibold = caption3Semibold.copy(fontFamily = defaultFontFamily),
            caption3Bold = caption3Bold.copy(fontFamily = defaultFontFamily),
        )
    }
}

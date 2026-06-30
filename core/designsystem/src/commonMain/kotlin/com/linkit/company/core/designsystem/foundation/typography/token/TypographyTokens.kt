package com.linkit.company.core.designsystem.foundation.typography.token

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.TextUnit

internal object TypographyTokens {
    // Display 1
    val Display1Regular = typeStyle(
        TypeScaleTokens.RegularWeight,
        TypeScaleTokens.Display1FontSize,
        TypeScaleTokens.Display1LineHeight,
        TypeScaleTokens.Display1LetterSpacing
    )
    val Display1Medium = typeStyle(
        TypeScaleTokens.MediumWeight,
        TypeScaleTokens.Display1FontSize,
        TypeScaleTokens.Display1LineHeight,
        TypeScaleTokens.Display1LetterSpacing
    )
    val Display1Semibold = typeStyle(
        TypeScaleTokens.SemiboldWeight,
        TypeScaleTokens.Display1FontSize,
        TypeScaleTokens.Display1LineHeight,
        TypeScaleTokens.Display1LetterSpacing
    )
    val Display1Bold = typeStyle(
        TypeScaleTokens.BoldWeight,
        TypeScaleTokens.Display1FontSize,
        TypeScaleTokens.Display1LineHeight,
        TypeScaleTokens.Display1LetterSpacing
    )

    // Display 2
    val Display2Regular = typeStyle(
        TypeScaleTokens.RegularWeight,
        TypeScaleTokens.Display2FontSize,
        TypeScaleTokens.Display2LineHeight,
        TypeScaleTokens.Display2LetterSpacing
    )
    val Display2Medium = typeStyle(
        TypeScaleTokens.MediumWeight,
        TypeScaleTokens.Display2FontSize,
        TypeScaleTokens.Display2LineHeight,
        TypeScaleTokens.Display2LetterSpacing
    )
    val Display2Semibold = typeStyle(
        TypeScaleTokens.SemiboldWeight,
        TypeScaleTokens.Display2FontSize,
        TypeScaleTokens.Display2LineHeight,
        TypeScaleTokens.Display2LetterSpacing
    )
    val Display2Bold = typeStyle(
        TypeScaleTokens.BoldWeight,
        TypeScaleTokens.Display2FontSize,
        TypeScaleTokens.Display2LineHeight,
        TypeScaleTokens.Display2LetterSpacing
    )

    // Title 1
    val Title1Regular = typeStyle(
        TypeScaleTokens.RegularWeight,
        TypeScaleTokens.Title1FontSize,
        TypeScaleTokens.Title1LineHeight,
        TypeScaleTokens.Title1LetterSpacing
    )
    val Title1Medium = typeStyle(
        TypeScaleTokens.MediumWeight,
        TypeScaleTokens.Title1FontSize,
        TypeScaleTokens.Title1LineHeight,
        TypeScaleTokens.Title1LetterSpacing
    )
    val Title1Semibold = typeStyle(
        TypeScaleTokens.SemiboldWeight,
        TypeScaleTokens.Title1FontSize,
        TypeScaleTokens.Title1LineHeight,
        TypeScaleTokens.Title1LetterSpacing
    )
    val Title1Bold = typeStyle(
        TypeScaleTokens.BoldWeight,
        TypeScaleTokens.Title1FontSize,
        TypeScaleTokens.Title1LineHeight,
        TypeScaleTokens.Title1LetterSpacing
    )

    // Title 2
    val Title2Regular = typeStyle(
        TypeScaleTokens.RegularWeight,
        TypeScaleTokens.Title2FontSize,
        TypeScaleTokens.Title2LineHeight,
        TypeScaleTokens.Title2LetterSpacing
    )
    val Title2Medium = typeStyle(
        TypeScaleTokens.MediumWeight,
        TypeScaleTokens.Title2FontSize,
        TypeScaleTokens.Title2LineHeight,
        TypeScaleTokens.Title2LetterSpacing
    )
    val Title2Semibold = typeStyle(
        TypeScaleTokens.SemiboldWeight,
        TypeScaleTokens.Title2FontSize,
        TypeScaleTokens.Title2LineHeight,
        TypeScaleTokens.Title2LetterSpacing
    )
    val Title2Bold = typeStyle(
        TypeScaleTokens.BoldWeight,
        TypeScaleTokens.Title2FontSize,
        TypeScaleTokens.Title2LineHeight,
        TypeScaleTokens.Title2LetterSpacing
    )

    // Title 3
    val Title3Regular = typeStyle(
        TypeScaleTokens.RegularWeight,
        TypeScaleTokens.Title3FontSize,
        TypeScaleTokens.Title3LineHeight,
        TypeScaleTokens.Title3LetterSpacing
    )
    val Title3Medium = typeStyle(
        TypeScaleTokens.MediumWeight,
        TypeScaleTokens.Title3FontSize,
        TypeScaleTokens.Title3LineHeight,
        TypeScaleTokens.Title3LetterSpacing
    )
    val Title3Semibold = typeStyle(
        TypeScaleTokens.SemiboldWeight,
        TypeScaleTokens.Title3FontSize,
        TypeScaleTokens.Title3LineHeight,
        TypeScaleTokens.Title3LetterSpacing
    )
    val Title3Bold = typeStyle(
        TypeScaleTokens.BoldWeight,
        TypeScaleTokens.Title3FontSize,
        TypeScaleTokens.Title3LineHeight,
        TypeScaleTokens.Title3LetterSpacing
    )

    // Heading 1
    val Heading1Regular = typeStyle(
        TypeScaleTokens.RegularWeight,
        TypeScaleTokens.Heading1FontSize,
        TypeScaleTokens.Heading1LineHeight,
        TypeScaleTokens.Heading1LetterSpacing
    )
    val Heading1Medium = typeStyle(
        TypeScaleTokens.MediumWeight,
        TypeScaleTokens.Heading1FontSize,
        TypeScaleTokens.Heading1LineHeight,
        TypeScaleTokens.Heading1LetterSpacing
    )
    val Heading1Semibold = typeStyle(
        TypeScaleTokens.SemiboldWeight,
        TypeScaleTokens.Heading1FontSize,
        TypeScaleTokens.Heading1LineHeight,
        TypeScaleTokens.Heading1LetterSpacing
    )
    val Heading1Bold = typeStyle(
        TypeScaleTokens.BoldWeight,
        TypeScaleTokens.Heading1FontSize,
        TypeScaleTokens.Heading1LineHeight,
        TypeScaleTokens.Heading1LetterSpacing
    )

    // Heading 2
    val Heading2Regular = typeStyle(
        TypeScaleTokens.RegularWeight,
        TypeScaleTokens.Heading2FontSize,
        TypeScaleTokens.Heading2LineHeight,
        TypeScaleTokens.Heading2LetterSpacing
    )
    val Heading2Medium = typeStyle(
        TypeScaleTokens.MediumWeight,
        TypeScaleTokens.Heading2FontSize,
        TypeScaleTokens.Heading2LineHeight,
        TypeScaleTokens.Heading2LetterSpacing
    )
    val Heading2Semibold = typeStyle(
        TypeScaleTokens.SemiboldWeight,
        TypeScaleTokens.Heading2FontSize,
        TypeScaleTokens.Heading2LineHeight,
        TypeScaleTokens.Heading2LetterSpacing
    )
    val Heading2Bold = typeStyle(
        TypeScaleTokens.BoldWeight,
        TypeScaleTokens.Heading2FontSize,
        TypeScaleTokens.Heading2LineHeight,
        TypeScaleTokens.Heading2LetterSpacing
    )

    // Headline 1 (letterSpacing 0)
    val Headline1Regular = typeStyle(
        TypeScaleTokens.RegularWeight,
        TypeScaleTokens.Headline1FontSize,
        TypeScaleTokens.Headline1LineHeight
    )
    val Headline1Medium = typeStyle(
        TypeScaleTokens.MediumWeight,
        TypeScaleTokens.Headline1FontSize,
        TypeScaleTokens.Headline1LineHeight
    )
    val Headline1Semibold = typeStyle(
        TypeScaleTokens.SemiboldWeight,
        TypeScaleTokens.Headline1FontSize,
        TypeScaleTokens.Headline1LineHeight
    )
    val Headline1Bold = typeStyle(
        TypeScaleTokens.BoldWeight,
        TypeScaleTokens.Headline1FontSize,
        TypeScaleTokens.Headline1LineHeight
    )

    // Headline 2 (letterSpacing 0)
    val Headline2Regular = typeStyle(
        TypeScaleTokens.RegularWeight,
        TypeScaleTokens.Headline2FontSize,
        TypeScaleTokens.Headline2LineHeight
    )
    val Headline2Medium = typeStyle(
        TypeScaleTokens.MediumWeight,
        TypeScaleTokens.Headline2FontSize,
        TypeScaleTokens.Headline2LineHeight
    )
    val Headline2Semibold = typeStyle(
        TypeScaleTokens.SemiboldWeight,
        TypeScaleTokens.Headline2FontSize,
        TypeScaleTokens.Headline2LineHeight
    )
    val Headline2Bold = typeStyle(
        TypeScaleTokens.BoldWeight,
        TypeScaleTokens.Headline2FontSize,
        TypeScaleTokens.Headline2LineHeight
    )

    // Body 1 - Normal
    val Body1NormalRegular = typeStyle(
        TypeScaleTokens.RegularWeight,
        TypeScaleTokens.Body1NormalFontSize,
        TypeScaleTokens.Body1NormalLineHeight,
        TypeScaleTokens.Body1NormalLetterSpacing
    )
    val Body1NormalMedium = typeStyle(
        TypeScaleTokens.MediumWeight,
        TypeScaleTokens.Body1NormalFontSize,
        TypeScaleTokens.Body1NormalLineHeight,
        TypeScaleTokens.Body1NormalLetterSpacing
    )
    val Body1NormalSemibold = typeStyle(
        TypeScaleTokens.SemiboldWeight,
        TypeScaleTokens.Body1NormalFontSize,
        TypeScaleTokens.Body1NormalLineHeight,
        TypeScaleTokens.Body1NormalLetterSpacing
    )
    val Body1NormalBold = typeStyle(
        TypeScaleTokens.BoldWeight,
        TypeScaleTokens.Body1NormalFontSize,
        TypeScaleTokens.Body1NormalLineHeight,
        TypeScaleTokens.Body1NormalLetterSpacing
    )

    // Body 1 - Reading
    val Body1ReadingRegular = typeStyle(
        TypeScaleTokens.RegularWeight,
        TypeScaleTokens.Body1ReadingFontSize,
        TypeScaleTokens.Body1ReadingLineHeight,
        TypeScaleTokens.Body1ReadingLetterSpacing
    )
    val Body1ReadingMedium = typeStyle(
        TypeScaleTokens.MediumWeight,
        TypeScaleTokens.Body1ReadingFontSize,
        TypeScaleTokens.Body1ReadingLineHeight,
        TypeScaleTokens.Body1ReadingLetterSpacing
    )
    val Body1ReadingSemibold = typeStyle(
        TypeScaleTokens.SemiboldWeight,
        TypeScaleTokens.Body1ReadingFontSize,
        TypeScaleTokens.Body1ReadingLineHeight,
        TypeScaleTokens.Body1ReadingLetterSpacing
    )
    val Body1ReadingBold = typeStyle(
        TypeScaleTokens.BoldWeight,
        TypeScaleTokens.Body1ReadingFontSize,
        TypeScaleTokens.Body1ReadingLineHeight,
        TypeScaleTokens.Body1ReadingLetterSpacing
    )

    // Body 2 - Normal
    val Body2NormalRegular = typeStyle(
        TypeScaleTokens.RegularWeight,
        TypeScaleTokens.Body2NormalFontSize,
        TypeScaleTokens.Body2NormalLineHeight,
        TypeScaleTokens.Body2NormalLetterSpacing
    )
    val Body2NormalMedium = typeStyle(
        TypeScaleTokens.MediumWeight,
        TypeScaleTokens.Body2NormalFontSize,
        TypeScaleTokens.Body2NormalLineHeight,
        TypeScaleTokens.Body2NormalLetterSpacing
    )
    val Body2NormalSemibold = typeStyle(
        TypeScaleTokens.SemiboldWeight,
        TypeScaleTokens.Body2NormalFontSize,
        TypeScaleTokens.Body2NormalLineHeight,
        TypeScaleTokens.Body2NormalLetterSpacing
    )
    val Body2NormalBold = typeStyle(
        TypeScaleTokens.BoldWeight,
        TypeScaleTokens.Body2NormalFontSize,
        TypeScaleTokens.Body2NormalLineHeight,
        TypeScaleTokens.Body2NormalLetterSpacing
    )

    // Body 2 - Reading
    val Body2ReadingRegular = typeStyle(
        TypeScaleTokens.RegularWeight,
        TypeScaleTokens.Body2ReadingFontSize,
        TypeScaleTokens.Body2ReadingLineHeight,
        TypeScaleTokens.Body2ReadingLetterSpacing
    )
    val Body2ReadingMedium = typeStyle(
        TypeScaleTokens.MediumWeight,
        TypeScaleTokens.Body2ReadingFontSize,
        TypeScaleTokens.Body2ReadingLineHeight,
        TypeScaleTokens.Body2ReadingLetterSpacing
    )
    val Body2ReadingSemibold = typeStyle(
        TypeScaleTokens.SemiboldWeight,
        TypeScaleTokens.Body2ReadingFontSize,
        TypeScaleTokens.Body2ReadingLineHeight,
        TypeScaleTokens.Body2ReadingLetterSpacing
    )
    val Body2ReadingBold = typeStyle(
        TypeScaleTokens.BoldWeight,
        TypeScaleTokens.Body2ReadingFontSize,
        TypeScaleTokens.Body2ReadingLineHeight,
        TypeScaleTokens.Body2ReadingLetterSpacing
    )

    // Label 1 - Normal
    val Label1NormalRegular = typeStyle(
        TypeScaleTokens.RegularWeight,
        TypeScaleTokens.Label1NormalFontSize,
        TypeScaleTokens.Label1NormalLineHeight,
        TypeScaleTokens.Label1NormalLetterSpacing
    )
    val Label1NormalMedium = typeStyle(
        TypeScaleTokens.MediumWeight,
        TypeScaleTokens.Label1NormalFontSize,
        TypeScaleTokens.Label1NormalLineHeight,
        TypeScaleTokens.Label1NormalLetterSpacing
    )
    val Label1NormalSemibold = typeStyle(
        TypeScaleTokens.SemiboldWeight,
        TypeScaleTokens.Label1NormalFontSize,
        TypeScaleTokens.Label1NormalLineHeight,
        TypeScaleTokens.Label1NormalLetterSpacing
    )
    val Label1NormalBold = typeStyle(
        TypeScaleTokens.BoldWeight,
        TypeScaleTokens.Label1NormalFontSize,
        TypeScaleTokens.Label1NormalLineHeight,
        TypeScaleTokens.Label1NormalLetterSpacing
    )

    // Label 1 - Reading
    val Label1ReadingRegular = typeStyle(
        TypeScaleTokens.RegularWeight,
        TypeScaleTokens.Label1ReadingFontSize,
        TypeScaleTokens.Label1ReadingLineHeight,
        TypeScaleTokens.Label1ReadingLetterSpacing
    )
    val Label1ReadingMedium = typeStyle(
        TypeScaleTokens.MediumWeight,
        TypeScaleTokens.Label1ReadingFontSize,
        TypeScaleTokens.Label1ReadingLineHeight,
        TypeScaleTokens.Label1ReadingLetterSpacing
    )
    val Label1ReadingSemibold = typeStyle(
        TypeScaleTokens.SemiboldWeight,
        TypeScaleTokens.Label1ReadingFontSize,
        TypeScaleTokens.Label1ReadingLineHeight,
        TypeScaleTokens.Label1ReadingLetterSpacing
    )
    val Label1ReadingBold = typeStyle(
        TypeScaleTokens.BoldWeight,
        TypeScaleTokens.Label1ReadingFontSize,
        TypeScaleTokens.Label1ReadingLineHeight,
        TypeScaleTokens.Label1ReadingLetterSpacing
    )

    // Label 2
    val Label2Regular = typeStyle(
        TypeScaleTokens.RegularWeight,
        TypeScaleTokens.Label2FontSize,
        TypeScaleTokens.Label2LineHeight,
        TypeScaleTokens.Label2LetterSpacing
    )
    val Label2Medium = typeStyle(
        TypeScaleTokens.MediumWeight,
        TypeScaleTokens.Label2FontSize,
        TypeScaleTokens.Label2LineHeight,
        TypeScaleTokens.Label2LetterSpacing
    )
    val Label2Semibold = typeStyle(
        TypeScaleTokens.SemiboldWeight,
        TypeScaleTokens.Label2FontSize,
        TypeScaleTokens.Label2LineHeight,
        TypeScaleTokens.Label2LetterSpacing
    )
    val Label2Bold = typeStyle(
        TypeScaleTokens.BoldWeight,
        TypeScaleTokens.Label2FontSize,
        TypeScaleTokens.Label2LineHeight,
        TypeScaleTokens.Label2LetterSpacing
    )

    // Caption 1
    val Caption1Regular = typeStyle(
        TypeScaleTokens.RegularWeight,
        TypeScaleTokens.Caption1FontSize,
        TypeScaleTokens.Caption1LineHeight,
        TypeScaleTokens.Caption1LetterSpacing
    )
    val Caption1Medium = typeStyle(
        TypeScaleTokens.MediumWeight,
        TypeScaleTokens.Caption1FontSize,
        TypeScaleTokens.Caption1LineHeight,
        TypeScaleTokens.Caption1LetterSpacing
    )
    val Caption1Semibold = typeStyle(
        TypeScaleTokens.SemiboldWeight,
        TypeScaleTokens.Caption1FontSize,
        TypeScaleTokens.Caption1LineHeight,
        TypeScaleTokens.Caption1LetterSpacing
    )
    val Caption1Bold = typeStyle(
        TypeScaleTokens.BoldWeight,
        TypeScaleTokens.Caption1FontSize,
        TypeScaleTokens.Caption1LineHeight,
        TypeScaleTokens.Caption1LetterSpacing
    )

    // Caption 2
    val Caption2Regular = typeStyle(
        TypeScaleTokens.RegularWeight,
        TypeScaleTokens.Caption2FontSize,
        TypeScaleTokens.Caption2LineHeight,
        TypeScaleTokens.Caption2LetterSpacing
    )
    val Caption2Medium = typeStyle(
        TypeScaleTokens.MediumWeight,
        TypeScaleTokens.Caption2FontSize,
        TypeScaleTokens.Caption2LineHeight,
        TypeScaleTokens.Caption2LetterSpacing
    )
    val Caption2Semibold = typeStyle(
        TypeScaleTokens.SemiboldWeight,
        TypeScaleTokens.Caption2FontSize,
        TypeScaleTokens.Caption2LineHeight,
        TypeScaleTokens.Caption2LetterSpacing
    )
    val Caption2Bold = typeStyle(
        TypeScaleTokens.BoldWeight,
        TypeScaleTokens.Caption2FontSize,
        TypeScaleTokens.Caption2LineHeight,
        TypeScaleTokens.Caption2LetterSpacing
    )

    // Caption 3
    val Caption3Regular = typeStyle(
        TypeScaleTokens.RegularWeight,
        TypeScaleTokens.Caption3FontSize,
        TypeScaleTokens.Caption3LineHeight,
        TypeScaleTokens.Caption3LetterSpacing
    )
    val Caption3Medium = typeStyle(
        TypeScaleTokens.MediumWeight,
        TypeScaleTokens.Caption3FontSize,
        TypeScaleTokens.Caption3LineHeight,
        TypeScaleTokens.Caption3LetterSpacing
    )
    val Caption3Semibold = typeStyle(
        TypeScaleTokens.SemiboldWeight,
        TypeScaleTokens.Caption3FontSize,
        TypeScaleTokens.Caption3LineHeight,
        TypeScaleTokens.Caption3LetterSpacing
    )
    val Caption3Bold = typeStyle(
        TypeScaleTokens.BoldWeight,
        TypeScaleTokens.Caption3FontSize,
        TypeScaleTokens.Caption3LineHeight,
        TypeScaleTokens.Caption3LetterSpacing
    )
}

private fun typeStyle(
    fontWeight: FontWeight,
    fontSize: TextUnit,
    lineHeight: TextUnit,
    letterSpacing: TextUnit = TextUnit.Unspecified,
): TextStyle = DefaultTextStyle.copy(
    fontWeight = fontWeight,
    fontSize = fontSize,
    lineHeight = lineHeight,
    letterSpacing = letterSpacing,
)

internal val DefaultLineHeightStyle: LineHeightStyle = LineHeightStyle(
    alignment = LineHeightStyle.Alignment.Center,
    trim = LineHeightStyle.Trim.None,
)

internal val DefaultTextStyle: TextStyle = TextStyle(
    lineHeightStyle = DefaultLineHeightStyle,
)

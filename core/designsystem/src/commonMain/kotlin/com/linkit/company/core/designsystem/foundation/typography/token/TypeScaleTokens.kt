package com.linkit.company.core.designsystem.foundation.typography.token

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

internal object TypeScaleTokens {
    // 공통 Font weight
    val RegularWeight = FontWeight.W400
    val MediumWeight = FontWeight.W500
    val SemiboldWeight = FontWeight.W600
    val BoldWeight = FontWeight.W700

    // Display 1
    val Display1FontSize = 48.sp
    val Display1LineHeight = 72.sp
    val Display1LetterSpacing = (-0.5).sp

    // Display 2
    val Display2FontSize = 40.sp
    val Display2LineHeight = 52.sp
    val Display2LetterSpacing = (-0.5).sp

    // Title 1
    val Title1FontSize = 36.sp
    val Title1LineHeight = 48.sp
    val Title1LetterSpacing = (-0.5).sp

    // Title 2
    val Title2FontSize = 28.sp
    val Title2LineHeight = 38.sp
    val Title2LetterSpacing = (-0.25).sp

    // Title 3
    val Title3FontSize = 24.sp
    val Title3LineHeight = 32.sp
    val Title3LetterSpacing = (-0.25).sp

    // Heading 1 (Figma의 깨진 {Font size.800} → 22sp 확정)
    val Heading1FontSize = 22.sp
    val Heading1LineHeight = 30.sp
    val Heading1LetterSpacing = (-0.25).sp

    // Heading 2
    val Heading2FontSize = 20.sp
    val Heading2LineHeight = 28.sp
    val Heading2LetterSpacing = (-0.1).sp

    // Headline 1 (letterSpacing 0 → 미지정)
    val Headline1FontSize = 18.sp
    val Headline1LineHeight = 26.sp

    // Headline 2 (letterSpacing 0 → 미지정)
    val Headline2FontSize = 16.sp
    val Headline2LineHeight = 24.sp

    // Body 1 - Normal
    val Body1NormalFontSize = 16.sp
    val Body1NormalLineHeight = 24.sp
    val Body1NormalLetterSpacing = 0.1.sp

    // Body 1 - Reading
    val Body1ReadingFontSize = 16.sp
    val Body1ReadingLineHeight = 26.sp
    val Body1ReadingLetterSpacing = 0.1.sp

    // Body 2 - Normal
    val Body2NormalFontSize = 15.sp
    val Body2NormalLineHeight = 22.sp
    val Body2NormalLetterSpacing = 0.1.sp

    // Body 2 - Reading
    val Body2ReadingFontSize = 15.sp
    val Body2ReadingLineHeight = 24.sp
    val Body2ReadingLetterSpacing = 0.1.sp

    // Label 1 - Normal
    val Label1NormalFontSize = 14.sp
    val Label1NormalLineHeight = 20.sp
    val Label1NormalLetterSpacing = 0.1.sp

    // Label 1 - Reading
    val Label1ReadingFontSize = 14.sp
    val Label1ReadingLineHeight = 22.sp
    val Label1ReadingLetterSpacing = 0.1.sp

    // Label 2
    val Label2FontSize = 13.sp
    val Label2LineHeight = 18.sp
    val Label2LetterSpacing = 0.25.sp

    // Caption 1
    val Caption1FontSize = 12.sp
    val Caption1LineHeight = 16.sp
    val Caption1LetterSpacing = 0.5.sp

    // Caption 2
    val Caption2FontSize = 11.sp
    val Caption2LineHeight = 14.sp
    val Caption2LetterSpacing = 0.5.sp

    // Caption 3
    val Caption3FontSize = 10.sp
    val Caption3LineHeight = 14.sp
    val Caption3LetterSpacing = 0.5.sp
}

package com.linkit.company.core.designsystem.legacy.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val LinkItFontFamily = FontFamily.Default

object LinkItTextStyle {
    val caption1 = TextStyle(
        fontFamily = LinkItFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 21.sp,
        letterSpacing = (-0.3).sp,
    )
    val small = TextStyle(
        fontFamily = LinkItFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        lineHeight = 21.sp,
    )
    val base2 = TextStyle(
        fontFamily = LinkItFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
        lineHeight = 24.sp,
    )
    val xs = TextStyle(
        fontFamily = LinkItFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        lineHeight = 14.sp,
        letterSpacing = (-0.1).sp,
    )
    val heading = TextStyle(
        fontFamily = LinkItFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
    )
    val body = TextStyle(
        fontFamily = LinkItFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 19.5.sp,
    )
    val sectionTitle = TextStyle(
        fontFamily = LinkItFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    )
    val tagText = TextStyle(
        fontFamily = LinkItFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 10.sp,
        lineHeight = 13.sp,
        letterSpacing = 0.5.sp,
    )
    val chipLabel = TextStyle(
        fontFamily = LinkItFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
        lineHeight = 18.2.sp,
    )
    val keywordTag = TextStyle(
        fontFamily = LinkItFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 10.sp,
        lineHeight = 14.sp,
        letterSpacing = (-0.1).sp,
    )
    val metaInfo = TextStyle(
        fontFamily = LinkItFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        lineHeight = 16.2.sp,
        letterSpacing = 0.174.sp,
    )
}

val LinkItTypography = Typography(
    headlineLarge = TextStyle(
        fontFamily = LinkItFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = LinkItFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 26.sp,
    ),
    headlineSmall = LinkItTextStyle.heading,
    titleLarge = TextStyle(
        fontFamily = LinkItFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = (-0.1).sp,
    ),
    titleMedium = LinkItTextStyle.sectionTitle,
    titleSmall = TextStyle(
        fontFamily = LinkItFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
    ),
    bodyLarge = LinkItTextStyle.base2,
    bodyMedium = TextStyle(
        fontFamily = LinkItFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    bodySmall = LinkItTextStyle.body,
    labelLarge = LinkItTextStyle.caption1,
    labelMedium = LinkItTextStyle.xs,
    labelSmall = LinkItTextStyle.small,
)

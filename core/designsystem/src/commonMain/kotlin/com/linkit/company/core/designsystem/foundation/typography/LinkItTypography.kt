package com.linkit.company.core.designsystem.foundation.typography

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import com.linkit.company.core.designsystem.foundation.typography.token.TypographyTokens

internal val LocalTypography = staticCompositionLocalOf { LinkItTypography() }

@Immutable
class LinkItTypography internal constructor(
    val headline3Bold: TextStyle,
    val headline3Medium: TextStyle,
    val headline3Normal: TextStyle,
    val headline4Bold: TextStyle,
    val headline4Medium: TextStyle,
    val headline4Normal: TextStyle,
    val headline5Bold: TextStyle,
    val headline5Medium: TextStyle,
    val headline5Normal: TextStyle,
    val headline6Bold: TextStyle,
    val headline6Medium: TextStyle,
    val headline6Normal: TextStyle,
    val titleBold: TextStyle,
    val titleMedium: TextStyle,
    val titleNormal: TextStyle,
    val subtitle1Bold: TextStyle,
    val subtitle1Medium: TextStyle,
    val subtitle1Normal: TextStyle,
    val subtitle2Bold: TextStyle,
    val subtitle2Medium: TextStyle,
    val subtitle2Normal: TextStyle,
    val base1Bold: TextStyle,
    val base1Medium: TextStyle,
    val base1Normal: TextStyle,
    val base2Bold: TextStyle,
    val base2Medium: TextStyle,
    val base2Normal: TextStyle,
    val caption3Bold: TextStyle,
    val caption3Medium: TextStyle,
    val caption3Normal: TextStyle,
    val caption2Bold: TextStyle,
    val caption2Medium: TextStyle,
    val caption2Normal: TextStyle,
    val caption1Bold: TextStyle,
    val caption1Medium: TextStyle,
    val caption1Normal: TextStyle,
    val sBold: TextStyle,
    val sMedium: TextStyle,
    val sNormal: TextStyle,
    val xsBold: TextStyle,
    val xsMedium: TextStyle,
    val xsNormal: TextStyle,
) {
    constructor(
        defaultColor: Color = Color.Black,
        headline3Bold: TextStyle = TypographyTokens.Headline3Bold.copy(color = defaultColor),
        headline3Medium: TextStyle = TypographyTokens.Headline3Medium.copy(color = defaultColor),
        headline3Normal: TextStyle = TypographyTokens.Headline3Normal.copy(color = defaultColor),
        headline4Bold: TextStyle = TypographyTokens.Headline4Bold.copy(color = defaultColor),
        headline4Medium: TextStyle = TypographyTokens.Headline4Medium.copy(color = defaultColor),
        headline4Normal: TextStyle = TypographyTokens.Headline4Normal.copy(color = defaultColor),
        headline5Bold: TextStyle = TypographyTokens.Headline5Bold.copy(color = defaultColor),
        headline5Medium: TextStyle = TypographyTokens.Headline5Medium.copy(color = defaultColor),
        headline5Normal: TextStyle = TypographyTokens.Headline5Normal.copy(color = defaultColor),
        headline6Bold: TextStyle = TypographyTokens.Headline6Bold.copy(color = defaultColor),
        headline6Medium: TextStyle = TypographyTokens.Headline6Medium.copy(color = defaultColor),
        headline6Normal: TextStyle = TypographyTokens.Headline6Normal.copy(color = defaultColor),
        titleBold: TextStyle = TypographyTokens.TitleBold.copy(color = defaultColor),
        titleMedium: TextStyle = TypographyTokens.TitleMedium.copy(color = defaultColor),
        titleNormal: TextStyle = TypographyTokens.TitleNormal.copy(color = defaultColor),
        subtitle1Bold: TextStyle = TypographyTokens.Subtitle1Bold.copy(color = defaultColor),
        subtitle1Medium: TextStyle = TypographyTokens.Subtitle1Medium.copy(color = defaultColor),
        subtitle1Normal: TextStyle = TypographyTokens.Subtitle1Normal.copy(color = defaultColor),
        subtitle2Bold: TextStyle = TypographyTokens.Subtitle2Bold.copy(color = defaultColor),
        subtitle2Medium: TextStyle = TypographyTokens.Subtitle2Medium.copy(color = defaultColor),
        subtitle2Normal: TextStyle = TypographyTokens.Subtitle2Normal.copy(color = defaultColor),
        base1Bold: TextStyle = TypographyTokens.Base1Bold.copy(color = defaultColor),
        base1Medium: TextStyle = TypographyTokens.Base1Medium.copy(color = defaultColor),
        base1Normal: TextStyle = TypographyTokens.Base1Normal.copy(color = defaultColor),
        base2Bold: TextStyle = TypographyTokens.Base2Bold.copy(color = defaultColor),
        base2Medium: TextStyle = TypographyTokens.Base2Medium.copy(color = defaultColor),
        base2Normal: TextStyle = TypographyTokens.Base2Normal.copy(color = defaultColor),
        caption3Bold: TextStyle = TypographyTokens.Caption3Bold.copy(color = defaultColor),
        caption3Medium: TextStyle = TypographyTokens.Caption3Medium.copy(color = defaultColor),
        caption3Normal: TextStyle = TypographyTokens.Caption3Normal.copy(color = defaultColor),
        caption2Bold: TextStyle = TypographyTokens.Caption2Bold.copy(color = defaultColor),
        caption2Medium: TextStyle = TypographyTokens.Caption2Medium.copy(color = defaultColor),
        caption2Normal: TextStyle = TypographyTokens.Caption2Normal.copy(color = defaultColor),
        caption1Bold: TextStyle = TypographyTokens.Caption1Bold.copy(color = defaultColor),
        caption1Medium: TextStyle = TypographyTokens.Caption1Medium.copy(color = defaultColor),
        caption1Normal: TextStyle = TypographyTokens.Caption1Normal.copy(color = defaultColor),
        sBold: TextStyle = TypographyTokens.SBold.copy(color = defaultColor),
        sMedium: TextStyle = TypographyTokens.SMedium.copy(color = defaultColor),
        sNormal: TextStyle = TypographyTokens.SNormal.copy(color = defaultColor),
        xsBold: TextStyle = TypographyTokens.XSBold.copy(color = defaultColor),
        xsMedium: TextStyle = TypographyTokens.XSMedium.copy(color = defaultColor),
        xsNormal: TextStyle = TypographyTokens.XSNormal.copy(color = defaultColor),
    ) : this(
        headline3Bold = headline3Bold,
        headline3Medium = headline3Medium,
        headline3Normal = headline3Normal,
        headline4Bold = headline4Bold,
        headline4Medium = headline4Medium,
        headline4Normal = headline4Normal,
        headline5Bold = headline5Bold,
        headline5Medium = headline5Medium,
        headline5Normal = headline5Normal,
        headline6Bold = headline6Bold,
        headline6Medium = headline6Medium,
        headline6Normal = headline6Normal,
        titleBold = titleBold,
        titleMedium = titleMedium,
        titleNormal = titleNormal,
        subtitle1Bold = subtitle1Bold,
        subtitle1Medium = subtitle1Medium,
        subtitle1Normal = subtitle1Normal,
        subtitle2Bold = subtitle2Bold,
        subtitle2Medium = subtitle2Medium,
        subtitle2Normal = subtitle2Normal,
        base1Bold = base1Bold,
        base1Medium = base1Medium,
        base1Normal = base1Normal,
        base2Bold = base2Bold,
        base2Medium = base2Medium,
        base2Normal = base2Normal,
        caption3Bold = caption3Bold,
        caption3Medium = caption3Medium,
        caption3Normal = caption3Normal,
        caption2Bold = caption2Bold,
        caption2Medium = caption2Medium,
        caption2Normal = caption2Normal,
        caption1Bold = caption1Bold,
        caption1Medium = caption1Medium,
        caption1Normal = caption1Normal,
        sBold = sBold,
        sMedium = sMedium,
        sNormal = sNormal,
        xsBold = xsBold,
        xsMedium = xsMedium,
        xsNormal = xsNormal,
    )

    fun copy(
        headline3Bold: TextStyle = this.headline3Bold,
        headline3Medium: TextStyle = this.headline3Medium,
        headline3Normal: TextStyle = this.headline3Normal,
        headline4Bold: TextStyle = this.headline4Bold,
        headline4Medium: TextStyle = this.headline4Medium,
        headline4Normal: TextStyle = this.headline4Normal,
        headline5Bold: TextStyle = this.headline5Bold,
        headline5Medium: TextStyle = this.headline5Medium,
        headline5Normal: TextStyle = this.headline5Normal,
        headline6Bold: TextStyle = this.headline6Bold,
        headline6Medium: TextStyle = this.headline6Medium,
        headline6Normal: TextStyle = this.headline6Normal,
        titleBold: TextStyle = this.titleBold,
        titleMedium: TextStyle = this.titleMedium,
        titleNormal: TextStyle = this.titleNormal,
        subtitle1Bold: TextStyle = this.subtitle1Bold,
        subtitle1Medium: TextStyle = this.subtitle1Medium,
        subtitle1Normal: TextStyle = this.subtitle1Normal,
        subtitle2Bold: TextStyle = this.subtitle2Bold,
        subtitle2Medium: TextStyle = this.subtitle2Medium,
        subtitle2Normal: TextStyle = this.subtitle2Normal,
        base1Bold: TextStyle = this.base1Bold,
        base1Medium: TextStyle = this.base1Medium,
        base1Normal: TextStyle = this.base1Normal,
        base2Bold: TextStyle = this.base2Bold,
        base2Medium: TextStyle = this.base2Medium,
        base2Normal: TextStyle = this.base2Normal,
        caption3Bold: TextStyle = this.caption3Bold,
        caption3Medium: TextStyle = this.caption3Medium,
        caption3Normal: TextStyle = this.caption3Normal,
        caption2Bold: TextStyle = this.caption2Bold,
        caption2Medium: TextStyle = this.caption2Medium,
        caption2Normal: TextStyle = this.caption2Normal,
        caption1Bold: TextStyle = this.caption1Bold,
        caption1Medium: TextStyle = this.caption1Medium,
        caption1Normal: TextStyle = this.caption1Normal,
        sBold: TextStyle = this.sBold,
        sMedium: TextStyle = this.sMedium,
        sNormal: TextStyle = this.sNormal,
        xsBold: TextStyle = this.xsBold,
        xsMedium: TextStyle = this.xsMedium,
        xsNormal: TextStyle = this.xsNormal,
    ): LinkItTypography = LinkItTypography(
        headline3Bold = headline3Bold,
        headline3Medium = headline3Medium,
        headline3Normal = headline3Normal,
        headline4Bold = headline4Bold,
        headline4Medium = headline4Medium,
        headline4Normal = headline4Normal,
        headline5Bold = headline5Bold,
        headline5Medium = headline5Medium,
        headline5Normal = headline5Normal,
        headline6Bold = headline6Bold,
        headline6Medium = headline6Medium,
        headline6Normal = headline6Normal,
        titleBold = titleBold,
        titleMedium = titleMedium,
        titleNormal = titleNormal,
        subtitle1Bold = subtitle1Bold,
        subtitle1Medium = subtitle1Medium,
        subtitle1Normal = subtitle1Normal,
        subtitle2Bold = subtitle2Bold,
        subtitle2Medium = subtitle2Medium,
        subtitle2Normal = subtitle2Normal,
        base1Bold = base1Bold,
        base1Medium = base1Medium,
        base1Normal = base1Normal,
        base2Bold = base2Bold,
        base2Medium = base2Medium,
        base2Normal = base2Normal,
        caption3Bold = caption3Bold,
        caption3Medium = caption3Medium,
        caption3Normal = caption3Normal,
        caption2Bold = caption2Bold,
        caption2Medium = caption2Medium,
        caption2Normal = caption2Normal,
        caption1Bold = caption1Bold,
        caption1Medium = caption1Medium,
        caption1Normal = caption1Normal,
        sBold = sBold,
        sMedium = sMedium,
        sNormal = sNormal,
        xsBold = xsBold,
        xsMedium = xsMedium,
        xsNormal = xsNormal,
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as LinkItTypography

        if (headline3Bold != other.headline3Bold) return false
        if (headline3Medium != other.headline3Medium) return false
        if (headline3Normal != other.headline3Normal) return false
        if (headline4Bold != other.headline4Bold) return false
        if (headline4Medium != other.headline4Medium) return false
        if (headline4Normal != other.headline4Normal) return false
        if (headline5Bold != other.headline5Bold) return false
        if (headline5Medium != other.headline5Medium) return false
        if (headline5Normal != other.headline5Normal) return false
        if (headline6Bold != other.headline6Bold) return false
        if (headline6Medium != other.headline6Medium) return false
        if (headline6Normal != other.headline6Normal) return false
        if (titleBold != other.titleBold) return false
        if (titleMedium != other.titleMedium) return false
        if (titleNormal != other.titleNormal) return false
        if (subtitle1Bold != other.subtitle1Bold) return false
        if (subtitle1Medium != other.subtitle1Medium) return false
        if (subtitle1Normal != other.subtitle1Normal) return false
        if (subtitle2Bold != other.subtitle2Bold) return false
        if (subtitle2Medium != other.subtitle2Medium) return false
        if (subtitle2Normal != other.subtitle2Normal) return false
        if (base1Bold != other.base1Bold) return false
        if (base1Medium != other.base1Medium) return false
        if (base1Normal != other.base1Normal) return false
        if (base2Bold != other.base2Bold) return false
        if (base2Medium != other.base2Medium) return false
        if (base2Normal != other.base2Normal) return false
        if (caption3Bold != other.caption3Bold) return false
        if (caption3Medium != other.caption3Medium) return false
        if (caption3Normal != other.caption3Normal) return false
        if (caption2Bold != other.caption2Bold) return false
        if (caption2Medium != other.caption2Medium) return false
        if (caption2Normal != other.caption2Normal) return false
        if (caption1Bold != other.caption1Bold) return false
        if (caption1Medium != other.caption1Medium) return false
        if (caption1Normal != other.caption1Normal) return false
        if (sBold != other.sBold) return false
        if (sMedium != other.sMedium) return false
        if (sNormal != other.sNormal) return false
        if (xsBold != other.xsBold) return false
        if (xsMedium != other.xsMedium) return false
        if (xsNormal != other.xsNormal) return false

        return true
    }

    override fun hashCode(): Int {
        return arrayOf(
            headline3Bold,
            headline3Medium,
            headline3Normal,
            headline4Bold,
            headline4Medium,
            headline4Normal,
            headline5Bold,
            headline5Medium,
            headline5Normal,
            headline6Bold,
            headline6Medium,
            headline6Normal,
            titleBold,
            titleMedium,
            titleNormal,
            subtitle1Bold,
            subtitle1Medium,
            subtitle1Normal,
            subtitle2Bold,
            subtitle2Medium,
            subtitle2Normal,
            base1Bold,
            base1Medium,
            base1Normal,
            base2Bold,
            base2Medium,
            base2Normal,
            caption3Bold,
            caption3Medium,
            caption3Normal,
            caption2Bold,
            caption2Medium,
            caption2Normal,
            caption1Bold,
            caption1Medium,
            caption1Normal,
            sBold,
            sMedium,
            sNormal,
            xsBold,
            xsMedium,
            xsNormal,
        ).contentHashCode()
    }
}

package com.linkit.company.core.designsystem.foundation.radius

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.staticCompositionLocalOf
import com.linkit.company.core.designsystem.foundation.radius.token.RadiusTokens

data class LinkItRadius(
    val round99: RoundedCornerShape,
    val round50: RoundedCornerShape,
    val round24: RoundedCornerShape,
    val round20: RoundedCornerShape,
    val round16: RoundedCornerShape,
    val round12: RoundedCornerShape,
    val topRound12: RoundedCornerShape,
    val topRound16: RoundedCornerShape,
    val bottomRound12: RoundedCornerShape,
    val round8: RoundedCornerShape,
    val round4: RoundedCornerShape,
)

internal val LocalRadius = staticCompositionLocalOf {
    LinkItRadius(
        round99 = RadiusTokens.Round99,
        round50 = RadiusTokens.Round50,
        round24 = RadiusTokens.Round24,
        round20 = RadiusTokens.Round20,
        round16 = RadiusTokens.Round16,
        round12 = RadiusTokens.Round12,
        topRound12 = RadiusTokens.TopRound12,
        topRound16 = RadiusTokens.TopRound16,
        bottomRound12 = RadiusTokens.BottomRound12,
        round8 = RadiusTokens.Round8,
        round4 = RadiusTokens.Round4,
    )
}

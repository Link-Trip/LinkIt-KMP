package com.linkit.company.core.designsystem.foundation.spacing

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import com.linkit.company.core.designsystem.foundation.spacing.token.SpacingAccessKeyToken

@Immutable
class LinkItSpacing(
    val space0: Dp = SpacingDefaults.Space0,
    val space2: Dp = SpacingDefaults.Space2,
    val space4: Dp = SpacingDefaults.Space4,
    val space8: Dp = SpacingDefaults.Space8,
    val space12: Dp = SpacingDefaults.Space12,
    val space16: Dp = SpacingDefaults.Space16,
    val space20: Dp = SpacingDefaults.Space20,
    val space24: Dp = SpacingDefaults.Space24,
    val space32: Dp = SpacingDefaults.Space32,
    val space40: Dp = SpacingDefaults.Space40,
    val space44: Dp = SpacingDefaults.Space44,
    val space48: Dp = SpacingDefaults.Space48,
    val space52: Dp = SpacingDefaults.Space52,
    val space64: Dp = SpacingDefaults.Space64,
) {
    fun copy(
        space0: Dp = this.space0,
        space2: Dp = this.space2,
        space4: Dp = this.space4,
        space8: Dp = this.space8,
        space12: Dp = this.space12,
        space16: Dp = this.space16,
        space20: Dp = this.space20,
        space24: Dp = this.space24,
        space32: Dp = this.space32,
        space40: Dp = this.space40,
        space44: Dp = this.space44,
        space48: Dp = this.space48,
        space52: Dp = this.space52,
        space64: Dp = this.space64,
    ): LinkItSpacing = LinkItSpacing(
        space0 = space0,
        space2 = space2,
        space4 = space4,
        space8 = space8,
        space12 = space12,
        space16 = space16,
        space20 = space20,
        space24 = space24,
        space32 = space32,
        space40 = space40,
        space44 = space44,
        space48 = space48,
        space52 = space52,
        space64 = space64,
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is LinkItSpacing) return false
        if (space0 != other.space0) return false
        if (space2 != other.space2) return false
        if (space4 != other.space4) return false
        if (space8 != other.space8) return false
        if (space12 != other.space12) return false
        if (space16 != other.space16) return false
        if (space20 != other.space20) return false
        if (space24 != other.space24) return false
        if (space32 != other.space32) return false
        if (space40 != other.space40) return false
        if (space44 != other.space44) return false
        if (space48 != other.space48) return false
        if (space52 != other.space52) return false
        if (space64 != other.space64) return false
        return true
    }

    override fun hashCode(): Int {
        var result = space0.hashCode()
        result = 31 * result + space2.hashCode()
        result = 31 * result + space4.hashCode()
        result = 31 * result + space8.hashCode()
        result = 31 * result + space12.hashCode()
        result = 31 * result + space16.hashCode()
        result = 31 * result + space20.hashCode()
        result = 31 * result + space24.hashCode()
        result = 31 * result + space32.hashCode()
        result = 31 * result + space40.hashCode()
        result = 31 * result + space44.hashCode()
        result = 31 * result + space48.hashCode()
        result = 31 * result + space52.hashCode()
        result = 31 * result + space64.hashCode()
        return result
    }
}

internal fun LinkItSpacing.fromToken(value: SpacingAccessKeyToken): Dp {
    return when (value) {
        SpacingAccessKeyToken.Space0 -> space0
        SpacingAccessKeyToken.Space2 -> space2
        SpacingAccessKeyToken.Space4 -> space4
        SpacingAccessKeyToken.Space8 -> space8
        SpacingAccessKeyToken.Space12 -> space12
        SpacingAccessKeyToken.Space16 -> space16
        SpacingAccessKeyToken.Space20 -> space20
        SpacingAccessKeyToken.Space24 -> space24
        SpacingAccessKeyToken.Space32 -> space32
        SpacingAccessKeyToken.Space40 -> space40
        SpacingAccessKeyToken.Space44 -> space44
        SpacingAccessKeyToken.Space48 -> space48
        SpacingAccessKeyToken.Space52 -> space52
        SpacingAccessKeyToken.Space64 -> space64
    }
}

internal val LocalSpacing = staticCompositionLocalOf { LinkItSpacing() }

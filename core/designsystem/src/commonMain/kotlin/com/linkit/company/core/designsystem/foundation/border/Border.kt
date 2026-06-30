package com.linkit.company.core.designsystem.foundation.border

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import com.linkit.company.core.designsystem.foundation.border.token.BorderAccessKeyToken

@Immutable
class LinkItBorderWidth(
    val sm: Dp = BorderDefaults.Sm,
    val md: Dp = BorderDefaults.Md,
    val lg: Dp = BorderDefaults.Lg,
) {
    fun copy(
        sm: Dp = this.sm,
        md: Dp = this.md,
        lg: Dp = this.lg,
    ): LinkItBorderWidth = LinkItBorderWidth(
        sm = sm,
        md = md,
        lg = lg,
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is LinkItBorderWidth) return false
        if (sm != other.sm) return false
        if (md != other.md) return false
        if (lg != other.lg) return false
        return true
    }

    override fun hashCode(): Int {
        var result = sm.hashCode()
        result = 31 * result + md.hashCode()
        result = 31 * result + lg.hashCode()
        return result
    }
}

internal fun LinkItBorderWidth.fromToken(value: BorderAccessKeyToken): Dp {
    return when (value) {
        BorderAccessKeyToken.WidthSm -> sm
        BorderAccessKeyToken.WidthMd -> md
        BorderAccessKeyToken.WidthLg -> lg
    }
}

internal val LocalBorderWidth = staticCompositionLocalOf { LinkItBorderWidth() }

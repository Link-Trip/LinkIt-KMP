package com.linkit.company.core.designsystem.foundation.shape

import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Shape
import com.linkit.company.core.designsystem.foundation.shape.token.ShapeAccessKeyToken

@Immutable
class LinkItShapes(
    val sm: CornerBasedShape = ShapeDefaults.Sm,
    val md: CornerBasedShape = ShapeDefaults.Md,
    val lg: CornerBasedShape = ShapeDefaults.Lg,
    val xl: CornerBasedShape = ShapeDefaults.Xl,
    val xxl: CornerBasedShape = ShapeDefaults.Xxl,
    val rounded: CornerBasedShape = ShapeDefaults.Rounded,
    val none: Shape = ShapeDefaults.None,
) {
    fun copy(
        sm: CornerBasedShape = this.sm,
        md: CornerBasedShape = this.md,
        lg: CornerBasedShape = this.lg,
        xl: CornerBasedShape = this.xl,
        xxl: CornerBasedShape = this.xxl,
        rounded: CornerBasedShape = this.rounded,
        none: Shape = this.none,
    ): LinkItShapes = LinkItShapes(
        sm = sm,
        md = md,
        lg = lg,
        xl = xl,
        xxl = xxl,
        rounded = rounded,
        none = none,
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is LinkItShapes) return false
        if (sm != other.sm) return false
        if (md != other.md) return false
        if (lg != other.lg) return false
        if (xl != other.xl) return false
        if (xxl != other.xxl) return false
        if (rounded != other.rounded) return false
        if (none != other.none) return false
        return true
    }

    override fun hashCode(): Int {
        var result = sm.hashCode()
        result = 31 * result + md.hashCode()
        result = 31 * result + lg.hashCode()
        result = 31 * result + xl.hashCode()
        result = 31 * result + xxl.hashCode()
        result = 31 * result + rounded.hashCode()
        result = 31 * result + none.hashCode()
        return result
    }
}

internal fun LinkItShapes.fromToken(value: ShapeAccessKeyToken): Shape {
    return when (value) {
        ShapeAccessKeyToken.CornerSm -> sm
        ShapeAccessKeyToken.CornerMd -> md
        ShapeAccessKeyToken.CornerLg -> lg
        ShapeAccessKeyToken.CornerXl -> xl
        ShapeAccessKeyToken.CornerXxl -> xxl
        ShapeAccessKeyToken.CornerRounded -> rounded
        ShapeAccessKeyToken.CornerNone -> none
    }
}

internal val LocalShapes = staticCompositionLocalOf { LinkItShapes() }

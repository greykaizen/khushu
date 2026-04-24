package com.kaizen.khushu.ui.util

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

/**
 * Adds padding to an existing PaddingValues object.
 * The calculation is deferred to the layout phase where calculate*Padding is called,
 * allowing the underlying PaddingValues (like WindowInsets) to update reactively
 * without causing recompositions.
 */
fun PaddingValues.add(
    start: Dp = 0.dp,
    top: Dp = 0.dp,
    end: Dp = 0.dp,
    bottom: Dp = 0.dp
): PaddingValues {
    return object : PaddingValues {
        override fun calculateLeftPadding(layoutDirection: LayoutDirection) =
            this@add.calculateLeftPadding(layoutDirection) + if (layoutDirection == LayoutDirection.Ltr) start else end

        override fun calculateTopPadding() = this@add.calculateTopPadding() + top

        override fun calculateRightPadding(layoutDirection: LayoutDirection) =
            this@add.calculateRightPadding(layoutDirection) + if (layoutDirection == LayoutDirection.Ltr) end else start

        override fun calculateBottomPadding() = this@add.calculateBottomPadding() + bottom
    }
}

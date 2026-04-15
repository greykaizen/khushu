package com.kaizen.khushu.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.kaizen.khushu.R

@Composable
fun KhushuLogoBadge(
    logoStyle: String,
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
    iconSize: Dp = 48.dp,
) {
    val context = LocalContext.current
    val bgColor = when (logoStyle) {
        "DARK"  -> Color(0xFF000000)
        "LIGHT" -> Color(0xFFFFFFFF)
        "GREEN" -> Color(0xFF1B7A3E)
        else    -> Color(ContextCompat.getColor(context, android.R.color.system_accent1_500))
    }
    val iconTint = when (logoStyle) {
        "LIGHT" -> Color(0xFF000000)
        else    -> Color(0xFFFFFFFF)
    }

    Surface(
        modifier = modifier.size(size),
        shape = CircleShape,
        color = bgColor,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                painter = painterResource(id = R.drawable.ic_khushu_logo),
                contentDescription = null,
                modifier = Modifier.size(iconSize),
                tint = iconTint,
            )
        }
    }
}

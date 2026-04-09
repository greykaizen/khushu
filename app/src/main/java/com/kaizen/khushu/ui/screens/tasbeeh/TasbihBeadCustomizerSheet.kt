package com.kaizen.khushu.ui.screens.tasbeeh

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kaizen.khushu.data.BeadStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasbihBeadCustomizerSheet(
    currentStyle: BeadStyle,
    onStyleSelected: (BeadStyle) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        dragHandle = null,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Text(
                text = "Bead Style",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )

            Text(
                text = "Choose how your tasbih beads look",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                BeadStyleCard(
                    label = "Classic Amber",
                    subtitle = "Warm resin, gold tones",
                    style = BeadStyle.CLASSIC_AMBER,
                    isSelected = currentStyle == BeadStyle.CLASSIC_AMBER,
                    onClick = { onStyleSelected(BeadStyle.CLASSIC_AMBER) },
                    modifier = Modifier.weight(1f),
                )
                BeadStyleCard(
                    label = "Dark Onyx",
                    subtitle = "Deep black, sharp gloss",
                    style = BeadStyle.DARK_ONYX,
                    isSelected = currentStyle == BeadStyle.DARK_ONYX,
                    onClick = { onStyleSelected(BeadStyle.DARK_ONYX) },
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun BeadStyleCard(
    label: String,
    subtitle: String,
    style: BeadStyle,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
    val bgColor = if (isSelected)
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
    else
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .border(2.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Live bead preview drawn on Canvas
        Canvas(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
        ) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val r = size.minDimension / 2f * 0.8f
            val center = Offset(cx, cy)
            val lightCenter = center + Offset(-r * 0.28f, -r * 0.28f)

            when (style) {
                BeadStyle.CLASSIC_AMBER -> {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFFFBF4D),
                                Color(0xFFD4850A),
                                Color(0xFF7A4000),
                            ),
                            center = lightCenter,
                            radius = r * 1.6f,
                        ),
                        radius = r,
                        center = center,
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = 0.65f),
                        radius = r * 0.22f,
                        center = lightCenter + Offset(r * 0.04f, r * 0.04f),
                    )
                }
                BeadStyle.DARK_ONYX -> {
                    drawOval(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF555555),
                                Color(0xFF1A1A1A),
                                Color(0xFF000000),
                            ),
                            center = lightCenter,
                            radius = r * 1.5f,
                        ),
                        topLeft = center + Offset(-r, -r * 1.08f),
                        size = androidx.compose.ui.geometry.Size(r * 2f, r * 2.16f),
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = 0.85f),
                        radius = r * 0.18f,
                        center = lightCenter + Offset(r * 0.05f, r * 0.05f),
                    )
                }
            }
        }

        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        if (isSelected) {
            Surface(
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.primary,
            ) {
                Text(
                    text = "Active",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                )
            }
        }
    }
}

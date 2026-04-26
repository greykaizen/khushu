package com.kaizen.khushu.ui.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.util.Calendar
import kotlin.math.max
import kotlin.math.min


// --- Stars for Dark Mode ---
private data class Star(val x: Float, val y: Float, val r: Float, val alpha: Float, val delay: Int)

private val STARS = List(22) { i ->
    Star(
        x = ((i * 47 + 19) % 120 + 5).toFloat(),
        y = ((i * 31 + 11) % 55 + 4).toFloat(),
        r = ((i * 13) % 8) / 10f + 0.35f,
        alpha = ((i * 17) % 8) / 10f * 0.38f + 0.1f,
        delay = ((i * 7) % 20) * 100
    )
}

// --- Bezier Arc Math ---
private val P0 = Offset(0f, 130f)
private val P1 = Offset(166f, -18f)
private val P2 = Offset(332f, 130f)

private fun bpt(t: Float): Offset {
    val u = 1 - t
    return Offset(
        u * u * P0.x + 2 * u * t * P1.x + t * t * P2.x,
        u * u * P0.y + 2 * u * t * P1.y + t * t * P2.y
    )
}

@Composable
fun SunArcCard(
    sunT: Float,
    nextT: Float?,
    nextName: String,
    makruhZones: List<MakruhZone>,
    darkTheme: Boolean,
    hijriDate: String,
    modifier: Modifier = Modifier
) {
    var activeMk: Int? by remember { mutableStateOf(null) }
    val effectiveNextT = when {
        nextT == null -> sunT.coerceIn(0f, 1f)
        nextT < sunT -> 1f
        else -> nextT
    }

    // Animate stars alpha
    val infiniteTransition = rememberInfiniteTransition(label = "stars")
    val starAlphas = STARS.map { star ->
        infiniteTransition.animateFloat(
            initialValue = 0.12f,
            targetValue = 0.55f,
            animationSpec = infiniteRepeatable(
                animation = tween(1800, delayMillis = star.delay, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "star_alpha"
        )
    }

    Box(
        modifier = modifier
            .size(161.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
//            .border(
//                1.dp,
//                MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
//                RoundedCornerShape(20.dp)
//            )
    ) {
        val inMakruh = makruhZones.find { sunT >= it.tStart && sunT <= it.tEnd }
        val makruhOverlayColor = Color(0xFFD98A24).copy(alpha = 0.10f)

        Column(modifier = Modifier.padding(top = 12.dp, start = 13.dp, end = 13.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = "SUN PATH",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 8.5.sp,
                        letterSpacing = 0.09.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                if (inMakruh != null) {
                    Box(
                        modifier = Modifier
                            .background(Color.Red.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${inMakruh.label.uppercase()} NOW",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 7.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.07.sp
                            ),
                            color = Color(0xFFE04030) // Hardcoded Makruh red
                        )
                    }
                } else {
                    Text(
                        text = if (darkTheme) "Night" else "Morning",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                }
            }
        }

        if (inMakruh != null) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(makruhOverlayColor)
            )
        }

        // --- Canvas Drawing ---
        val arcBaseColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
        val arcSolidColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        val arcNextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
        val mkruhColor = Color(0xFFE04030) // Hardcoded Makruh red
        val surfaceColor = MaterialTheme.colorScheme.surfaceContainerHigh
        val primaryColor = MaterialTheme.colorScheme.primary

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(makruhZones, sunT, effectiveNextT) {
                    detectTapGestures { offset ->
                        val pad = 9f.dp.toPx()
                        val svgW = size.width.toFloat() - 26f.dp.toPx()
                        val svgH = size.height.toFloat() - 58f.dp.toPx()

                        val tS = max(0f, sunT - 0.17f)
                        val tE = min(1f, effectiveNextT + 0.12f)
                        val steps = 60
                        val rawPts = (0..steps).map { bpt(tS + (tE - tS) * (it.toFloat() / steps)) }

                        val xs = rawPts.map { it.x }
                        val ys = rawPts.map { it.y }
                        val x0 = xs.minOrNull() ?: 0f
                        val x1 = xs.maxOrNull() ?: 1f
                        val y0 = ys.minOrNull() ?: 0f
                        val y1 = ys.maxOrNull() ?: 1f

                        val sx = svgW / (if (x1 == x0) 1f else x1 - x0)
                        val sy = svgH / (if (y1 == y0) 1f else y1 - y0)
                        val s = min(sx, sy)

                        val ox = pad + (svgW - (x1 - x0) * s) / 2
                        val oy = pad + (svgH - (y1 - y0) * s) / 2

                        fun mapPt(p: Offset) = Offset(ox + (p.x - x0) * s, oy + (p.y - y0) * s)

                        val hitX = offset.x - 13f.dp.toPx()
                        val hitY = offset.y - 26f.dp.toPx()
                        val tapPt = Offset(hitX, hitY)
                        val hitRadius = 40f.dp.toPx() // Increased touch target for Makruh zones

                        var found = false
                        for (i in makruhZones.indices) {
                            val mk = makruhZones[i]
                            if (mk.tEnd >= tS && mk.tStart <= tE) {
                                val mS = max(mk.tStart, tS)
                                val mE = min(mk.tEnd, tE)
                                val mpts = (0..15).map { mapPt(bpt(mS + (mE - mS) * (it / 15f))) }
                                if (mpts.any { (it - tapPt).getDistance() < hitRadius }) {
                                    activeMk = if (activeMk == i) null else i
                                    found = true
                                    break
                                }
                            }
                        }

                        if (!found) {
                            activeMk = null
                        }
                    }
                }
        ) {
            val pad = 9f.dp.toPx()
            val svgW = size.width - 26f.dp.toPx()
            val svgH = size.height - 58f.dp.toPx()

            val tS = max(0f, sunT - 0.17f)
            val tE = min(1f, effectiveNextT + 0.12f)
            val steps = 60
            val rawPts = (0..steps).map { bpt(tS + (tE - tS) * (it.toFloat() / steps)) }

            val xs = rawPts.map { it.x }
            val ys = rawPts.map { it.y }
            val x0 = xs.minOrNull() ?: 0f
            val x1 = xs.maxOrNull() ?: 1f
            val y0 = ys.minOrNull() ?: 0f
            val y1 = ys.maxOrNull() ?: 1f

            val sx = svgW / (if (x1 == x0) 1f else x1 - x0)
            val sy = svgH / (if (y1 == y0) 1f else y1 - y0)
            val s = min(sx, sy)

            val ox = pad + (svgW - (x1 - x0) * s) / 2
            val oy = pad + (svgH - (y1 - y0) * s) / 2

            fun mapPt(p: Offset) = Offset(ox + (p.x - x0) * s, oy + (p.y - y0) * s)

            translate(left = 13f.dp.toPx(), top = 26f.dp.toPx()) {

                // Draw stars if dark theme
                if (darkTheme) {
                    STARS.forEachIndexed { i, star ->
                        drawCircle(
                            color = Color.White,
                            radius = star.r.dp.toPx(),
                            center = Offset(star.x.dp.toPx(), star.y.dp.toPx()),
                            alpha = starAlphas[i].value
                        )
                    }
                }

                val sunM = mapPt(bpt(sunT))
                val nextM = mapPt(bpt(effectiveNextT))

                // Sun glow
                drawCircle(
                    color = primaryColor.copy(alpha = 0.1f),
                    radius = 30f.dp.toPx(),
                    center = sunM
                )

                // Base arc path
                val pathAll = Path().apply {
                    rawPts.forEachIndexed { i, p ->
                        val mp = mapPt(p)
                        if (i == 0) moveTo(mp.x, mp.y) else lineTo(mp.x, mp.y)
                    }
                }
                drawPath(
                    path = pathAll,
                    color = arcBaseColor,
                    style = Stroke(
                        width = 1.3f.dp.toPx(),
                        cap = StrokeCap.Round,
                        pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                            floatArrayOf(4f.dp.toPx(), 6f.dp.toPx())
                        )
                    )
                )

                // Past trail path
                val progIdx = ((sunT - tS) / (tE - tS) * steps).toInt().coerceIn(0, steps)
                if (progIdx > 0) {
                    val pathDone = Path().apply {
                        rawPts.take(progIdx + 1).forEachIndexed { i, p ->
                            val mp = mapPt(p)
                            if (i == 0) moveTo(mp.x, mp.y) else lineTo(mp.x, mp.y)
                        }
                    }
                    drawPath(
                        path = pathDone,
                        color = arcSolidColor,
                        style = Stroke(width = 1.8f.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                // Makruh segments
                makruhZones.forEachIndexed { index, mk ->
                    if (mk.tEnd >= tS && mk.tStart <= tE) {
                        val mS = max(mk.tStart, tS)
                        val mE = min(mk.tEnd, tE)
                        val mpts = (0..7).map { mapPt(bpt(mS + (mE - mS) * (it / 7f))) }
                        val mdPath = Path().apply {
                            mpts.forEachIndexed { j, p ->
                                if (j == 0) moveTo(p.x, p.y) else lineTo(p.x, p.y)
                            }
                        }
                        val isA = activeMk == index
                        drawPath(
                            path = mdPath,
                            color = mkruhColor,
                            style = Stroke(
                                width = if (isA) 9f.dp.toPx() else 5f.dp.toPx(),
                                cap = StrokeCap.Round
                            ),
                            alpha = if (isA) 0.7f else 0.36f
                        )
                    }
                }

                // Next prayer dot
                drawCircle(
                    color = arcNextColor,
                    radius = 8.5f.dp.toPx(),
                    center = nextM,
                    style = Stroke(width = 1.1f.dp.toPx()),
                    alpha = 0.4f
                )
                drawCircle(color = arcNextColor, radius = 3.8f.dp.toPx(), center = nextM)

                // Sun / Moon drawing
                if (!darkTheme) {
                    drawCircle(
                        color = Color(0xFFFAC82D).copy(alpha = 0.18f),
                        radius = 17f.dp.toPx(),
                        center = sunM
                    )
                    drawCircle(
                        color = Color(0xFFFFC328).copy(alpha = 0.32f),
                        radius = 12f.dp.toPx(),
                        center = sunM
                    )
                    drawCircle(
                        color = Color(0xFFF8C832).copy(alpha = 0.85f),
                        radius = 9f.dp.toPx(),
                        center = sunM,
                        style = Stroke(width = 2.5f.dp.toPx())
                    )
                    drawCircle(color = Color(0xFFFDE03C), radius = 5.5f.dp.toPx(), center = sunM)
                } else {
                    val craterColor = surfaceColor
                    drawCircle(
                        color = Color(0xFFB4A2FF).copy(alpha = 0.15f),
                        radius = 15f.dp.toPx(),
                        center = sunM
                    )
                    drawCircle(
                        color = Color(0xFFB9A8FF).copy(alpha = 0.25f),
                        radius = 10f.dp.toPx(),
                        center = sunM
                    )
                    drawCircle(
                        color = Color(0xFFD2CCFC).copy(alpha = 0.9f),
                        radius = 8.5f.dp.toPx(),
                        center = sunM
                    )
                    drawCircle(
                        color = craterColor,
                        radius = 7f.dp.toPx(),
                        center = Offset(sunM.x + 3.2f.dp.toPx(), sunM.y - 0.8f.dp.toPx())
                    )
                }
            }
        }

        // Date text
        Text(
            text = hijriDate.ifBlank { "Loading date..." },
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 13.dp, bottom = 10.dp)
        )

        // Makruh overlay
        AnimatedVisibility(
            visible = activeMk != null,
            enter = slideInVertically { it / 2 },
            exit = slideOutVertically { it / 2 }
        ) {
            if (activeMk != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.84f))
                        .clickable { activeMk = null }
                        .padding(15.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column {
                        val mk = makruhZones[activeMk!!]
                        Text(
                            text = "Makruh · ${mk.label}".uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 7.5.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.09.sp
                            ),
                            color = Color(0xFFF06045),
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        Text(
                            text = mk.description,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Light,
                                lineHeight = 16.sp
                            ),
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        Text(
                            text = "Tap to dismiss",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                            color = Color.White.copy(alpha = 0.3f),
                            modifier = Modifier.padding(top = 10.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NextPrayerCard(
    prayer: PrayerInfo?,
    doneCount: Int,
    source: CalculationSource = CalculationSource.LOCAL,
    usingPreviewTime: Boolean = false,
    onTimeClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Current Clock Logic
    var hh by remember { mutableStateOf("00") }
    var mm by remember { mutableStateOf("00") }
    var ss by remember { mutableStateOf("00") }

    LaunchedEffect(Unit) {
        while (true) {
            val now = Calendar.getInstance()
            // 12-hour format for the clock
            var hour = now.get(Calendar.HOUR)
            if (hour == 0) hour = 12 // handle 12 AM/PM

            hh = String.format("%02d", hour)
            mm = String.format("%02d", now.get(Calendar.MINUTE))
            ss = String.format("%02d", now.get(Calendar.SECOND))

            delay(1000)
        }
    }

    Box(
        modifier = modifier
            .size(161.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
//            .border(
//                1.dp,
//                MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
//                RoundedCornerShape(20.dp)
//            )
            .padding(top = 14.dp, start = 16.dp, end = 16.dp, bottom = 13.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "NEXT PRAYER",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 8.5.sp,
                            letterSpacing = 0.09.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        modifier = Modifier.padding(bottom = 5.dp)
                    )
                    // Source Indicator
                    Box(
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = source.name,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 7.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = prayer?.name ?: "Loading",
                        style = MaterialTheme.typography.displaySmall.copy(fontSize = 32.sp),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                }
                Text(
                    text = prayer?.time ?: "Calculating prayer times",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp,
                        fontStyle = FontStyle.Italic
                    ),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier
                        .padding(top = 3.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { onTimeClick() }
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }

            Column {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "$hh:$mm",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = ss,
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        modifier = Modifier.padding(start = 2.dp, bottom = 2.dp)
                    )
                }

                Row(
                    modifier = Modifier.padding(top = 9.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(5) { i ->
                        Box(
                            modifier = Modifier
                                .padding(end = 5.dp)
                                .size(if (i < doneCount) 7.dp else 5.5.dp)
                                .clip(CircleShape)
                                .background(
                                    if (i < doneCount) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
                                )
                        )
                    }
                    Text(
                        text = "$doneCount/5",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        modifier = Modifier.padding(start = 2.dp)
                    )
                }

                if (usingPreviewTime) {
                    Text(
                        text = "Preview time active",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun EventsStrip(
    events: List<IslamicEvent>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "UPCOMING · DHU'L-QI'DAH 1446",
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                letterSpacing = 0.09.sp,
                fontWeight = FontWeight.SemiBold
            ),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            modifier = Modifier.padding(start = 14.dp, bottom = 8.dp)
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(events) { index, ev ->
                val isFirst = index == 0
                val bgColor =
                    if (isFirst) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.surfaceContainerLow
                val borderColor =
                    if (isFirst) MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f) else MaterialTheme.colorScheme.outline.copy(
                        alpha = 0.2f
                    )
                val labelColor =
                    if (isFirst) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurface.copy(
                        alpha = 0.4f
                    )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(bgColor)
                        .border(1.dp, borderColor, RoundedCornerShape(14.dp))
                        .padding(10.dp)
                        .widthIn(min = 116.dp)
                ) {
                    Column {
                        Text(
                            text = ev.label.uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 7.5.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.06.sp
                            ),
                            color = labelColor,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                        Text(
                            text = ev.name,
                            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 14.sp),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = ev.date,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.5.sp),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PrayerSlab(
    prayers: List<PrayerInfo>,
    doneStates: Map<String, Boolean>,
    onToggleDone: (String) -> Unit,
//    ayahText: String,
    ayahRef: String,
    darkTheme: Boolean,
    bottomPadding: androidx.compose.ui.unit.Dp = 0.dp,
    modifier: Modifier = Modifier
) {
    val doneCount = doneStates.values.count { it }
    val borderColor =
        MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)


    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(start = 22.dp, end = 22.dp, top = 22.dp, bottom = 32.dp + bottomPadding)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TODAY'S PRAYERS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        letterSpacing = 0.09.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Text(
                    text = "$doneCount of 5",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }

            // Progress bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.5.dp)
                    .clip(RoundedCornerShape(2.dp))

                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(doneCount / 5f)
                        .height(2.5.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(MaterialTheme.colorScheme.primary)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Prayer List
            prayers.forEachIndexed { i, p ->
                val isPrayed = doneStates[p.name] ?: false
                val isNext =
                    !isPrayed && prayers.find { !(doneStates[it.name] ?: false) }?.name == p.name
                val dotColor = if (darkTheme) p.dotColorDark else p.dotColorLight

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { onToggleDone(p.name) }
                        .padding(vertical = 8.5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Dot
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(
                                if (isPrayed) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                                else dotColor
                            )
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    // Checkbox
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(if (isPrayed) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f) else Color.Transparent)
                            .border(
                                1.6.dp,
                                if (isPrayed) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.22f) else MaterialTheme.colorScheme.onSurface.copy(
                                    alpha = 0.14f
                                ),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isPrayed) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    // Name
                    Text(
                        text = p.name,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 18.sp,
                            fontWeight = if (isNext) FontWeight.SemiBold else FontWeight.Light
                        ),
                        color = if (isPrayed) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f) else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                        textDecoration = if (isPrayed) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                    )

                    if (isNext) {
                        Text(
                            text = "NEXT",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 7.5.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.1.sp
                            ),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }

                    // Arabic Name
                    Text(
                        text = p.ar,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 14.sp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Time
                    Text(
                        text = p.time,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        textAlign = TextAlign.End,
                        modifier = Modifier.defaultMinSize(minWidth = 60.dp)
                    )
                }

                if (i < prayers.size - 1) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                    )
                }
            }

//            Spacer(modifier = Modifier.height(14.dp))
//            Box(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(1.dp)
//                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
//            )
//            Spacer(modifier = Modifier.height(10.dp))

            // Ayah quote
//            Text(
//                text = "\"$ayahText\" — $ayahRef",
//                style = MaterialTheme.typography.bodyMedium.copy(
//                    fontSize = 11.5.sp,
//                    fontStyle = FontStyle.Italic,
//                    lineHeight = 16.5.sp
//                ),
//                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
//            )
        }
    }
}

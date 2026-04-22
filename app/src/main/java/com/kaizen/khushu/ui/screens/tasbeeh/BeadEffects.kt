package com.kaizen.khushu.ui.screens.tasbeeh

import android.graphics.BitmapShader
import android.graphics.Shader
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import com.kaizen.khushu.data.model.BeadDepthMode
import com.kaizen.khushu.data.model.BeadShapeType
import com.kaizen.khushu.data.model.BeadTextureStyle
import com.kaizen.khushu.data.model.CustomBeadStyle
import kotlin.random.Random

// ---------------------------------------------------------------------------
// Shape resolution
// ---------------------------------------------------------------------------

/** Maps a BeadShapeType to its Compose Shape. Call inside a @Composable context. */
@Composable
fun beadShapeTypeToShape(type: BeadShapeType): Shape = when (type) {
    BeadShapeType.CIRCLE         -> MaterialShapes.Circle.toShape()
    BeadShapeType.SQUARE         -> MaterialShapes.Square.toShape()
    BeadShapeType.TRIANGLE       -> MaterialShapes.Triangle.toShape()
    BeadShapeType.DIAMOND        -> MaterialShapes.Diamond.toShape()
    BeadShapeType.PILL           -> MaterialShapes.Pill.toShape()
    BeadShapeType.SLANTED        -> MaterialShapes.Slanted.toShape()
    BeadShapeType.SEMI_CIRCLE    -> MaterialShapes.SemiCircle.toShape()
    BeadShapeType.ARCH           -> MaterialShapes.Arch.toShape()
    BeadShapeType.COOKIE_4       -> MaterialShapes.Cookie4Sided.toShape()
    BeadShapeType.COOKIE_6       -> MaterialShapes.Cookie6Sided.toShape()
    BeadShapeType.COOKIE_7       -> MaterialShapes.Cookie7Sided.toShape()
    BeadShapeType.COOKIE_9       -> MaterialShapes.Cookie9Sided.toShape()
    BeadShapeType.COOKIE_12      -> MaterialShapes.Cookie12Sided.toShape()
    BeadShapeType.CLOVER_4       -> MaterialShapes.Clover4Leaf.toShape()
    BeadShapeType.CLOVER_8       -> MaterialShapes.Clover8Leaf.toShape()
    BeadShapeType.PUFFY          -> MaterialShapes.Puffy.toShape()
    BeadShapeType.PUFFY_DIAMOND  -> MaterialShapes.PuffyDiamond.toShape()
    BeadShapeType.SUNNY          -> MaterialShapes.Sunny.toShape()
    BeadShapeType.VERY_SUNNY     -> MaterialShapes.VerySunny.toShape()
    BeadShapeType.BURST          -> MaterialShapes.Burst.toShape()
    BeadShapeType.SOFT_BURST     -> MaterialShapes.SoftBurst.toShape()
    BeadShapeType.BOOM           -> MaterialShapes.Boom.toShape()
    BeadShapeType.SOFT_BOOM      -> MaterialShapes.SoftBoom.toShape()
    BeadShapeType.HEART          -> MaterialShapes.Heart.toShape()
    BeadShapeType.GEM            -> MaterialShapes.Gem.toShape()
    BeadShapeType.BUN            -> MaterialShapes.Bun.toShape()
    BeadShapeType.CLAMSHELL      -> MaterialShapes.ClamShell.toShape()
    BeadShapeType.FAN            -> MaterialShapes.Fan.toShape()
    BeadShapeType.ARROW          -> MaterialShapes.Arrow.toShape()
    BeadShapeType.PIXEL_CIRCLE   -> MaterialShapes.PixelCircle.toShape()
    BeadShapeType.PIXEL_TRIANGLE -> MaterialShapes.PixelTriangle.toShape()
}

/**
 * Converts a Compose Shape into a Path at the given pixel size.
 * Call inside remember(shapeType, size) { } — never on every frame.
 */
fun createBeadPath(
    shape: Shape,
    size: Size,
    layoutDirection: LayoutDirection,
    density: Density,
): Path {
    val outline = shape.createOutline(size, layoutDirection, density)
    return Path().apply {
        when (outline) {
            is Outline.Generic   -> addPath(outline.path)
            is Outline.Rectangle -> addRect(outline.rect)
            is Outline.Rounded   -> addRoundRect(outline.roundRect)
        }
    }
}

// ---------------------------------------------------------------------------
// Noise texture
// ---------------------------------------------------------------------------

/**
 * Creates a tileable greyscale noise Bitmap and wraps it in a GPU BitmapShader.
 * Works on all API levels (21+). Call inside remember { } — baked once per screen.
 */
fun createNoiseShader(size: Int = 128): Shader {
    val bitmap = android.graphics.Bitmap.createBitmap(
        size, size, android.graphics.Bitmap.Config.ARGB_8888
    )
    val rng = Random(seed = 7)
    for (y in 0 until size) {
        for (x in 0 until size) {
            val v = rng.nextInt(80) + 88  // 88–167: mid-grey bias
            bitmap.setPixel(x, y, android.graphics.Color.argb(v, v, v, v))
        }
    }
    return BitmapShader(bitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
}

// ---------------------------------------------------------------------------
// Effect 1 — Hard extrusion  (Emboss / Deboss, no BlurMaskFilter)
// ---------------------------------------------------------------------------

fun DrawScope.drawBeadExtrusion(path: Path, style: CustomBeadStyle) {
    when (style.depthMode) {
        BeadDepthMode.EMBOSS -> withTransform({ translate(left = 4f, top = 7f) }) {
            drawPath(path, color = Color.Black.copy(alpha = 0.38f))
        }
        BeadDepthMode.DEBOSS -> withTransform({ translate(left = -3f, top = -5f) }) {
            drawPath(path, color = Color.White.copy(alpha = 0.18f))
        }
    }
}

// ---------------------------------------------------------------------------
// Effect 2 — Base color fill
// ---------------------------------------------------------------------------

fun DrawScope.drawBeadBaseColor(path: Path, style: CustomBeadStyle) {
    drawPath(path, color = Color(style.baseColor))
}

// ---------------------------------------------------------------------------
// Effect 3 — Texture overlay  (FROSTED / RESIN via noise BitmapShader)
// BlendMode.Overlay tints the base color instead of overriding it.
// ---------------------------------------------------------------------------

fun DrawScope.drawBeadTexture(
    path: Path,
    style: CustomBeadStyle,
    noiseBrush: ShaderBrush?,
) {
    if (style.textureStyle == BeadTextureStyle.SOLID || noiseBrush == null) return
    val noiseAlpha = when (style.textureStyle) {
        BeadTextureStyle.FROSTED -> 0.22f
        BeadTextureStyle.RESIN   -> 0.38f
        else -> return
    }
    withTransform({ clipPath(path) }) {
        drawRect(brush = noiseBrush, alpha = noiseAlpha, blendMode = BlendMode.Overlay)
        if (style.textureStyle == BeadTextureStyle.FROSTED) {
            drawRect(color = Color.White.copy(alpha = 0.12f), blendMode = BlendMode.Screen)
        }
    }
}

// ---------------------------------------------------------------------------
// Effect 4 — Volumetric dome specular highlight
// Off-centre radial gradient simulates a top-left light source.
// ---------------------------------------------------------------------------

fun DrawScope.drawBeadSpecular(path: Path, specularBrush: Brush?) {
    if (specularBrush == null) return
    withTransform({ clipPath(path) }) {
        drawRect(brush = specularBrush)
    }
}

// ---------------------------------------------------------------------------
// Effect 5 — Chromatic aberration  (fake glass refraction)
// Cyan + Magenta offset draws with Screen blend.
// ---------------------------------------------------------------------------

fun DrawScope.drawBeadChromaticAberration(path: Path, style: CustomBeadStyle) {
    if (!style.chromaticAberration) return
    val shift = 2.2f
    withTransform({ translate(left = -shift, top = 0f) }) {
        drawPath(path, color = Color.Cyan.copy(alpha = 0.32f), blendMode = BlendMode.Screen)
    }
    withTransform({ translate(left = shift, top = 0f) }) {
        drawPath(path, color = Color.Magenta.copy(alpha = 0.32f), blendMode = BlendMode.Screen)
    }
}

// ---------------------------------------------------------------------------
// Effect 6 — Anisotropic metallic sheen  (machined metal bands)
// Sweep gradient with alternating light/dark bands clipped to path.
// ---------------------------------------------------------------------------

fun DrawScope.drawBeadMetallicSheen(path: Path, metallicBrush: Brush?) {
    if (metallicBrush == null) return
    withTransform({ clipPath(path) }) {
        drawRect(brush = metallicBrush, blendMode = BlendMode.Overlay, alpha = 0.75f)
    }
}

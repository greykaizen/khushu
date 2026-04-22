package com.kaizen.khushu.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class BeadShapeType {
    CIRCLE, SQUARE, TRIANGLE, DIAMOND, PILL, SLANTED, SEMI_CIRCLE, ARCH,
    COOKIE_4, COOKIE_6, COOKIE_7, COOKIE_9, COOKIE_12,
    CLOVER_4, CLOVER_8, PUFFY, PUFFY_DIAMOND,
    SUNNY, VERY_SUNNY, BURST, SOFT_BURST, BOOM, SOFT_BOOM,
    HEART, GEM, BUN, CLAMSHELL, FAN, ARROW,
    PIXEL_CIRCLE, PIXEL_TRIANGLE
}

@Serializable
enum class BeadDepthMode { EMBOSS, DEBOSS }

@Serializable
enum class BeadTextureStyle { SOLID, FROSTED, RESIN }

@Serializable
enum class BeadPreset {
    NONE,
    AMBER,
    OBSIDIAN,
    JADE,
    CRYSTAL,
    GOLD,
    PEARL,
}

@Serializable
data class CustomBeadStyle(
    val id: String,
    val name: String,
    val shapeType: BeadShapeType = BeadShapeType.CIRCLE,
    val baseColor: Long = 0xFFFFBF4D,
    val engravingText: String = "",
    val textScale: Float = 1.0f,
    val textOffsetX: Float = 0f,
    val textOffsetY: Float = 0f,
    val depthMode: BeadDepthMode = BeadDepthMode.EMBOSS,
    val textureStyle: BeadTextureStyle = BeadTextureStyle.SOLID,
    val is3dEnabled: Boolean = true,
    val chromaticAberration: Boolean = false,
    val metallicSheen: Boolean = false,
    val specularity: Float = 0.6f,
    val preset: BeadPreset = BeadPreset.NONE,
)

/** Returns a copy of this style with all fields set for the given preset. */
fun CustomBeadStyle.applyPreset(preset: BeadPreset): CustomBeadStyle = when (preset) {
    BeadPreset.NONE     -> this.copy(preset = BeadPreset.NONE)
    BeadPreset.AMBER    -> copy(
        baseColor = 0xFFD4850A, depthMode = BeadDepthMode.EMBOSS,
        textureStyle = BeadTextureStyle.RESIN, specularity = 0.7f,
        chromaticAberration = false, metallicSheen = false, is3dEnabled = true,
        preset = BeadPreset.AMBER
    )
    BeadPreset.OBSIDIAN -> copy(
        baseColor = 0xFF1A1A1A, depthMode = BeadDepthMode.DEBOSS,
        textureStyle = BeadTextureStyle.FROSTED, specularity = 0.3f,
        chromaticAberration = false, metallicSheen = false, is3dEnabled = true,
        preset = BeadPreset.OBSIDIAN
    )
    BeadPreset.JADE     -> copy(
        baseColor = 0xFF1B4332, depthMode = BeadDepthMode.EMBOSS,
        textureStyle = BeadTextureStyle.SOLID, specularity = 0.5f,
        chromaticAberration = false, metallicSheen = false, is3dEnabled = true,
        preset = BeadPreset.JADE
    )
    BeadPreset.CRYSTAL  -> copy(
        baseColor = 0xFF9DD4F0, depthMode = BeadDepthMode.EMBOSS,
        textureStyle = BeadTextureStyle.FROSTED, specularity = 0.8f,
        chromaticAberration = true, metallicSheen = false, is3dEnabled = true,
        preset = BeadPreset.CRYSTAL
    )
    BeadPreset.GOLD     -> copy(
        baseColor = 0xFFD4AF37, depthMode = BeadDepthMode.EMBOSS,
        textureStyle = BeadTextureStyle.SOLID, specularity = 0.9f,
        chromaticAberration = false, metallicSheen = true, is3dEnabled = true,
        preset = BeadPreset.GOLD
    )
    BeadPreset.PEARL    -> copy(
        baseColor = 0xFFF0EAD6, depthMode = BeadDepthMode.DEBOSS,
        textureStyle = BeadTextureStyle.FROSTED, specularity = 0.4f,
        chromaticAberration = false, metallicSheen = false, is3dEnabled = true,
        preset = BeadPreset.PEARL
    )
}

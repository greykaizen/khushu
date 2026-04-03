package com.kaizen.khushu.ui.screens.salah

/**
 * Visual style presets for the Salah immersive counter.
 *
 * To add a new preset:
 *   1. Add an object here (e.g. `object OutlineRight : SalahPreset()`)
 *   2. Add a branch in `SalahImmersiveScreen.kt`'s `when (preset)` block
 */
sealed class SalahPreset {
    /** Preset 1 — minimal: medium-large Antonio numeral, centered, light grey fill */
    object Minimal : SalahPreset()

    /** Preset 2 — custom: user-defined widgets from the canvas editor */
    object Custom : SalahPreset()

    // Reserved for future presets (design §6):
    // object OutlineRight : SalahPreset()   // Preset 2
    // object MassiveFill : SalahPreset()    // Preset 3
    // object OutlineCenter : SalahPreset()  // Preset 4
    // object ClockArabic : SalahPreset()    // Preset 5
}

package com.kaizen.khushu.ui.screens.tasbeeh

import android.content.Context
import android.media.MediaPlayer
import com.kaizen.khushu.R

data class TasbihSoundOption(
    val id: String,
    val label: String,
    val resId: Int,
)

object TasbihSoundCatalog {
    const val DEFAULT_ID = "1"

    val options = listOf(
        TasbihSoundOption(id = "1", label = "Soft Click", resId = R.raw.tasbih_soft_click),
        TasbihSoundOption(id = "2", label = "Glass Tap", resId = R.raw.tasbih_glass_tap),
        TasbihSoundOption(id = "3", label = "Muted Bead", resId = R.raw.tasbih_muted_bead),
        TasbihSoundOption(id = "4", label = "Light Chime", resId = R.raw.tasbih_light_chime),
        TasbihSoundOption(id = "5", label = "Warm Knock", resId = R.raw.tasbih_warm_knock),
    )

    fun optionFor(id: String): TasbihSoundOption {
        return options.firstOrNull { it.id == id } ?: options.first()
    }
}

class TasbihSoundPlayer(context: Context) {
    private val appContext = context.applicationContext
    private var activePlayer: MediaPlayer? = null

    fun play(soundId: String, volume: Float = 0.72f) {
        val option = TasbihSoundCatalog.options.firstOrNull { it.id == soundId } ?: return
        activePlayer?.runCatching {
            stop()
            release()
        }
        activePlayer = runCatching {
            MediaPlayer.create(appContext, option.resId)?.apply {
                setVolume(volume, volume)
                setOnCompletionListener { player ->
                    player.release()
                    if (activePlayer === player) {
                        activePlayer = null
                    }
                }
                start()
            }
        }.getOrNull()
    }

    fun release() {
        activePlayer?.runCatching {
            stop()
            release()
        }
        activePlayer = null
    }
}

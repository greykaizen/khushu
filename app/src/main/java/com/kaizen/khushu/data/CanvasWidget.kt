package com.kaizen.khushu.data

import kotlinx.serialization.Serializable

@Serializable
sealed interface CanvasWidget {
    val id: String
    val offsetX: Float      // absolute pixel position
    val offsetY: Float      // absolute pixel position
    val scale: Float        // 1.0f = default size
    val zIndex: Float
    val width: Float        // measured width in pixels
    val height: Float       // measured height in pixels

    @Serializable
    data class RakatCount(
        override val id: String = java.util.UUID.randomUUID().toString(),
        override val offsetX: Float = 0f,
        override val offsetY: Float = 0f,
        override val scale: Float = 1f,
        override val zIndex: Float = 0f,
        override val width: Float = 0f,
        override val height: Float = 0f,
        val color: Int = 0xFFFFFFFF.toInt(),
        val opacity: Float = 1f,
        val fontSizeSp: Float = 180f,
        val fontWeight: Int = 400,
        val isOutline: Boolean = false,
    ) : CanvasWidget

    @Serializable
    data class ClockWidget(
        override val id: String = java.util.UUID.randomUUID().toString(),
        override val offsetX: Float = 0f,
        override val offsetY: Float = 0f,
        override val scale: Float = 1f,
        override val zIndex: Float = 1f,
        override val width: Float = 0f,
        override val height: Float = 0f,
        val color: Int = 0xFFFFFFFF.toInt(),
        val opacity: Float = 1f,
        val fontSizeSp: Float = 48f,
        val showSeconds: Boolean = false,
        val use24Hour: Boolean = true,
        val isOutline: Boolean = false,
    ) : CanvasWidget

    @Serializable
    data class CustomText(
        override val id: String = java.util.UUID.randomUUID().toString(),
        override val offsetX: Float = 0f,
        override val offsetY: Float = 0f,
        override val scale: Float = 1f,
        override val zIndex: Float = 2f,
        override val width: Float = 0f,
        override val height: Float = 0f,
        val text: String = "Bismillah",
        val color: Int = 0xFFFFFFFF.toInt(),
        val opacity: Float = 1f,
        val fontSizeSp: Float = 32f,
        val fontWeight: Int = 400,
        val italic: Boolean = false,
        val textAlign: String = "Center",
        val verticalAlign: String = "Center",
        val isOutline: Boolean = false,
    ) : CanvasWidget
}

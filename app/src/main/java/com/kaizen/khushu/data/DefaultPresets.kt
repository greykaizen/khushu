package com.kaizen.khushu.data

object DefaultPresets {
    val defaults = listOf(
        CanvasPreset(
            id = "classy",
            name = "Classy",
            backgroundColor = 0xFF000000.toInt(),
            widgets = listOf(
                CanvasWidget.RakatCount(
                    offsetX = 441.7226f,
                    offsetY = 662.8323f,
                    scale = 3.3677413f,
                    color = -1,
                    opacity = 1.0f,
                    fontSizeSp = 77.18303f,
                    fontWeight = 400,
                    isOutline = true,
                    fontName = "Antonio"
                ),
            ),
            isDeletable = false
        ),
        CanvasPreset(
            id = "pure",
            name = "Pure",
            backgroundColor = 0xFF000000.toInt(),
            widgets = listOf(
                CanvasWidget.RakatCount(
                    offsetX = 770.9055f,
                    offsetY = 667.29614f,
                    scale = 3.3677413f,
                    color = -1,
                    opacity = 1.0f,
                    fontSizeSp = 77.18303f,
                    fontWeight = 400,
                    isOutline = false,
                    fontName = "Antonio"
                ),
                CanvasWidget.CustomText(
                    offsetX = 114.73828f,
                    offsetY = 1122.4414f,
                    scale = 1.0f,
                    text = "صلاة",
                    color = -1,
                    opacity = 1.0f,
                    fontSizeSp = 71.04196f,
                    fontWeight = 400,
                    italic = false,
                    textAlign = "Center",
                    verticalAlign = "Center",
                    isOutline = true,
                    fontName = "BeVietnamPro"
                ),
            ),
            isDeletable = false
        ),
        CanvasPreset(
            id = "subtle",
            name = "Subtle",
            backgroundColor = 0xFF000000.toInt(),
            widgets = listOf(
                CanvasWidget.RakatCount(
                    offsetX = 378.284f,
                    offsetY = 31.923096f,
                    scale = 3.3677413f,
                    color = -1,
                    opacity = 1.0f,
                    fontSizeSp = 180.0f,
                    fontWeight = 400,
                    isOutline = false,
                    fontName = "Antonio"
                ),
                CanvasWidget.CustomText(
                    offsetX = 67.146484f,
                    offsetY = 108.02832f,
                    scale = 1.0f,
                    text = "صلاة",
                    color = -1,
                    opacity = 1.0f,
                    fontSizeSp = 71.04196f,
                    fontWeight = 400,
                    italic = false,
                    textAlign = "Center",
                    verticalAlign = "Center",
                    isOutline = true,
                    fontName = "BeVietnamPro"
                ),
            ),
            isDeletable = false
        ),
        CanvasPreset(
            id = "premium",
            name = "Premium",
            backgroundColor = 0xFF000000.toInt(),
            widgets = listOf(
                CanvasWidget.ClockWidget(
                    offsetX = 41.727997f,
                    offsetY = 242.21165f,
                    scale = 1.5047318f,
                    color = -1,
                    opacity = 1.0f,
                    fontSizeSp = 48.0f,
                    showSeconds = false,
                    use24Hour = true,
                    isOutline = false,
                    fontName = "BeVietnamPro"
                ),
                CanvasWidget.CustomText(
                    offsetX = 719.8713f,
                    offsetY = 589.70654f,
                    scale = 2.133892f,
                    text = "صلاة",
                    color = -1,
                    opacity = 1.0f,
                    fontSizeSp = 32.0f,
                    fontWeight = 400,
                    italic = false,
                    textAlign = "Center",
                    verticalAlign = "Center",
                    isOutline = false,
                    fontName = "BeVietnamPro"
                ),
                CanvasWidget.RakatCount(
                    offsetX = 559.13025f,
                    offsetY = 687.25366f,
                    scale = 2.5634913f,
                    color = -1,
                    opacity = 1.0f,
                    fontSizeSp = 180.0f,
                    fontWeight = 400,
                    isOutline = false,
                    fontName = "Antonio"
                ),
            ),
            isDeletable = false
        ),
    )
}
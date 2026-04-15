package com.kaizen.khushu.data

object DefaultPresets {
    val defaults = listOf(
        CanvasPreset(
            id = "core",
            name = "Minimal",
            backgroundColor = 0xFF000000.toInt(),
            widgets = listOf(
                CanvasWidget.RakatCount(offsetX = 0.5f, offsetY = 0.5f, scale = 1.8902512f, color = -1, opacity = 1.0f, fontSizeSp = 122.17562f, fontWeight = 400, isOutline = false, fontName = "Antonio")
            ),
            isDeletable = false
        ),
        CanvasPreset(
            id = "signature",
            name = "Noor",
            backgroundColor = 0xFF000000.toInt(),
            widgets = listOf(
                CanvasWidget.RakatCount(offsetX = 0.5f, offsetY = 0.5f, scale = 1.8902512f, color = -1, opacity = 1.0f, fontSizeSp = 122.17562f, fontWeight = 400, isOutline = true, fontName = "Antonio")
            ),
            isDeletable = false
        ),
        CanvasPreset(
            id = "horizon",
            name = "Fajr",
            backgroundColor = 0xFF000000.toInt(),
            widgets = listOf(
                CanvasWidget.RakatCount(offsetX = 0.82547814f, offsetY = 0.46540943f, scale = 1.8902512f, color = -1, opacity = 1.0f, fontSizeSp = 122.17562f, fontWeight = 400, isOutline = true, fontName = "Antonio"),
                CanvasWidget.CustomText(offsetX = 0.25720674f, offsetY = 0.49911657f, scale = 1.2732823f, text = "صلاة", color = -1, opacity = 1.0f, fontSizeSp = 65.48039f, fontWeight = 400, italic = false, textAlign = "Center", verticalAlign = "Center", isOutline = false, fontName = "BeVietnamPro")
            ),
            isDeletable = false
        ),
        CanvasPreset(
            id = "eclipse",
            name = "Layl",
            backgroundColor = 0xFF000000.toInt(),
            widgets = listOf(
                CanvasWidget.RakatCount(offsetX = 0.82547814f, offsetY = 0.46540943f, scale = 1.8902512f, color = -1, opacity = 1.0f, fontSizeSp = 122.17562f, fontWeight = 400, isOutline = false, fontName = "Antonio"),
                CanvasWidget.CustomText(offsetX = 0.25720674f, offsetY = 0.49911657f, scale = 1.2732823f, text = "صلاة", color = -1, opacity = 1.0f, fontSizeSp = 65.48039f, fontWeight = 400, italic = false, textAlign = "Center", verticalAlign = "Center", isOutline = true, fontName = "BeVietnamPro")
            ),
            isDeletable = false
        ),
        CanvasPreset(
            id = "essence",
            name = "Ruh",
            backgroundColor = 0xFF000000.toInt(),
            widgets = listOf(
                CanvasWidget.RakatCount(offsetX = 0.6724961f, offsetY = 0.5379764f, scale = 1.8902512f, color = -1, opacity = 1.0f, fontSizeSp = 300.0f, fontWeight = 400, isOutline = false, fontName = "Antonio"),
                CanvasWidget.CustomText(offsetX = 0.29035127f, offsetY = 0.12168428f, scale = 1.2732823f, text = "صلاة", color = -1, opacity = 1.0f, fontSizeSp = 65.48039f, fontWeight = 400, italic = false, textAlign = "Center", verticalAlign = "Center", isOutline = true, fontName = "BeVietnamPro")
            ),
            isDeletable = false
        ),
        CanvasPreset(
            id = "lumina",
            name = "Badr",
            backgroundColor = 0xFF000000.toInt(),
            widgets = listOf(
                CanvasWidget.RakatCount(offsetX = 0.6724961f, offsetY = 0.5379764f, scale = 1.8902512f, color = -1, opacity = 1.0f, fontSizeSp = 300.0f, fontWeight = 400, isOutline = true, fontName = "Antonio"),
                CanvasWidget.CustomText(offsetX = 0.29035127f, offsetY = 0.12168428f, scale = 1.2732823f, text = "صلاة", color = -1, opacity = 1.0f, fontSizeSp = 65.48039f, fontWeight = 400, italic = false, textAlign = "Center", verticalAlign = "Center", isOutline = false, fontName = "BeVietnamPro")
            ),
            isDeletable = false
        ),
        CanvasPreset(
            id = "zenith",
            name = "Dhuha",
            backgroundColor = 0xFF000000.toInt(),
            widgets = listOf(
                CanvasWidget.RakatCount(offsetX = 0.7321662f, offsetY = 0.62215555f, scale = 1.5458537f, color = -1, opacity = 1.0f, fontSizeSp = 300.0f, fontWeight = 400, isOutline = true, fontName = "Antonio"),
                CanvasWidget.CustomText(offsetX = 0.7394825f, offsetY = 0.27562904f, scale = 1.0f, text = "صلاة", color = -1, opacity = 1.0f, fontSizeSp = 65.48039f, fontWeight = 400, italic = false, textAlign = "Center", verticalAlign = "Center", isOutline = false, fontName = "BeVietnamPro"),
                CanvasWidget.ClockWidget(offsetX = 0.29168332f, offsetY = 0.12341536f, scale = 1.5398576f, color = -1, opacity = 1.0f, fontSizeSp = 48.0f, showSeconds = false, use24Hour = false, isOutline = false, fontName = "BeVietnamPro")
            ),
            isDeletable = false
        ),
    )
}

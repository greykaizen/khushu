package com.kaizen.khushu.data

import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

class CanvasWidgetListConverter {
    private val json = Json { }

    @TypeConverter
    fun fromJson(value: String): List<CanvasWidget> = json.decodeFromString(value)

    @TypeConverter
    fun toJson(items: List<CanvasWidget>): String = json.encodeToString(items)
}

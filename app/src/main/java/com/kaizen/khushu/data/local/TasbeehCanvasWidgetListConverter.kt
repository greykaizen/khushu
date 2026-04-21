package com.kaizen.khushu.data.local

import androidx.room.TypeConverter
import com.kaizen.khushu.ui.screens.tasbeeh.TasbihWidget
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

class TasbeehCanvasWidgetListConverter {
    private val json = Json {
        ignoreUnknownKeys = true
        serializersModule = SerializersModule {
            polymorphic(TasbihWidget::class) {
                subclass(TasbihWidget.StringBeadWidget.serializer())
                subclass(TasbihWidget.DhikrNameWidget.serializer())
                subclass(TasbihWidget.CounterWidget.serializer())
                subclass(TasbihWidget.ProgressCircleWidget.serializer())
                subclass(TasbihWidget.MeaningWidget.serializer())
            }
        }
    }

    @TypeConverter
    fun fromJson(value: String): List<TasbihWidget> = json.decodeFromString(value)

    @TypeConverter
    fun toJson(items: List<TasbihWidget>): String = json.encodeToString(items)
}

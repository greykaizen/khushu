package com.kaizen.khushu.ui.screens.tasbeeh

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.kaizen.khushu.data.local.TasbeehCanvasWidgetListConverter

@Entity(tableName = "tasbeeh_canvas_layouts")
@TypeConverters(TasbeehCanvasWidgetListConverter::class)
data class TasbeehCanvasLayout(
    @PrimaryKey val id: String = "default",
    val backgroundColorInt: Int = 0xFF000000.toInt(),
    val widgets: List<TasbihWidget> = emptyList(),
)

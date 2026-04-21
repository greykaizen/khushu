package com.kaizen.khushu.data.local

import android.content.Context
import androidx.room.*
import com.kaizen.khushu.ui.screens.salah.SalahCanvasLayout
import com.kaizen.khushu.ui.screens.tasbeeh.TasbeehCanvasLayout
import com.kaizen.khushu.data.model.PresetEntity
import com.kaizen.khushu.data.model.TasbeehPresetEntity

@Database(
    entities = [SalahCanvasLayout::class, TasbeehCanvasLayout::class, PresetEntity::class, TasbeehPresetEntity::class],
    version = 5,
    exportSchema = false
)
@TypeConverters(CanvasWidgetListConverter::class, TasbeehCanvasWidgetListConverter::class)
abstract class CanvasDatabase : RoomDatabase() {
    abstract fun canvasDao(): CanvasDao

    companion object {
        @Volatile
        private var INSTANCE: CanvasDatabase? = null

        fun getInstance(context: Context): CanvasDatabase =
            INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    CanvasDatabase::class.java,
                    "canvas_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                .also { INSTANCE = it }
            }
    }
}

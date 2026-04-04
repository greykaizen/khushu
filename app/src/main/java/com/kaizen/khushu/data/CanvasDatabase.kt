package com.kaizen.khushu.data

import android.content.Context
import androidx.room.*

@Database(entities = [SalahCanvasLayout::class, PresetEntity::class], version = 1, exportSchema = false)
@TypeConverters(CanvasWidgetListConverter::class)
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
                ).build().also { INSTANCE = it }
            }
    }
}

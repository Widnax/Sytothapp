package com.example.sytothapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.sytothapp.data.local.dao.DailyEntryDao
import com.example.sytothapp.data.local.entity.DailyEntry

@Database(entities = [DailyEntry::class], version = 2, exportSchema = false)
@TypeConverters(Converters::class)
abstract class SytothDatabase : RoomDatabase() {
    abstract fun dailyEntryDao(): DailyEntryDao

    companion object {
        const val DATABASE_NAME = "sytoth_db"

        @Volatile
        private var INSTANCE: SytothDatabase? = null

        fun getDatabase(context: android.content.Context): SytothDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = androidx.room.Room.databaseBuilder(
                    context.applicationContext,
                    SytothDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

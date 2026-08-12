package com.wifisense.motiontracker.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * Room database for the WiFi Motion Tracker application.
 * Contains: [ActivitySessionEntity]
 */
@Database(
    entities = [ActivitySessionEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun sessionDao(): SessionDao
}

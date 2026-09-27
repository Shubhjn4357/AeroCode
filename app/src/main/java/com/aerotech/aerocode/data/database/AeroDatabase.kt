package com.aerotech.aerocode.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        ProjectEntity::class,
        RecentFileEntity::class,
        PluginEntity::class,
        WorkspaceEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AeroDatabase : RoomDatabase() {
    abstract fun projectDao(): ProjectDao
    abstract fun recentFileDao(): RecentFileDao
    abstract fun pluginDao(): PluginDao
    abstract fun workspaceDao(): WorkspaceDao

    companion object {
        @Volatile
        private var INSTANCE: AeroDatabase? = null

        fun getInstance(context: Context): AeroDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AeroDatabase::class.java,
                    "aerocode.db"
                ).fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

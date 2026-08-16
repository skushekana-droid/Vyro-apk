package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [VideoEntity::class, UserEntity::class, StassenMemoryEntity::class],
    version = 3,
    exportSchema = false
)
abstract class VyroDatabase : RoomDatabase() {
    abstract fun videoDao(): VideoDao
    abstract fun userDao(): UserDao
    abstract fun stassenMemoryDao(): StassenMemoryDao

    companion object {
        @Volatile
        private var INSTANCE: VyroDatabase? = null

        fun getDatabase(context: Context): VyroDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    VyroDatabase::class.java,
                    "vyro_platform.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}

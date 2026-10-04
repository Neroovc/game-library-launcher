package com.gamelauncher.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.gamelauncher.core.database.dao.GameDao
import com.gamelauncher.core.database.entity.GameEntity

@Database(
    entities = [GameEntity::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun gameDao(): GameDao

    companion object {
        const val DATABASE_NAME = "game_library.db"
    }
}

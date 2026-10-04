package com.gamelauncher.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.gamelauncher.core.database.dao.AssetDao
import com.gamelauncher.core.database.dao.GameDao
import com.gamelauncher.core.database.dao.TagDao
import com.gamelauncher.core.database.entity.AssetEntity
import com.gamelauncher.core.database.entity.GameEntity
import com.gamelauncher.core.database.entity.GameTagEntity
import com.gamelauncher.core.database.entity.TagEntity

@Database(
    entities = [GameEntity::class, TagEntity::class, GameTagEntity::class, AssetEntity::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun gameDao(): GameDao
    abstract fun tagDao(): TagDao
    abstract fun assetDao(): AssetDao

    companion object {
        const val DATABASE_NAME = "game_library.db"
    }
}

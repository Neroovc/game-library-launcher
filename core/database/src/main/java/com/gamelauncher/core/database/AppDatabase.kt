package com.gamelauncher.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.gamelauncher.core.database.dao.AssetDao
import com.gamelauncher.core.database.dao.GameDao
import com.gamelauncher.core.database.dao.InstallationDao
import com.gamelauncher.core.database.dao.LocalizationDao
import com.gamelauncher.core.database.dao.PlaySessionDao
import com.gamelauncher.core.database.dao.TagDao
import com.gamelauncher.core.database.entity.AssetEntity
import com.gamelauncher.core.database.entity.GameEntity
import com.gamelauncher.core.database.entity.GameTagEntity
import com.gamelauncher.core.database.entity.InstallationEntity
import com.gamelauncher.core.database.entity.LocalizationEntity
import com.gamelauncher.core.database.entity.PlaySessionEntity
import com.gamelauncher.core.database.entity.TagEntity

@Database(
    entities = [GameEntity::class, TagEntity::class, GameTagEntity::class, AssetEntity::class, InstallationEntity::class, PlaySessionEntity::class, LocalizationEntity::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun gameDao(): GameDao
    abstract fun tagDao(): TagDao
    abstract fun assetDao(): AssetDao
    abstract fun installationDao(): InstallationDao
    abstract fun playSessionDao(): PlaySessionDao
    abstract fun localizationDao(): LocalizationDao

    companion object {
        const val DATABASE_NAME = "game_library.db"
    }
}

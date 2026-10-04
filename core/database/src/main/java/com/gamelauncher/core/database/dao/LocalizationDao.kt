package com.gamelauncher.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.gamelauncher.core.database.entity.LocalizationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LocalizationDao {
    @Query("SELECT * FROM localizations WHERE gameId = :gameId")
    fun getLocalizationsForGame(gameId: String): Flow<List<LocalizationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocalization(localization: LocalizationEntity): Long

    @Update
    suspend fun updateLocalization(localization: LocalizationEntity)

    @Delete
    suspend fun deleteLocalization(localization: LocalizationEntity)
}

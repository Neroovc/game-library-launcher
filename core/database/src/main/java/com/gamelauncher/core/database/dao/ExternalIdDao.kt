package com.gamelauncher.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.gamelauncher.core.database.entity.ExternalIdEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExternalIdDao {
    @Query("SELECT * FROM external_ids WHERE gameId = :gameId")
    fun getExternalIdsForGame(gameId: String): Flow<List<ExternalIdEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExternalId(externalId: ExternalIdEntity): Long

    @Update
    suspend fun updateExternalId(externalId: ExternalIdEntity)

    @Delete
    suspend fun deleteExternalId(externalId: ExternalIdEntity)
}

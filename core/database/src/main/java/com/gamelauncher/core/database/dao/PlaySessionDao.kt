package com.gamelauncher.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.gamelauncher.core.database.entity.PlaySessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaySessionDao {
    @Query("SELECT * FROM play_sessions WHERE gameId = :gameId")
    fun getPlaySessionsForGame(gameId: String): Flow<List<PlaySessionEntity>>

    @Query("SELECT SUM(durationMs) FROM play_sessions WHERE gameId = :gameId")
    suspend fun getTotalPlaytimeForGame(gameId: String): Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaySession(session: PlaySessionEntity): Long

    @Update
    suspend fun updatePlaySession(session: PlaySessionEntity)

    @Delete
    suspend fun deletePlaySession(session: PlaySessionEntity)
}

package com.gamelauncher.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.gamelauncher.core.database.entity.InstallationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InstallationDao {
    @Query("SELECT * FROM installations WHERE gameId = :gameId")
    fun getInstallationsForGame(gameId: String): Flow<List<InstallationEntity>>

    @Query("SELECT * FROM installations WHERE id = :id")
    suspend fun getInstallationById(id: String): InstallationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInstallation(installation: InstallationEntity): Long

    @Update
    suspend fun updateInstallation(installation: InstallationEntity)

    @Delete
    suspend fun deleteInstallation(installation: InstallationEntity)
}

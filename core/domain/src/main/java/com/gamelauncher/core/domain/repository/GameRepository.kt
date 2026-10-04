package com.gamelauncher.core.domain.repository

import com.gamelauncher.core.domain.model.Game
import kotlinx.coroutines.flow.Flow

interface GameRepository {
    fun getAllGames(): Flow<List<Game>>
    suspend fun getGameById(id: String): Game?
    suspend fun insertGame(game: Game)
    suspend fun updateGame(game: Game)
    suspend fun deleteGame(game: Game)
}

package com.gamelauncher.core.database.repository

import com.gamelauncher.core.database.dao.GameDao
import com.gamelauncher.core.database.mapper.GameMapper
import com.gamelauncher.core.domain.model.Game
import com.gamelauncher.core.domain.repository.GameRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GameRepositoryImpl(
    private val gameDao: GameDao
) : GameRepository {
    override fun getAllGames(): Flow<List<Game>> {
        return gameDao.getAllGames().map { entities -> entities.map(GameMapper::toDomain) }
    }

    override suspend fun getGameById(id: String): Game? {
        return gameDao.getGameById(id)?.let(GameMapper::toDomain)
    }

    override suspend fun insertGame(game: Game) {
        gameDao.insertGame(GameMapper.toEntity(game))
    }

    override suspend fun updateGame(game: Game) {
        gameDao.updateGame(GameMapper.toEntity(game))
    }

    override suspend fun deleteGame(game: Game) {
        gameDao.deleteGame(GameMapper.toEntity(game))
    }
}

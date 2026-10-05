package com.gamelauncher.core.data.repository

import com.gamelauncher.core.database.dao.GameDao
import com.gamelauncher.core.database.entity.GameEntity
import com.gamelauncher.core.domain.model.Game
import com.gamelauncher.core.domain.repository.GameRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GameRepositoryImpl(private val gameDao: GameDao) : GameRepository {
    override fun getAllGames(): Flow<List<Game>> {
        return gameDao.getAllGames().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getGameById(id: Long): Game? {
        return gameDao.getGameById(id)?.toDomain()
    }

    override suspend fun insertGame(game: Game): Long {
        return gameDao.insertGame(game.toEntity())
    }

    override suspend fun updateGame(game: Game) {
        gameDao.updateGame(game.toEntity())
    }

    override suspend fun deleteGame(id: Long) {
        gameDao.deleteGame(id)
    }

    private fun GameEntity.toDomain(): Game {
        return Game(
            id = id,
            title = title,
            packageName = packageName,
            launcherPath = launcherPath,
            iconPath = iconPath,
            category = category,
            isFavorite = isFavorite,
            playTimeMillis = playTimeMillis,
            lastPlayedAt = lastPlayedAt
        )
    }

    private fun Game.toEntity(): GameEntity {
        return GameEntity(
            id = id,
            title = title,
            packageName = packageName,
            launcherPath = launcherPath,
            iconPath = iconPath,
            category = category,
            isFavorite = isFavorite,
            playTimeMillis = playTimeMillis,
            lastPlayedAt = lastPlayedAt
        )
    }
}

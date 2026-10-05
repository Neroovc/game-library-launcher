package com.gamelauncher.core.data.repository

import com.gamelauncher.core.database.dao.GameDao
import com.gamelauncher.core.database.entity.GameEntity
import com.gamelauncher.core.domain.model.Game
import com.gamelauncher.core.domain.model.GameAvailability
import com.gamelauncher.core.domain.model.GameStatus
import com.gamelauncher.core.domain.repository.GameRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GameRepositoryImpl(private val gameDao: GameDao) : GameRepository {
    override fun getAllGames(): Flow<List<Game>> {
        return gameDao.getAllGames().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getGameById(id: String): Game? {
        return gameDao.getGameById(id)?.toDomain()
    }

    override suspend fun insertGame(game: Game) {
        gameDao.insertGame(game.toEntity())
    }

    override suspend fun updateGame(game: Game) {
        gameDao.updateGame(game.toEntity())
    }

    override suspend fun deleteGame(game: Game) {
        gameDao.deleteGame(game.toEntity())
    }

    private fun GameEntity.toDomain(): Game {
        return Game(
            id = id,
            title = title,
            originalTitle = originalTitle,
            description = description,
            developer = developer,
            publisher = publisher,
            version = version,
            releaseYear = releaseYear,
            favorite = favorite,
            hidden = hidden,
            rating = rating,
            notes = notes,
            status = GameStatus.valueOf(status),
            engine = engine,
            platform = platform,
            totalPlaytimeMs = totalPlaytimeMs,
            lastPlayedAt = lastPlayedAt,
            firstPlayedAt = firstPlayedAt,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    private fun Game.toEntity(): GameEntity {
        return GameEntity(
            id = id,
            title = title,
            originalTitle = originalTitle,
            description = description,
            developer = developer,
            publisher = publisher,
            version = version,
            releaseYear = releaseYear,
            favorite = favorite,
            hidden = hidden,
            rating = rating,
            notes = notes,
            status = status.name,
            engine = engine,
            platform = platform,
            totalPlaytimeMs = totalPlaytimeMs,
            lastPlayedAt = lastPlayedAt,
            firstPlayedAt = firstPlayedAt,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
}

package com.gamelauncher.core.database.mapper

import com.gamelauncher.core.database.entity.GameEntity
import com.gamelauncher.core.domain.model.Game
import com.gamelauncher.core.domain.model.GameStatus

object GameMapper {
    fun toDomain(entity: GameEntity): Game = Game(
        id = entity.id,
        title = entity.title,
        originalTitle = entity.originalTitle,
        description = entity.description,
        developer = entity.developer,
        publisher = entity.publisher,
        version = entity.version,
        releaseYear = entity.releaseYear,
        favorite = entity.favorite,
        hidden = entity.hidden,
        rating = entity.rating,
        notes = entity.notes,
        status = try { GameStatus.valueOf(entity.status) } catch (e: Exception) { GameStatus.PENDING },
        engine = entity.engine,
        platform = entity.platform,
        totalPlaytimeMs = entity.totalPlaytimeMs,
        lastPlayedAt = entity.lastPlayedAt,
        firstPlayedAt = entity.firstPlayedAt,
        createdAt = entity.createdAt,
        updatedAt = entity.updatedAt
    )

    fun toEntity(domain: Game): GameEntity = GameEntity(
        id = domain.id,
        title = domain.title,
        originalTitle = domain.originalTitle,
        description = domain.description,
        developer = domain.developer,
        publisher = domain.publisher,
        version = domain.version,
        releaseYear = domain.releaseYear,
        favorite = domain.favorite,
        hidden = domain.hidden,
        rating = domain.rating,
        notes = domain.notes,
        status = domain.status.name,
        engine = domain.engine,
        platform = domain.platform,
        totalPlaytimeMs = domain.totalPlaytimeMs,
        lastPlayedAt = domain.lastPlayedAt,
        firstPlayedAt = domain.firstPlayedAt,
        createdAt = domain.createdAt,
        updatedAt = domain.updatedAt
    )
}

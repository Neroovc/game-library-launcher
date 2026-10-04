package com.gamelauncher.core.domain.model

import java.util.UUID

enum class GameStatus {
    PENDING,
    PLAYING,
    COMPLETED,
    ABANDONED,
    ON_HOLD
}

enum class GameAvailability {
    PLAYABLE,
    CATALOG_ONLY,
    INSTALLATION_MISSING
}

data class Game(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val originalTitle: String = title,
    val description: String? = null,
    val developer: String? = null,
    val publisher: String? = null,
    val version: String? = null,
    val releaseYear: Int? = null,
    val favorite: Boolean = false,
    val hidden: Boolean = false,
    val rating: Int? = null,
    val notes: String? = null,
    val status: GameStatus = GameStatus.PENDING,
    val engine: String? = null,
    val platform: String? = null,
    val totalPlaytimeMs: Long = 0L,
    val lastPlayedAt: Long? = null,
    val firstPlayedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

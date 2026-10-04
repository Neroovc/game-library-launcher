package com.gamelauncher.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "games")
data class GameEntity(
    @PrimaryKey
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
    val status: String = "PENDING",
    val engine: String? = null,
    val platform: String? = null,
    val totalPlaytimeMs: Long = 0L,
    val lastPlayedAt: Long? = null,
    val firstPlayedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

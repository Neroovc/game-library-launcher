package com.gamelauncher.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "play_sessions",
    foreignKeys = [
        ForeignKey(
            entity = GameEntity::class,
            parentColumns = ["id"],
            childColumns = ["gameId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("gameId"), Index("installationId")]
)
data class PlaySessionEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val gameId: String,
    val installationId: String? = null,
    val startedAt: Long,
    val endedAt: Long? = null,
    val durationMs: Long = 0L,
    val terminationReason: String = "UNKNOWN",
    val source: String = "MANUAL"
)

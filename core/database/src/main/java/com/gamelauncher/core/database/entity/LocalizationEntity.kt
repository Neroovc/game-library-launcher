package com.gamelauncher.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "localizations",
    foreignKeys = [
        ForeignKey(
            entity = GameEntity::class,
            parentColumns = ["id"],
            childColumns = ["gameId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("gameId")]
)
data class LocalizationEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val gameId: String,
    val language: String,
    val kind: String = "UNKNOWN",
    val title: String? = null,
    val version: String? = null,
    val translator: String? = null,
    val sourceProvider: String? = null,
    val sourceUrl: String? = null,
    val notes: String? = null
)

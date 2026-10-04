package com.gamelauncher.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "external_ids",
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
data class ExternalIdEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val gameId: String,
    val providerId: String,
    val externalId: String
)

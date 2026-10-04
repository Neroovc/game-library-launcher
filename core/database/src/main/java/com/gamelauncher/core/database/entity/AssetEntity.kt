package com.gamelauncher.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "assets",
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
data class AssetEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val gameId: String,
    val type: String,
    val uri: String? = null,
    val sourceUrl: String? = null,
    val providerId: String? = null,
    val width: Int? = null,
    val height: Int? = null,
    val checksum: String? = null
)

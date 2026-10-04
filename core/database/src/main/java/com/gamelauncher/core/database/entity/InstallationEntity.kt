package com.gamelauncher.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "installations",
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
data class InstallationEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val gameId: String,
    val type: String = "UNKNOWN",
    val platform: String = "UNKNOWN",
    val state: String = "UNKNOWN",
    val launchAdapterId: String? = null,
    val runtimeId: String? = null,
    val path: String? = null,
    val uri: String? = null,
    val packageName: String? = null,
    val version: String? = null,
    val sizeBytes: Long? = null,
    val hash: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

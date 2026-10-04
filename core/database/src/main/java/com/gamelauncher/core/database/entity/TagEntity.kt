package com.gamelauncher.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "tags")
data class TagEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val sourceType: String = "PERSONAL",
    val providerId: String? = null,
    val confidence: Float = 1.0f,
    val isPersonal: Boolean = true
)

package com.gamelauncher.core.domain.model

import java.util.UUID

enum class TagSourceType {
    EXTERNAL,
    PERSONAL,
    SYSTEM
}

data class Tag(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val sourceType: TagSourceType = TagSourceType.PERSONAL,
    val providerId: String? = null,
    val confidence: Float = 1.0f,
    val isPersonal: Boolean = true
)

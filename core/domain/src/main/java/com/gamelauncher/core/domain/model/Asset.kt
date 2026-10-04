package com.gamelauncher.core.domain.model

import java.util.UUID

data class Asset(
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

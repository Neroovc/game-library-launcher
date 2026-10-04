package com.gamelauncher.core.domain.model

import java.util.UUID

data class ExternalId(
    val id: String = UUID.randomUUID().toString(),
    val gameId: String,
    val providerId: String,
    val externalId: String
)

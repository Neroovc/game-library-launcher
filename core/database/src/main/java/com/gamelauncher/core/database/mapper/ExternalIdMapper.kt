package com.gamelauncher.core.database.mapper

import com.gamelauncher.core.database.entity.ExternalIdEntity
import com.gamelauncher.core.domain.model.ExternalId

object ExternalIdMapper {
    fun toDomain(entity: ExternalIdEntity): ExternalId = ExternalId(
        id = entity.id,
        gameId = entity.gameId,
        providerId = entity.providerId,
        externalId = entity.externalId
    )

    fun toEntity(domain: ExternalId): ExternalIdEntity = ExternalIdEntity(
        id = domain.id,
        gameId = domain.gameId,
        providerId = domain.providerId,
        externalId = domain.externalId
    )
}

package com.gamelauncher.core.database.mapper

import com.gamelauncher.core.database.entity.AssetEntity
import com.gamelauncher.core.domain.model.Asset

object AssetMapper {
    fun toDomain(entity: AssetEntity): Asset = Asset(
        id = entity.id,
        gameId = entity.gameId,
        type = entity.type,
        uri = entity.uri,
        sourceUrl = entity.sourceUrl,
        providerId = entity.providerId,
        width = entity.width,
        height = entity.height,
        checksum = entity.checksum
    )

    fun toEntity(domain: Asset): AssetEntity = AssetEntity(
        id = domain.id,
        gameId = domain.gameId,
        type = domain.type,
        uri = domain.uri,
        sourceUrl = domain.sourceUrl,
        providerId = domain.providerId,
        width = domain.width,
        height = domain.height,
        checksum = domain.checksum
    )
}

package com.gamelauncher.core.database.mapper

import com.gamelauncher.core.database.entity.LocalizationEntity
import com.gamelauncher.core.domain.model.Localization
import com.gamelauncher.core.domain.model.LocalizationKind

object LocalizationMapper {
    fun toDomain(entity: LocalizationEntity): Localization = Localization(
        id = entity.id,
        gameId = entity.gameId,
        language = entity.language,
        kind = try { LocalizationKind.valueOf(entity.kind) } catch (e: Exception) { LocalizationKind.UNKNOWN },
        title = entity.title,
        version = entity.version,
        translator = entity.translator,
        sourceProvider = entity.sourceProvider,
        sourceUrl = entity.sourceUrl,
        notes = entity.notes
    )

    fun toEntity(domain: Localization): LocalizationEntity = LocalizationEntity(
        id = domain.id,
        gameId = domain.gameId,
        language = domain.language,
        kind = domain.kind.name,
        title = domain.title,
        version = domain.version,
        translator = domain.translator,
        sourceProvider = domain.sourceProvider,
        sourceUrl = domain.sourceUrl,
        notes = domain.notes
    )
}

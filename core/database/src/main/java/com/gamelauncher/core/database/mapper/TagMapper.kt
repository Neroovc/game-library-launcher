package com.gamelauncher.core.database.mapper

import com.gamelauncher.core.database.entity.TagEntity
import com.gamelauncher.core.domain.model.Tag
import com.gamelauncher.core.domain.model.TagSourceType

object TagMapper {
    fun toDomain(entity: TagEntity): Tag = Tag(
        id = entity.id,
        name = entity.name,
        sourceType = try { TagSourceType.valueOf(entity.sourceType) } catch (e: Exception) { TagSourceType.PERSONAL },
        providerId = entity.providerId,
        confidence = entity.confidence,
        isPersonal = entity.isPersonal
    )

    fun toEntity(domain: Tag): TagEntity = TagEntity(
        id = domain.id,
        name = domain.name,
        sourceType = domain.sourceType.name,
        providerId = domain.providerId,
        confidence = domain.confidence,
        isPersonal = domain.isPersonal
    )
}

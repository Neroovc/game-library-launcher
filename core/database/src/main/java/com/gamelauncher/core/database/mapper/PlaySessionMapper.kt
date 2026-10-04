package com.gamelauncher.core.database.mapper

import com.gamelauncher.core.database.entity.PlaySessionEntity
import com.gamelauncher.core.domain.model.PlaySession
import com.gamelauncher.core.domain.model.PlaySessionSource
import com.gamelauncher.core.domain.model.TerminationReason

object PlaySessionMapper {
    fun toDomain(entity: PlaySessionEntity): PlaySession = PlaySession(
        id = entity.id,
        gameId = entity.gameId,
        installationId = entity.installationId,
        startedAt = entity.startedAt,
        endedAt = entity.endedAt,
        durationMs = entity.durationMs,
        terminationReason = try { TerminationReason.valueOf(entity.terminationReason) } catch (e: Exception) { TerminationReason.UNKNOWN },
        source = try { PlaySessionSource.valueOf(entity.source) } catch (e: Exception) { PlaySessionSource.MANUAL }
    )

    fun toEntity(domain: PlaySession): PlaySessionEntity = PlaySessionEntity(
        id = domain.id,
        gameId = domain.gameId,
        installationId = domain.installationId,
        startedAt = domain.startedAt,
        endedAt = domain.endedAt,
        durationMs = domain.durationMs,
        terminationReason = domain.terminationReason.name,
        source = domain.source.name
    )
}

package com.gamelauncher.core.database.mapper

import com.gamelauncher.core.database.entity.InstallationEntity
import com.gamelauncher.core.domain.model.Installation
import com.gamelauncher.core.domain.model.InstallationState
import com.gamelauncher.core.domain.model.InstallationType
import com.gamelauncher.core.domain.model.Platform

object InstallationMapper {
    fun toDomain(entity: InstallationEntity): Installation = Installation(
        id = entity.id,
        gameId = entity.gameId,
        type = try { InstallationType.valueOf(entity.type) } catch (e: Exception) { InstallationType.UNKNOWN },
        platform = try { Platform.valueOf(entity.platform) } catch (e: Exception) { Platform.UNKNOWN },
        state = try { InstallationState.valueOf(entity.state) } catch (e: Exception) { InstallationState.UNKNOWN },
        launchAdapterId = entity.launchAdapterId,
        runtimeId = entity.runtimeId,
        path = entity.path,
        uri = entity.uri,
        packageName = entity.packageName,
        version = entity.version,
        sizeBytes = entity.sizeBytes,
        hash = entity.hash,
        createdAt = entity.createdAt,
        updatedAt = entity.updatedAt
    )

    fun toEntity(domain: Installation): InstallationEntity = InstallationEntity(
        id = domain.id,
        gameId = domain.gameId,
        type = domain.type.name,
        platform = domain.platform.name,
        state = domain.state.name,
        launchAdapterId = domain.launchAdapterId,
        runtimeId = domain.runtimeId,
        path = domain.path,
        uri = domain.uri,
        packageName = domain.packageName,
        version = domain.version,
        sizeBytes = domain.sizeBytes,
        hash = domain.hash,
        createdAt = domain.createdAt,
        updatedAt = domain.updatedAt
    )
}

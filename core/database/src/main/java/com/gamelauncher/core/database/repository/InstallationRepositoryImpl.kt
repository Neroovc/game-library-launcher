package com.gamelauncher.core.database.repository

import com.gamelauncher.core.database.dao.InstallationDao
import com.gamelauncher.core.database.mapper.InstallationMapper
import com.gamelauncher.core.domain.model.Installation
import com.gamelauncher.core.domain.repository.InstallationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class InstallationRepositoryImpl(
    private val installationDao: InstallationDao
) : InstallationRepository {
    override fun getInstallationsForGame(gameId: String): Flow<List<Installation>> {
        return installationDao.getInstallationsForGame(gameId).map { entities -> entities.map(InstallationMapper::toDomain) }
    }

    override suspend fun getInstallationById(id: String): Installation? {
        return installationDao.getInstallationById(id)?.let(InstallationMapper::toDomain)
    }

    override suspend fun insertInstallation(installation: Installation) {
        installationDao.insertInstallation(InstallationMapper.toEntity(installation))
    }

    override suspend fun updateInstallation(installation: Installation) {
        installationDao.updateInstallation(InstallationMapper.toEntity(installation))
    }

    override suspend fun deleteInstallation(installation: Installation) {
        installationDao.deleteInstallation(InstallationMapper.toEntity(installation))
    }
}

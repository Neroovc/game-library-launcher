package com.gamelauncher.core.domain.repository

import com.gamelauncher.core.domain.model.Installation
import kotlinx.coroutines.flow.Flow

interface InstallationRepository {
    fun getInstallationsForGame(gameId: String): Flow<List<Installation>>
    suspend fun getInstallationById(id: String): Installation?
    suspend fun insertInstallation(installation: Installation)
    suspend fun updateInstallation(installation: Installation)
    suspend fun deleteInstallation(installation: Installation)
}

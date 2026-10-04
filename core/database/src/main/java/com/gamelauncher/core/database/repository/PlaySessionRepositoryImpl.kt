package com.gamelauncher.core.database.repository

import com.gamelauncher.core.database.dao.PlaySessionDao
import com.gamelauncher.core.database.mapper.PlaySessionMapper
import com.gamelauncher.core.domain.model.PlaySession
import com.gamelauncher.core.domain.repository.PlaySessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PlaySessionRepositoryImpl(
    private val playSessionDao: PlaySessionDao
) : PlaySessionRepository {
    override fun getPlaySessionsForGame(gameId: String): Flow<List<PlaySession>> {
        return playSessionDao.getPlaySessionsForGame(gameId).map { entities -> entities.map(PlaySessionMapper::toDomain) }
    }

    override suspend fun getTotalPlaytimeForGame(gameId: String): Long {
        return playSessionDao.getTotalPlaytimeForGame(gameId) ?: 0L
    }

    override suspend fun insertPlaySession(session: PlaySession) {
        playSessionDao.insertPlaySession(PlaySessionMapper.toEntity(session))
    }

    override suspend fun updatePlaySession(session: PlaySession) {
        playSessionDao.updatePlaySession(PlaySessionMapper.toEntity(session))
    }

    override suspend fun deletePlaySession(session: PlaySession) {
        playSessionDao.deletePlaySession(PlaySessionMapper.toEntity(session))
    }
}

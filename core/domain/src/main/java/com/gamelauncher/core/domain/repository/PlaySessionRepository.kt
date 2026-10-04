package com.gamelauncher.core.domain.repository

import com.gamelauncher.core.domain.model.PlaySession
import kotlinx.coroutines.flow.Flow

interface PlaySessionRepository {
    fun getPlaySessionsForGame(gameId: String): Flow<List<PlaySession>>
    suspend fun getTotalPlaytimeForGame(gameId: String): Long
    suspend fun insertPlaySession(session: PlaySession)
    suspend fun updatePlaySession(session: PlaySession)
    suspend fun deletePlaySession(session: PlaySession)
}

package com.gamelauncher.core.domain.usecase

import com.gamelauncher.core.domain.model.Game
import com.gamelauncher.core.domain.repository.GameRepository
import kotlinx.coroutines.flow.Flow

class GetGamesUseCase(private val repository: GameRepository) {
    operator fun invoke(): Flow<List<Game>> = repository.getAllGames()
}

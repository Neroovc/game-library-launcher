package com.gamelauncher.core.domain.usecase

import com.gamelauncher.core.domain.model.Game
import com.gamelauncher.core.domain.repository.GameRepository

class UpdateGameUseCase(private val repository: GameRepository) {
    suspend operator fun invoke(game: Game) {
        repository.updateGame(game)
    }
}

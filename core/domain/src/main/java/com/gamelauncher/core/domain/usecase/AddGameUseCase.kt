package com.gamelauncher.core.domain.usecase

import com.gamelauncher.core.domain.model.Game
import com.gamelauncher.core.domain.repository.GameRepository

class AddGameUseCase(private val repository: GameRepository) {
    suspend operator fun invoke(game: Game) {
        repository.insertGame(game)
    }
}

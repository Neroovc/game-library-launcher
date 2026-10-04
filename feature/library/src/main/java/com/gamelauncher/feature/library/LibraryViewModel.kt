package com.gamelauncher.feature.library

import androidx.lifecycle.ViewModel
import com.gamelauncher.core.domain.repository.GameRepository
import kotlinx.coroutines.flow.Flow
import com.gamelauncher.core.domain.model.Game

class LibraryViewModel(private val repository: GameRepository) : ViewModel() {
    val games: Flow<List<Game>> = repository.getAllGames()
}

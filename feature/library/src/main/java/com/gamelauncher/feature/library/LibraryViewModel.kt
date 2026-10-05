package com.gamelauncher.feature.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.gamelauncher.core.domain.model.Game
import com.gamelauncher.core.domain.repository.GameRepository
import kotlinx.coroutines.flow.Flow

class LibraryViewModel(repository: GameRepository) : ViewModel() {
    val games: Flow<List<Game>> = repository.getAllGames()
}

class LibraryViewModelFactory(private val repository: GameRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LibraryViewModel::class.java)) {
            return LibraryViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

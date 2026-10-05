package com.gamelauncher.core.data.di

import com.gamelauncher.core.data.repository.GameRepositoryImpl
import com.gamelauncher.core.database.dao.GameDao
import com.gamelauncher.core.domain.repository.GameRepository

object DataModule {
    fun provideGameRepository(gameDao: GameDao): GameRepository {
        return GameRepositoryImpl(gameDao)
    }
}

package com.gamelauncher.app.di

import android.content.Context
import androidx.room.Room
import com.gamelauncher.core.database.AppDatabase
import com.gamelauncher.core.database.repository.GameRepositoryImpl
import com.gamelauncher.core.domain.repository.GameRepository

object DatabaseModule {
    fun provideDatabase(context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        ).build()
    }

    fun provideGameRepository(db: AppDatabase): GameRepository {
        return GameRepositoryImpl(db.gameDao())
    }
}

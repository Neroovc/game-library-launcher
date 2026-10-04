package com.gamelauncher.core.database

import com.gamelauncher.core.database.entity.GameEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class GameEntityTest {
    @Test
    fun createsGameWithDefaults() {
        val game = GameEntity(title = "Test Game")
        assertNotNull(game.id)
        assertEquals("Test Game", game.title)
        assertEquals("Test Game", game.originalTitle)
        assertEquals(0L, game.totalPlaytimeMs)
    }
}

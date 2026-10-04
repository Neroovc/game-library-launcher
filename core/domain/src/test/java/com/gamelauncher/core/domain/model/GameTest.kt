package com.gamelauncher.core.domain.model

import org.junit.Test
import org.junit.Assert.*

class GameTest {
    @Test
    fun game_has_default_status_pending() {
        val game = Game(title = "Test")
        assertEquals(GameStatus.PENDING, game.status)
    }

    @Test
    fun game_has_unique_id() {
        val game = Game(title = "Test")
        assertNotNull(game.id)
        assertTrue(game.id.isNotEmpty())
    }
}

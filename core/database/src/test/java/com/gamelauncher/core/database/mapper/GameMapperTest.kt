package com.gamelauncher.core.database.mapper

import com.gamelauncher.core.database.entity.GameEntity
import com.gamelauncher.core.domain.model.Game
import com.gamelauncher.core.domain.model.GameStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class GameMapperTest {
    @Test
    fun maps_entity_to_domain() {
        val entity = GameEntity(title = "Test Game", status = "PLAYING")
        val domain = GameMapper.toDomain(entity)
        assertEquals("Test Game", domain.title)
        assertEquals(GameStatus.PLAYING, domain.status)
    }

    @Test
    fun maps_domain_to_entity() {
        val domain = Game(title = "Test Game", status = GameStatus.COMPLETED)
        val entity = GameMapper.toEntity(domain)
        assertEquals("Test Game", entity.title)
        assertEquals("COMPLETED", entity.status)
    }
}

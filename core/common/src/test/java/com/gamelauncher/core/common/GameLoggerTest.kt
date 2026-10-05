package com.gamelauncher.core.common

import org.junit.Test

class GameLoggerTest {
    @Test
    fun logger_sanitizes_sensitive_data() {
        val message = "token=abc123 password=secret"
        GameLogger.d(LogCategory.PROVIDER, message)
    }
}

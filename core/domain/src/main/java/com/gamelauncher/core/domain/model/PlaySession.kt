package com.gamelauncher.core.domain.model

import java.util.UUID

enum class TerminationReason {
    USER_STOPPED,
    APP_BACKGROUND,
    GAME_PROCESS_ENDED,
    ACTIVITY_CLOSED,
    CRASH,
    UNKNOWN
}

enum class PlaySessionSource {
    LAUNCHED_BY_APP,
    IMPORTED,
    MANUAL
}

data class PlaySession(
    val id: String = UUID.randomUUID().toString(),
    val gameId: String,
    val installationId: String? = null,
    val startedAt: Long,
    val endedAt: Long? = null,
    val durationMs: Long = 0L,
    val terminationReason: TerminationReason = TerminationReason.UNKNOWN,
    val source: PlaySessionSource = PlaySessionSource.MANUAL
)

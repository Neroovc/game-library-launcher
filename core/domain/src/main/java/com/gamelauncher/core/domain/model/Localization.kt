package com.gamelauncher.core.domain.model

import java.util.UUID

enum class LocalizationKind {
    ORIGINAL,
    OFFICIAL_TRANSLATION,
    FAN_TRANSLATION,
    MACHINE_TRANSLATION,
    UNKNOWN
}

data class Localization(
    val id: String = UUID.randomUUID().toString(),
    val gameId: String,
    val language: String,
    val kind: LocalizationKind = LocalizationKind.UNKNOWN,
    val title: String? = null,
    val version: String? = null,
    val translator: String? = null,
    val sourceProvider: String? = null,
    val sourceUrl: String? = null,
    val notes: String? = null
)

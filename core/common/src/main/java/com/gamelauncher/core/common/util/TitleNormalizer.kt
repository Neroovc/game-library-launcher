package com.gamelauncher.core.common.util

object TitleNormalizer {
    fun normalize(title: String): String {
        return title.trim().lowercase()
    }
}

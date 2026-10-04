package com.gamelauncher.core.common.util

object RjCanonicalizer {
    fun canonicalize(rj: String): String {
        val trimmed = rj.trim()
        val match = Regex("rj[0-9]+", RegexOption.IGNORE_CASE).find(trimmed)
        return match?.value?.uppercase() ?: trimmed.uppercase()
    }
}

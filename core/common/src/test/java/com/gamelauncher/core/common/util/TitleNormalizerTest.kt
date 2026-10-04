package com.gamelauncher.core.common.util

import org.junit.Assert.assertEquals
import org.junit.Test

class TitleNormalizerTest {
    @Test
    fun normalizes_title() {
        assertEquals("test game", TitleNormalizer.normalize(" Test Game "))
    }
}

package com.gamelauncher.core.common.util

import org.junit.Assert.assertEquals
import org.junit.Test

class RjCanonicalizerTest {
    @Test
    fun canonicalizes_rj() {
        assertEquals("RJ0145678", RjCanonicalizer.canonicalize("rj0145678"))
        assertEquals("RJ0145678", RjCanonicalizer.canonicalize("RJ-0145678"))
    }
}

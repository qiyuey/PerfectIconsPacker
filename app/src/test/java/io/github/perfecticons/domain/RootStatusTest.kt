package io.github.perfecticons.domain

import io.github.perfecticons.data.install.rootGranted
import org.junit.Assert.*
import org.junit.Test

class RootStatusTest {
    @Test fun `accepts successful uid zero including shell whitespace`() {
        assertTrue(rootGranted(0, listOf("0")))
        assertTrue(rootGranted(0, listOf(" 0 ", "")))
    }
    @Test fun `failed command never grants root even if stdout contains zero`() {
        assertFalse(rootGranted(1, listOf("0")))
        assertFalse(rootGranted(-1, emptyList()))
    }
    @Test fun `ordinary shell and missing uid are not root`() {
        assertFalse(rootGranted(0, listOf("2000")))
        assertFalse(rootGranted(0, emptyList()))
        assertFalse(rootGranted(0, listOf("permission denied")))
    }
    @Test fun `ambiguous and unrelated replies are not mistaken for a root uid`() {
        assertFalse(rootGranted(0, listOf("0", "2000")))
        assertFalse(rootGranted(0, listOf("uid=0")))
        assertFalse(rootGranted(0, listOf("version 0")))
    }
}

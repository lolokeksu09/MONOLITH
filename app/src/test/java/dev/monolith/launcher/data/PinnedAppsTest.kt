package dev.monolith.launcher.data

import org.junit.Assert.assertEquals
import org.junit.Test

class PinnedAppsTest {
    @Test fun pinAddsAppInOrder() {
        assertEquals(listOf("a", "b"), togglePinned(listOf("a"), "b"))
    }
    @Test fun unpinRemovesApp() {
        assertEquals(listOf("b"), togglePinned(listOf("a", "b"), "a"))
    }
    @Test fun pinDoesNotMutateInput() {
        val original = listOf("a")
        togglePinned(original, "b")
        assertEquals(listOf("a"), original)
    }
}

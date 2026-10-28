package dev.monolith.launcher.data

import org.junit.Assert.assertEquals
import org.junit.Test

class LauncherBehaviorTest {
    @Test fun reorderMovesEarlierAndLater() {
        val pins = listOf("a", "b", "c")
        assertEquals(listOf("b", "a", "c"), movePinned(pins, "b", -1))
        assertEquals(listOf("a", "c", "b"), movePinned(pins, "b", 1))
    }

    @Test fun reorderDoesNotMutateOriginal() {
        val pins = listOf("a", "b")
        movePinned(pins, "b", -1)
        assertEquals(listOf("a", "b"), pins)
    }

    @Test fun reorderRejectsMissingAndOutOfRange() {
        val pins = listOf("a", "b")
        assertEquals(pins, movePinned(pins, "missing", 1))
        assertEquals(pins, movePinned(pins, "a", -1))
        assertEquals(pins, movePinned(pins, "b", 1))
        assertEquals(pins, movePinned(pins, "a", 99))
    }

    @Test fun columnsAreBounded() {
        assertEquals(3, normalizeColumns(-1))
        assertEquals(4, normalizeColumns(4))
        assertEquals(6, normalizeColumns(100))
    }

    @Test fun drawerOpacityNeverGoesBelowReadableMinimumOrAboveOpaque() {
        assertEquals(25, normalizeDrawerOpacity(-500))
        assertEquals(25, normalizeDrawerOpacity(0))
        assertEquals(65, normalizeDrawerOpacity(65))
        assertEquals(100, normalizeDrawerOpacity(999))
    }

    @Test fun toggledPinsPreserveOrder() {
        assertEquals(listOf("a", "c"), togglePinned(listOf("a"), "c"))
        assertEquals(listOf("c"), togglePinned(listOf("a", "c"), "a"))
    }
}

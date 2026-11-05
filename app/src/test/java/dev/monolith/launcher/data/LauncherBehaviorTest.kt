package dev.monolith.launcher.data

import org.junit.Assert.assertEquals
import org.junit.Test

class LauncherBehaviorTest {
    private fun layoutOf(vararg ids: String) = HomeLayout(ids.map { HomeEntry.App(it) })

    @Test fun reorderMovesEarlierAndLater() {
        val layout = layoutOf("a", "b", "c")
        assertEquals(listOf("b", "a", "c"), reorderHome(layout, 1, 0).appIds)
        assertEquals(listOf("a", "c", "b"), reorderHome(layout, 1, 2).appIds)
    }

    @Test fun reorderDoesNotMutateOriginal() {
        val layout = layoutOf("a", "b")
        reorderHome(layout, 1, 0)
        assertEquals(listOf("a", "b"), layout.appIds)
    }

    @Test fun reorderRejectsMissingAndOutOfRange() {
        val layout = layoutOf("a", "b")
        assertEquals(layout, reorderHome(layout, -1, 0))
        assertEquals(layout, reorderHome(layout, 0, -1))
        assertEquals(layout, reorderHome(layout, 1, 2))
        assertEquals(layout, reorderHome(layout, 0, 99))
        assertEquals(layout, reorderHome(layout, 0, 0))
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

    @Test fun pinAndUnpinPreserveOrder() {
        val one = layoutOf("a")
        val two = pinToHome(one, "c")
        assertEquals(listOf("a", "c"), two.appIds)
        assertEquals(listOf("c"), unpinFromHome(two, "a").appIds)
        assertEquals(two, pinToHome(two, "c"))
        assertEquals(listOf("a"), one.appIds)
    }
}

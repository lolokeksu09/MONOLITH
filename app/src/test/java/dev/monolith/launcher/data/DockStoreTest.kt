package dev.monolith.launcher.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class DockStoreTest {
    @Test fun addHonorsCapacityAndUniqueEntries() {
        val ids = (1..5).map { "pkg/.App$it" }
        val full = ids.fold(emptyList<String>(), ::addDockApp)
        assertEquals(ids, full)
        assertSame(full, addDockApp(full, "pkg/.App6"))
        assertSame(full, addDockApp(full, ids[0]))
        assertEquals(emptyList<String>(), addDockApp(emptyList(), ""))
    }

    @Test fun removeAndMoveKeepOrder() {
        val ids = listOf("one", "two", "three")
        assertEquals(listOf("two", "one", "three"), moveDockApp(ids, 0, 1))
        assertEquals(listOf("one", "three"), removeDockApp(ids, "two"))
        assertSame(ids, moveDockApp(ids, -1, 1))
        assertSame(ids, moveDockApp(ids, 0, 10))
    }

    @Test fun normalizeDeduplicatesAndBoundsCorruptSavedIds() {
        val raw = listOf("", "one", "one", "two", "three", "four", "five", "six")
        assertEquals(listOf("one", "two", "three", "four", "five"), normalizeDock(raw))
    }

    @Test fun missingInstallationDoesNotRemoveDockAssociation() {
        val dock = listOf("installed/.A", "temporarily-missing/.B")
        assertEquals(dock, normalizeDock(dock))
    }
}

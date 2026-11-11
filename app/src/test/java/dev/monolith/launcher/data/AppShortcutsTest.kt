package dev.monolith.launcher.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AppShortcutsTest {
    @Test fun exposesOnlyUniqueActionsForSelectedAppSortedByRank() {
        val list = listOf(
            AppShortcut("pkg.alpha", "new", "New note", 9),
            AppShortcut("pkg.beta", "other", "Wrong package", 0),
            AppShortcut("pkg.alpha", "create", "Create", 1),
            AppShortcut("pkg.alpha", "new", "Duplicate", 0),
            AppShortcut("pkg.alpha", "", "No identifier", 2),
            AppShortcut("pkg.alpha", "empty", " ", 2),
            AppShortcut("pkg.alpha", "camera", "Camera", 3),
            AppShortcut("pkg.alpha", "share", "Share", 4),
            AppShortcut("pkg.alpha", "settings", "Settings", 5)
        )
        assertEquals(listOf("create", "camera", "share", "settings"),
            normalizeAppShortcuts("pkg.alpha", list).map { it.id })
    }

    @Test fun appWithNoPublishedShortcutsKeepsNormalActionsAvailable() {
        assertTrue(normalizeAppShortcuts("pkg.alpha", emptyList()).isEmpty())
        assertTrue(normalizeAppShortcuts("pkg.alpha", listOf(
            AppShortcut("other", "id", "Not ours", 0)
        )).isEmpty())
    }
}

package dev.monolith.launcher.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class HomeLayoutTest {
    private val initial = HomeLayout(listOf(HomeEntry.App("a"), HomeEntry.App("b"), HomeEntry.App("c")))

    @Test fun pinIsIdempotent() {
        assertSame(initial, pinToHome(initial, "a"))
        assertEquals(listOf("a", "b", "c", "d"), pinToHome(initial, "d").appIds)
    }
    @Test fun creatingFolderPreservesAppAndPosition() {
        val folded = createHomeFolder(initial, "b", "f", "Games")
        assertEquals(listOf("a", "b", "c"), folded.appIds)
        assertEquals("folder:f", folded.entries[1].key)
    }
    @Test fun moveToFolderCannotDuplicateApp() {
        val folded = createHomeFolder(initial, "a", "f", "Games")
        val moved = addAppToFolder(folded, "b", "f")
        assertEquals(listOf("a", "b", "c"), moved.appIds)
        assertEquals(listOf("a", "b"), (moved.entries[0] as HomeEntry.Folder).appIds)
        assertEquals(moved, addAppToFolder(moved, "b", "f"))
    }
    @Test fun removeFolderRestoresContents() {
        val folded = addAppToFolder(createHomeFolder(initial, "a", "f", "Games"), "b", "f")
        assertEquals(initial, removeHomeFolder(folded, "f"))
    }
    @Test fun unpinInsideFolderPreservesOthers() {
        val folded = addAppToFolder(createHomeFolder(initial, "a", "f", "Games"), "b", "f")
        assertEquals(listOf("b", "c"), unpinFromHome(folded, "a").appIds)
    }
    @Test fun reorderMaintainsEntriesAndIgnoresBadIndices() {
        assertEquals(listOf("b", "c", "a"), reorderHome(initial, 0, 2).appIds)
        assertEquals(initial, reorderHome(initial, -1, 1))
        assertEquals(initial, reorderHome(initial, 0, 99))
    }
    @Test fun renameRejectsBlankAndTrims() {
        val folded = createHomeFolder(initial, "a", "f", "Games")
        assertEquals(folded, renameHomeFolder(folded, "f", "   "))
        assertEquals("Work", (renameHomeFolder(folded, "f", "  Work  ").entries[0] as HomeEntry.Folder).name)
    }
    @Test fun moveOutOfFolderRetainsAppOnDesktop() {
        val folded = addAppToFolder(createHomeFolder(initial, "a", "f", "Games"), "b", "f")
        val moved = moveAppOutOfFolder(folded, "b", "f")
        assertEquals(listOf("a"), (moved.entries.first() as HomeEntry.Folder).appIds)
        assertEquals(listOf("a", "c", "b"), moved.appIds)
    }
    @Test fun moveOutOfMissingFolderIsNoOp() {
        assertEquals(initial, moveAppOutOfFolder(initial, "a", "missing"))
    }

    @Test fun widgetIdsIgnoreInvalidAndDuplicates() {
        assertEquals(listOf(7, 2), parseWidgetIds("7,broken,7,-1,2,0"))
    }
}

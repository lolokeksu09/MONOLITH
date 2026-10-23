package studio.nox.model

import org.junit.Assert.*
import org.junit.Test

class EditHistoryTest {
    private fun scene(seed: Int = 1) = SceneConfig("HALO", seed=seed)

    @Test fun undoRedoReturnExactSnapshots() {
        val history = EditHistory.start(scene()).record(scene(2)).record(scene(3))
        assertEquals(3, history.current.seed)
        assertEquals(2, history.undo().current.seed)
        assertEquals(1, history.undo().undo().current.seed)
        assertEquals(3, history.undo().redo().current.seed)
        assertFalse(history.undo().undo().canUndo)
    }

    @Test fun undoThenNewEditClearsRedoBranch() {
        val history=EditHistory.start(scene()).record(scene(2)).record(scene(3)).undo().record(scene(4))
        assertFalse(history.canRedo)
        assertEquals(4,history.current.seed)
        assertEquals(2,history.undo().current.seed)
    }

    @Test fun duplicateEditDoesNotFillHistoryOrClearRedo() {
        val h=EditHistory.start(scene()).record(scene(2)).undo()
        assertEquals(h,h.record(scene()))
        assertTrue(h.record(scene()).canRedo)
    }

    @Test fun historyBoundedToFortyAndCanSetLowerLimit() {
        var h=EditHistory.start(scene(), maxEntries=5)
        for (i in 2..100) h=h.record(scene(i))
        assertEquals(5,h.past.size)
        repeat(5){h=h.undo()}
        assertEquals(94,h.current.seed)
        assertFalse(h.canUndo)
        assertTrue(h.canRedo)
    }

    @Test fun previewNotCommittedUntilFinish() {
        val original=EditHistory.start(scene())
        val draft=scene().copy(speed=0.91f)
        assertFalse(original.canUndo)
        val committed=original.record(draft)
        assertTrue(committed.canUndo)
        assertEquals(scene().speed,committed.undo().current.speed,0f)
    }
}

package dev.lolokeksu.contextcapsule.domain

import org.junit.Assert.*
import org.junit.Test

class WorkspaceV9Test {
    private val items = listOf(
        ProjectTask("a", "p", "Тест сборки", false, 3),
        ProjectTask("b", "p", "Релиз", true, 2),
        ProjectTask("c", "p", "Проверить APK", false, 1)
    )

    @Test fun tasksHaveDistinctOpenAndCompletedViews() {
        assertEquals(listOf("c", "a"), WorkspaceV9.tasks(items, TaskScope.OPEN).map { it.id })
        assertEquals(listOf("b"), WorkspaceV9.tasks(items, TaskScope.COMPLETED).map { it.id })
        assertEquals(3, WorkspaceV9.tasks(items, TaskScope.ALL).size)
    }

    @Test fun projectSummaryReflectsLocalFactsOnly() {
        val s = WorkspaceV9.summary(items, listOf(
            Decision("n", "p", "Note", EvidenceKind.UNVERIFIED, 1, true),
            Decision("ok", "p", "Confirmed", EvidenceKind.OWNER_CONFIRMED, 2)
        ))
        assertEquals(2, s.openTasks)
        assertEquals(1, s.completedTasks)
        assertEquals(1, s.confirmedNotes)
        assertEquals(1, s.pendingNotes)
        assertEquals(1, s.pinnedNotes)
        assertEquals(33, s.progressPercent)
    }

    @Test fun compactHandoffIncludesCriticalContextNotCompletedTaskNoise() {
        val project = Project("p", "Проект", "user/repo", 1)
        val input = CapsuleInput(project,
            listOf(Decision("a", "p", "Одобрено", EvidenceKind.OWNER_CONFIRMED, 1),
                   Decision("b", "p", "Гипотеза", EvidenceKind.UNVERIFIED, 2)),
            items, null)
        val brief = CapsuleComposer().toMarkdown(input, compact = true)
        assertTrue(brief.contains("Одобрено"))
        assertTrue(brief.contains("Гипотеза"))
        assertTrue(brief.contains("Проверить APK"))
        assertFalse(brief.contains("Релиз"))
        assertTrue(brief.length < CapsuleComposer().toMarkdown(input).length)
        assertTrue(brief.contains("UNKNOWN"))
    }
}

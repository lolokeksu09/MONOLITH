package dev.lolokeksu.contextcapsule.domain

import org.junit.Assert.*
import org.junit.Test

class UnifiedSearchTest {
    private val projects = listOf(
        Project("a", "Основной", "user/main", 1L),
        Project("b", "Архив", null, 2L, archived = true)
    )
    private val notes = listOf(
        Decision("n1", "a", "Проверить сборку APK", EvidenceKind.UNVERIFIED, 2L, pinned = true),
        Decision("n2", "a", "Решение по дизайну", EvidenceKind.OWNER_CONFIRMED, 1L),
        Decision("n3", "b", "APK в архиве", EvidenceKind.UNVERIFIED, 3L)
    )
    private val tasks = listOf(
        ProjectTask("t1", "a", "Подготовить APK", false, 4L),
        ProjectTask("t2", "a", "Завершить релиз", true, 5L)
    )

    @Test fun searchUsesCyrillicCaseInsensitiveTextAndCrossesProjects() {
        val results = UnifiedSearch.find(projects, notes, tasks, "апк", SearchCategory.ALL, false)
        assertEquals(listOf("t1", "n1"), results.map { it.recordId })
        assertEquals(listOf(SearchCategory.TASKS, SearchCategory.NOTES), results.map { it.category })
        assertEquals("Основной", results.first().projectTitle)
    }

    @Test fun archivedDataRequiresExplicitOptIn() {
        assertFalse(UnifiedSearch.find(projects, notes, tasks, "APK", SearchCategory.ALL, false)
            .any { it.projectId == "b" })
        assertTrue(UnifiedSearch.find(projects, notes, tasks, "APK", SearchCategory.ALL, true)
            .any { it.projectId == "b" })
    }

    @Test fun blankQueryAndOrphanedDataAreNeverReturned() {
        assertTrue(UnifiedSearch.find(projects, notes, tasks, "  ", SearchCategory.ALL, true).isEmpty())
        val orphan = Decision("other", "missing", "APK orphan", EvidenceKind.UNKNOWN, 7L)
        assertFalse(UnifiedSearch.find(projects, notes + orphan, tasks, "apk", SearchCategory.ALL, true)
            .any { it.recordId == "other" })
    }

    @Test fun categoryFiltersAndResultLimitsAreDeterministic() {
        val notesOnly = UnifiedSearch.find(projects, notes, tasks, "apk", SearchCategory.NOTES, true)
        assertTrue(notesOnly.all { it.category == SearchCategory.NOTES })
        val tasksOnly = UnifiedSearch.find(projects, notes, tasks, "apk", SearchCategory.TASKS, true)
        assertEquals(listOf("t1"), tasksOnly.map { it.recordId })
        assertEquals(1, UnifiedSearch.find(projects, notes, tasks, "apk", SearchCategory.ALL, true, 1).size)
    }
}

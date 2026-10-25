package dev.lolokeksu.contextcapsule.domain

import org.junit.Assert.*
import org.junit.Test

class ProjectCatalogTest {
    private val records = listOf(
        Project("1", "Первый", "user/first", 1L, false),
        Project("2", "Закрытый", "user/closed", 2L, true),
        Project("3", "Другой", null, 3L, false)
    )

    @Test fun archivedProjectsHiddenFromDefaultWorkspaceButRecoverable() {
        assertEquals(listOf("3", "1"), ProjectCatalog.visible(records, false, "").map { it.id })
        assertEquals(listOf("2"), ProjectCatalog.visible(records, true, "").map { it.id })
        assertEquals(listOf("2"), ProjectCatalog.visible(records, true, "закр").map { it.id })
    }

    @Test fun editingRepositoryRequiresSnapshotInvalidationOnlyOnActualChange() {
        assertFalse(ProjectCatalog.repositoryChanged("user/repo", "user/repo"))
        assertTrue(ProjectCatalog.repositoryChanged("user/repo", "other/repo"))
        assertTrue(ProjectCatalog.repositoryChanged("user/repo", null))
        assertFalse(ProjectCatalog.repositoryChanged(null, null))
    }

    @Test fun deletionRequiresExactTitleNotBlankOrPartial() {
        assertFalse(ProjectCatalog.confirmDelete("My Project", "My"))
        assertFalse(ProjectCatalog.confirmDelete("My Project", "my project"))
        assertFalse(ProjectCatalog.confirmDelete("My Project", ""))
        assertTrue(ProjectCatalog.confirmDelete("My Project", "My Project"))
    }
}

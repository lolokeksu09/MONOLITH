package dev.lolokeksu.contextcapsule.domain

import org.junit.Assert.*
import org.junit.Test

class WorkspaceToolsTest {
    private val projects = listOf(
        Project("1", "WrCommunity", "lolokeksu09/WrCommunity", 1),
        Project("2", "NOX wallpapers", null, 2),
        Project("3", "Freshuck", "company/freshuck", 3)
    )

    @Test fun searchFindsProjectsByTitleOrRepositoryWithoutCaseSensitivity() {
        assertEquals(listOf("1"), WorkspaceTools.filterProjects(projects, "wrcomm").map { it.id })
        assertEquals(listOf("3"), WorkspaceTools.filterProjects(projects, "COMPANY/").map { it.id })
        assertEquals(3, WorkspaceTools.filterProjects(projects, " ").size)
    }

    @Test fun pendingFilterKeepsOnlyUnconfirmedEvidenceAndSearchesText() {
        val input = listOf(
            Decision("1","p","Release approved",EvidenceKind.OWNER_CONFIRMED, 1),
            Decision("2","p","Release may be broken",EvidenceKind.UNVERIFIED, 2),
            Decision("3","p","Need to verify",EvidenceKind.UNKNOWN,3)
        )
        assertEquals(listOf("2"), WorkspaceTools.filterDecisions(input, "release", true).map { it.id })
        assertEquals(2, WorkspaceTools.filterDecisions(input, "", true).size)
        assertEquals(3, WorkspaceTools.filterDecisions(input, "", false).size)
    }

    @Test fun pinnedNotesAppearFirstWithoutChangingTrustStatus() {
        val decisions = listOf(
            Decision("old", "project", "Old", EvidenceKind.OWNER_CONFIRMED, 1),
            Decision("pin", "project", "Pinned", EvidenceKind.UNVERIFIED, 2, pinned = true)
        )
        val ordered = WorkspaceTools.filterDecisions(decisions, "", false)
        assertEquals(listOf("pin", "old"), ordered.map { it.id })
        assertEquals(EvidenceKind.UNVERIFIED, ordered.first().evidence)
    }

    @Test fun editedConfirmedTextMustReturnToUnverified() {
        assertEquals(EvidenceKind.UNVERIFIED,
            WorkspaceTools.evidenceAfterEdit(EvidenceKind.OWNER_CONFIRMED, "old", "new"))
        assertEquals(EvidenceKind.OWNER_CONFIRMED,
            WorkspaceTools.evidenceAfterEdit(EvidenceKind.OWNER_CONFIRMED, "old", "old"))
    }

    @Test fun incomingSharedTextIsTrimmedButNeverSilentlyTruncated() {
        assertEquals("Remember this", WorkspaceTools.normalizeSharedText("  Remember this  "))
        assertNull(WorkspaceTools.normalizeSharedText("   "))
        assertNull(WorkspaceTools.normalizeSharedText(null))
        assertNull(WorkspaceTools.normalizeSharedText("a".repeat(20_001)))
        assertEquals(20_000, WorkspaceTools.normalizeSharedText("a".repeat(20_000))?.length)
    }
}

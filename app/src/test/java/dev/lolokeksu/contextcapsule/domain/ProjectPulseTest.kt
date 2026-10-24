package dev.lolokeksu.contextcapsule.domain

import org.junit.Assert.*
import org.junit.Test

class ProjectPulseTest {
    @Test fun firstSyncCannotBeReportedAsNewCommits() {
        val pulse = ProjectPulse.compare(null, null, "abcdef", "main")
        assertEquals("first", pulse.code)
        assertFalse(pulse.hasNewCommits)
    }
    @Test fun comparisonDetectsHeadChangeAndPreservesBothRevisions() {
        val pulse = ProjectPulse.compare("aabbcc", "main", "ddeeff", "main")
        assertEquals("changed", pulse.code)
        assertTrue(pulse.hasNewCommits)
        assertTrue(pulse.headline.contains("измен"))
        assertEquals("aabbcc", pulse.oldSha)
        assertEquals("ddeeff", pulse.newSha)
    }
    @Test fun sameShaMeansNoNewCommitsOnComparedBranchOnly() {
        val pulse = ProjectPulse.compare("abcd", "main", "abcd", "main")
        assertEquals("same", pulse.code)
        assertFalse(pulse.hasNewCommits)
        assertTrue(pulse.headline.contains("HEAD"))
    }
    @Test fun differentBranchesAreNotComparedAsChanges() {
        val pulse = ProjectPulse.compare("a", "develop", "b", "main")
        assertEquals("different_branch", pulse.code)
        assertFalse(pulse.hasNewCommits)
    }
    @Test fun suggestionsAreGroundedAndNeverGeneratedForUnknownCounts() {
        assertEquals(emptyList<String>(), ProjectPulse.suggestions(null, null, null))
        assertEquals(listOf("Проверить ошибки последнего CI", "Разобрать открытые PR", "Просмотреть открытые Issues"),
            ProjectPulse.suggestions("failure", 2, 1))
        assertEquals(emptyList<String>(), ProjectPulse.suggestions("success", 0, 0))
        assertEquals(listOf("Проверить текущий CI"), ProjectPulse.suggestions("in_progress", 0, 0))
    }
}

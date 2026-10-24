package dev.lolokeksu.contextcapsule.domain

import org.junit.Assert.*
import org.junit.Test

class CommitComparisonTest {
    @Test fun aheadShowsNewCommitsWithoutEquatingFilesToTotal() {
        val value = CommitComparison.explain("ahead", 7, 0, 7, 5, false)
        assertEquals("Новые коммиты: 7", value.headline)
        assertEquals("5", value.filesLabel)
        assertFalse(value.warning)
    }

    @Test fun divergenceMustNotClaimAllCommitsAreNew() {
        val value = CommitComparison.explain("diverged", 3, 2, 3, 2, false)
        assertTrue(value.headline.contains("разошл"))
        assertTrue(value.warning)
    }

    @Test fun rewindDoesNotClaimCommitsWereAdded() {
        val value = CommitComparison.explain("behind", 0, 4, 0, 0, false)
        assertFalse(value.headline.contains("Новые коммиты"))
        assertTrue(value.warning)
    }

    @Test fun unknownAndTruncatedRemainQualified() {
        assertTrue(CommitComparison.explain("wat", 0, 0, 0, 300, true).warning)
        assertTrue(CommitComparison.explain("ahead", 1, 0, 1, 300, true).warning)
    }

    @Test fun missingCompareReturnsUnknownNotSuccess() {
        assertEquals("Недоступно", CommitComparison.missingLabel())
    }
}

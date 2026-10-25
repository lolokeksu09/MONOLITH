package dev.lolokeksu.contextcapsule.domain

import org.junit.Assert.*
import org.junit.Test

class RepositoryAssessmentTest {
    @Test fun failedCIIsActionableNotHealthy() {
        val assessment = RepositoryAssessment.assess("failure", 2, 4, 3)
        assertEquals("needs_attention", assessment.code)
        assertTrue(assessment.headline.contains("CI"))
        assertEquals(2, assessment.openPRs)
    }
    @Test fun successfulCINotClaimedAsFeatureCompletion() {
        val assessment = RepositoryAssessment.assess("success", 0, 0, 1)
        assertEquals("ci_passed", assessment.code)
        assertFalse(assessment.headline.contains("готов"))
    }
    @Test fun missingOrPartialSourcesRemainUnknown() {
        val assessment = RepositoryAssessment.assess(null, null, null, null)
        assertEquals("unknown", assessment.code)
        assertNull(assessment.openIssues)
        assertTrue(assessment.headline.contains("нет данных"))
    }
    @Test fun ongoingCIIsNotReportedAsSuccess() {
        assertEquals("in_progress", RepositoryAssessment.assess("in_progress", 1, 0, 2).code)
    }
}

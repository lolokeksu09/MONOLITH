package dev.lolokeksu.contextcapsule.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertNull
import org.junit.Test

class ProjectInputTest {
    @Test fun acceptsLocalProjectWithoutGitHub() {
        val draft = ProjectInput.parse("  Мой проект  ", "")
        assertEquals("Мой проект", draft.title)
        assertNull(draft.repository)
    }

    @Test fun acceptsPublicGitHubLinkAndNormalizesToSlug() {
        assertEquals(
            "lolokeksu09/WrCommunity",
            ProjectInput.parse("WrCommunity", "https://github.com/lolokeksu09/WrCommunity/").repository
        )
        assertEquals(
            "lolokeksu09/WrCommunity",
            ProjectInput.parse("WrCommunity", "lolokeksu09/WrCommunity").repository
        )
    }

    @Test fun rejectsNonGitHubDomainsAndInvalidProjectNames() {
        assertThrows(IllegalArgumentException::class.java) {
            ProjectInput.parse("Project", "https://evil.example/lolokeksu09/WrCommunity")
        }
        assertThrows(IllegalArgumentException::class.java) {
            ProjectInput.parse("   ", "")
        }
    }
}

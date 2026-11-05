package dev.monolith.launcher.data

import org.junit.Assert.assertEquals
import org.junit.Test

class ReferenceFavoritesTest {
    private fun app(id: String) = LaunchableApp(id, id, "example", "Activity")

    @Test fun dockThenPinnedTakePriorityWithoutDuplicates() {
        val installed = listOf(app("a"), app("b"), app("c"), app("d"))
        val chosen = chooseReferenceApps(installed, listOf("c", "a"), listOf("a", "b"))
        assertEquals(listOf("c", "a", "b", "d"), chosen.map { it.id })
    }

    @Test fun hiddenUnpinnedAppsNeverLeakIntoOrbit() {
        val chosen = chooseReferenceApps(
            listOf(app("hidden"), app("favorite"), app("other")),
            listOf("favorite"), emptyList(), setOf("hidden")
        )
        assertEquals(listOf("favorite", "other"), chosen.map { it.id })
    }

    @Test fun missingAppsAndLimitAreHandled() {
        val chosen = chooseReferenceApps(
            listOf(app("a"), app("b"), app("c")),
            listOf("not-installed", "c"), listOf("b"), limit = 2
        )
        assertEquals(listOf("c", "b"), chosen.map { it.id })
    }
}

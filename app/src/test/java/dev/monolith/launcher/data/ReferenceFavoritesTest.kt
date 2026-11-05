package dev.monolith.launcher.data

import org.junit.Assert.assertEquals
import org.junit.Test

class ReferenceFavoritesTest {
    private fun app(id: String) = LaunchableApp(id, id, "example", "Activity")

    @Test fun orbitUsesRealLaunchActivityStats() {
        val installed = listOf(app("a"), app("b"), app("c"))
        val stats = mapOf(
            "b" to LaunchStats(count = 3, lastOpened = 300),
            "a" to LaunchStats(count = 9, lastOpened = 200)
        )
        assertEquals(listOf("a", "b", "c"),
            chooseReferenceApps(installed, stats = stats).map { it.id })
    }

    @Test fun hiddenAppsDoNotAppearInOrbitOrQuickAccess() {
        assertEquals(listOf("visible"),
            chooseReferenceApps(listOf(app("hidden"), app("visible")), hiddenIds = setOf("hidden")).map { it.id })
    }

    @Test fun emptyAndLimitsAreSafe() {
        assertEquals(emptyList<LaunchableApp>(), chooseReferenceApps(emptyList()))
        assertEquals(emptyList<LaunchableApp>(), chooseReferenceApps(listOf(app("a")), limit = 0))
        assertEquals(listOf("a"), chooseReferenceApps(listOf(app("a"), app("b")), limit = 1).map { it.id })
    }
}

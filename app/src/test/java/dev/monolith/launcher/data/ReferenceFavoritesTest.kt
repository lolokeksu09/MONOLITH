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

    @Test fun editorialPrefersRealLastOpenedInsteadOfOrbitFavoritesOrLaunchCount() {
        val installed = listOf(app("alpha"), app("beta"), app("gamma"))
        val stats = mapOf(
            "alpha" to LaunchStats(count = 200, lastOpened = 10L),
            "beta" to LaunchStats(count = 1, lastOpened = 90L),
            "gamma" to LaunchStats(count = 30, lastOpened = 60L)
        )
        assertEquals(listOf("beta", "gamma", "alpha"),
            chooseRecentlyOpenedApps(installed, stats = stats).map { it.id })
        // Saved Orbit favorites remain independent and unchanged.
        assertEquals("alpha", chooseReferenceApps(
            installed, stats = stats, favoriteIds = listOf("alpha")
        ).first().id)
    }

    @Test fun editorialNeverLabelsUnknownOrUninstalledAppAsRecent() {
        val installed = listOf(app("alpha"), app("beta"), app("beta"))
        val stats = mapOf(
            "alpha" to LaunchStats(count = 1, lastOpened = 100L),
            "beta" to LaunchStats(count = 3, lastOpened = 300L),
            "uninstalled" to LaunchStats(count = 8, lastOpened = 500L)
        )
        assertEquals(listOf("alpha"), chooseRecentlyOpenedApps(
            installed, hiddenIds = setOf("beta"), stats = stats
        ).map { it.id })
        assertEquals(listOf("beta", "alpha"),
            chooseRecentlyOpenedApps(installed, stats = stats).map { it.id })
    }

    @Test fun editorialWithoutLocalHistoryUsesFallbackAndRespectsLimit() {
        val installed = (1..12).map { app("app-$it") }
        assertEquals(emptyList<LaunchableApp>(), chooseRecentlyOpenedApps(installed))
        assertEquals(emptyList<LaunchableApp>(), chooseRecentlyOpenedApps(
            installed, stats = mapOf("app-1" to LaunchStats(count = 4, lastOpened = 0L))
        ))
        val stats = installed.associate { it.id to LaunchStats(1, it.id.removePrefix("app-").toLong()) }
        assertEquals(8, chooseRecentlyOpenedApps(installed, stats = stats, limit = 99).size)
        assertEquals(emptyList<LaunchableApp>(),
            chooseRecentlyOpenedApps(installed, stats = stats, limit = -3))
    }

    @Test fun emptyAndLimitsAreSafe() {
        assertEquals(emptyList<LaunchableApp>(), chooseReferenceApps(emptyList()))
        assertEquals(emptyList<LaunchableApp>(), chooseReferenceApps(listOf(app("a")), limit = 0))
        assertEquals(listOf("a"), chooseReferenceApps(listOf(app("a"), app("b")), limit = 1).map { it.id })
    }
}

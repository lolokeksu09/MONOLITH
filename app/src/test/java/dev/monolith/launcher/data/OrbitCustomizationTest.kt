package dev.monolith.launcher.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.hypot

class OrbitCustomizationTest {
    private fun app(id: String) = LaunchableApp(id, id, "example", "Activity")

    @Test fun selectedAppsLeadInSavedOrderAndFallbackUsesLaunchStats() {
        val all = listOf(app("a"), app("b"), app("c"), app("d"))
        val stats = mapOf("d" to LaunchStats(7, 3), "a" to LaunchStats(2, 1))
        val chosen = chooseReferenceApps(
            all, stats = stats, favoriteIds = listOf("c", "b")
        )
        assertEquals(listOf("c", "b", "d", "a"), chosen.map { it.id })
    }

    @Test fun hiddenOrUninstalledFavoritesNeverBecomeShortcuts() {
        val chosen = chooseReferenceApps(
            listOf(app("a"), app("b"), app("c")),
            hiddenIds = setOf("b"),
            favoriteIds = listOf("missing", "b", "c", "c")
        )
        assertEquals(listOf("c", "a"), chosen.map { it.id })
    }

    @Test fun favoritesAreUniqueBoundedAndReorderWithoutMutation() {
        val names = (1..30).map { "app-$it" } + listOf("", "app-1")
        val normalized = normalizeOrbitFavorites(names)
        assertEquals(15, normalized.size)
        assertEquals("app-1", normalized.first())
        assertEquals(listOf("beta", "alpha"), moveOrbitFavorite(listOf("alpha", "beta"), "beta", -1))
        assertEquals(listOf("alpha", "beta"), moveOrbitFavorite(listOf("alpha", "beta"), "beta", 1))
    }

    @Test fun orbitCoordinatesHaveRoomForRealTouchTargets() {
        val points = orbitPositions(15)
        assertEquals(15, points.size)
        for (i in points.indices) {
            assertTrue(points[i].x in -100f..100f)
            assertTrue(points[i].y in -135f..135f)
            for (j in (i+1) until points.size) {
                assertTrue("icons overlap", hypot(points[i].x-points[j].x, points[i].y-points[j].y) >= 51f)
            }
        }
        assertTrue(orbitPositions(0).isEmpty())
        assertEquals(15, orbitPositions(99).size)
    }

    @Test fun collapsedOrbitHasNoAppClickTargets() {
        assertFalse(orbitHitTargetsEnabled(false))
        assertTrue(orbitHitTargetsEnabled(true))
    }

    @Test fun movingOrbitWaitsUntilTouchTargetsAreSeparated() {
        assertFalse(orbitHitTargetsEnabled(true, progress = 0f))
        assertFalse(orbitHitTargetsEnabled(true, progress = .89f))
        assertTrue(orbitHitTargetsEnabled(true, progress = .90f))
        assertFalse(orbitHitTargetsEnabled(false, progress = 1f))
    }
}

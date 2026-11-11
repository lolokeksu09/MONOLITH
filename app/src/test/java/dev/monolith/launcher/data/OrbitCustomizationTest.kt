package dev.monolith.launcher.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs
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

    @Test fun everyDensityHasRoomForRealTouchTargets() {
        listOf(8, 12, 15).forEach { density ->
            val points = orbitPositions(density)
            assertEquals(density, points.size)
            for (i in points.indices) {
                assertTrue(points[i].x in -100f..100f)
                assertTrue(points[i].y in -135f..135f)
                for (j in (i+1) until points.size) {
                    assertTrue(
                        "icons overlap at density $density",
                        hypot(points[i].x-points[j].x, points[i].y-points[j].y) >= 51f
                    )
                }
            }
        }
        assertTrue(orbitPositions(0).isEmpty())
        assertEquals(15, orbitPositions(99).size)
    }

    @Test fun partiallyPopulatedOrbitsStayCenteredAndTouchable() {
        (1..15).forEach { count ->
            val points = orbitPositions(count)
            assertEquals(count, points.size)
            val centerX = points.map { it.x.toDouble() }.average()
            val centerY = points.map { it.y.toDouble() }.average()
            assertTrue("orbit $count is horizontally shifted", abs(centerX) <= 5.0)
            assertTrue("orbit $count is vertically shifted", abs(centerY) <= 5.0)
            for (i in points.indices) {
                assertTrue("orbit $count exceeds width", points[i].x in -100f..100f)
                assertTrue("orbit $count exceeds height", points[i].y in -135f..135f)
                for (j in i + 1 until points.size) {
                    assertTrue(
                        "overlapping touch targets for count $count",
                        hypot(points[i].x - points[j].x, points[i].y - points[j].y) >= 51f
                    )
                }
            }
        }
        assertEquals(OrbitPoint(0f, 0f), orbitPositions(1).single())
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

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
            assertTrue(
                "orbit $count is not mirrored around its vertical axis",
                points.all { point -> points.any { it.x == -point.x && it.y == point.y } }
            )
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

    @Test fun orbitDragSelectsOnlyInsideAnExpandedIconHitTarget() {
        for (count in listOf(1, 8, 12, 15)) {
            val points = orbitPositions(count)
            points.forEachIndexed { index, point ->
                assertEquals(
                    "incorrect drag target for count $count, icon $index",
                    index, orbitDragTarget(count, point.x, point.y, 1f, 1f, true)
                )
                assertEquals(
                    "incorrect scaled drag target for count $count, icon $index",
                    index, orbitDragTarget(count, point.x * .92f, point.y * .92f,
                        .92f, 1f, true)
                )
            }
        }
        assertEquals(null, orbitDragTarget(0, 0f, 0f, 1f, 1f, true))
    }

    @Test fun orbitDragRejectsUnintentionalOrIncompleteSelections() {
        // The 20dp selection radius keeps a gap between neighboring icons.

        assertEquals(null, orbitDragTarget(2, 0f, 0f, 1f, 1f, true))
        assertEquals(null, orbitDragTarget(8, 250f, 250f, 1f, 1f, true))
        assertEquals(null, orbitDragTarget(8, -58f, -87f, 1f, .89f, true))
        assertEquals(null, orbitDragTarget(8, -58f, -87f, 1f, 1f, false))
        assertEquals(null, orbitDragTarget(8, Float.NaN, 0f, 1f, 1f, true))
        assertEquals(null, orbitDragTarget(8, 0f, Float.POSITIVE_INFINITY, 1f, 1f, true))
        assertEquals(null, orbitDragTarget(8, 0f, 0f, 0f, 1f, true))
        assertEquals(null, orbitDragTarget(8, 0f, 0f, 1f, 1f, true, -1f))
        assertEquals(null, orbitDragTarget(8, 0f, 0f, 1f, 1f, true, Float.NaN))
    }

    @Test fun adjacentOrbitDragTargetsNeverOverlapAtMinimumScaleAndProgress() {
        val scale = .92f
        val progress = .90f
        for (count in 2..15) {
            val points = orbitPositions(count)
            for (i in points.indices) for (j in (i + 1) until points.size) {
                val midpoint = OrbitPoint(
                    (points[i].x + points[j].x) / 2f,
                    (points[i].y + points[j].y) / 2f
                )
                val result = orbitDragTarget(
                    count,
                    midpoint.x * scale * progress,
                    midpoint.y * scale * progress,
                    scale, progress, true
                )
                // Only neighbor pairs that are within 48dp in rendered space
                // could produce overlapping gesture zones; their midpoint
                // must never select either app.
                val distance = hypot(
                    (points[i].x - points[j].x) * scale * progress,
                    (points[i].y - points[j].y) * scale * progress
                )
                if (distance < 48f) {
                    assertEquals("overlapping drag zones for $count apps ($i, $j)",
                        null, result)
                }
            }
        }
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

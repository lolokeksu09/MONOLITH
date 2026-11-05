package dev.monolith.launcher.data

import org.junit.Assert.assertEquals
import org.junit.Test

class InkPreferencesTest {
    @Test fun upgradesAndUnknownValuesKeepOriginalAppearance() {
        assertEquals(AppIconStyle.ORIGINAL, parseAppIconStyle(null))
        assertEquals(AppIconStyle.ORIGINAL, parseAppIconStyle(""))
        assertEquals(AppIconStyle.ORIGINAL, parseAppIconStyle("unexpected"))
        assertEquals(AppIconStyle.MONO, parseAppIconStyle("MONO"))
        assertEquals(AppIconStyle.INK, parseAppIconStyle("INK"))
    }

    @Test fun orbitDensityAcceptsOnlyDesignedLayouts() {
        assertEquals(15, normalizeOrbitDensity(-1))
        assertEquals(8, normalizeOrbitDensity(8))
        assertEquals(12, normalizeOrbitDensity(12))
        assertEquals(15, normalizeOrbitDensity(15))
        assertEquals(15, normalizeOrbitDensity(14))
    }

    @Test fun reducingVisibleDensityDoesNotMutateSavedFavorites() {
        val favorites = (1..15).map { "app-$it" }
        val apps = favorites.map { LaunchableApp(it, it, "example", "Activity") }
        val visible = chooseReferenceApps(
            apps = apps,
            limit = 8,
            favoriteIds = favorites
        )
        assertEquals(8, visible.size)
        assertEquals(15, favorites.size)
        assertEquals(favorites.take(8), visible.map { it.id })
    }
}

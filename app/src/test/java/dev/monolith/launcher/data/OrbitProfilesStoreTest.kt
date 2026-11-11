package dev.monolith.launcher.data

import android.content.Context
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class OrbitProfilesStoreTest {
    private val context get() = RuntimeEnvironment.getApplication()

    private fun reset() {
        context.getSharedPreferences("monolith_orbit", Context.MODE_PRIVATE).edit().clear().commit()
        context.getSharedPreferences("monolith_orbit_profiles", Context.MODE_PRIVATE)
            .edit().clear().commit()
    }

    @Test fun upgradePreservesOriginalMainFavoritesWithoutCopyOrDeletion() {
        reset()
        OrbitFavoritesStore(context).write(listOf("one/A", "two/B"))
        val store = OrbitProfilesStore(context)
        assertEquals(OrbitProfile.MAIN, store.readActive())
        assertEquals(listOf("one/A", "two/B"), store.readFavorites(OrbitProfile.MAIN))
        assertTrue(store.readFavorites(OrbitProfile.WORK).isEmpty())
        assertTrue(store.readFavorites(OrbitProfile.GAMES).isEmpty())

        store.writeFavorites(OrbitProfile.WORK, listOf("work/W"))
        store.writeActive(OrbitProfile.WORK)
        val restored = OrbitProfilesStore(context)
        assertEquals(OrbitProfile.WORK, restored.readActive())
        assertEquals(listOf("work/W"), restored.readFavorites(restored.readActive()))
        assertEquals(listOf("one/A", "two/B"), OrbitFavoritesStore(context).read())
    }

    @Test fun profilesStayIndependentAndNormalizeWithoutTouchingLegacy() {
        reset()
        val store = OrbitProfilesStore(context)
        val oversized = (1..28).map { "pkg/$it" } + listOf("pkg/1", "")
        store.writeFavorites(OrbitProfile.WORK, oversized)
        store.writeFavorites(OrbitProfile.GAMES, listOf("game/G", "game/G", ""))
        store.writeFavorites(OrbitProfile.MAIN, listOf("main/M"))
        assertEquals(15, store.readFavorites(OrbitProfile.WORK).size)
        assertEquals(listOf("game/G"), store.readFavorites(OrbitProfile.GAMES))
        assertEquals(listOf("main/M"), OrbitFavoritesStore(context).read())
    }

    @Test fun malformedProfileStorageFallsBackWithoutClearingMain() {
        reset()
        OrbitFavoritesStore(context).write(listOf("safe/S"))
        val prefs = context.getSharedPreferences("monolith_orbit_profiles", Context.MODE_PRIVATE)
        prefs.edit().putString("active_profile", "BAD")
            .putString("favorites_work", "not-json")
            .putString("favorites_games", "[123]").commit()
        val restored = OrbitProfilesStore(context)
        assertEquals(OrbitProfile.MAIN, restored.readActive())
        assertEquals(emptyList<String>(), restored.readFavorites(OrbitProfile.WORK))
        assertEquals(emptyList<String>(), restored.readFavorites(OrbitProfile.GAMES))
        assertEquals(listOf("safe/S"), restored.readFavorites(OrbitProfile.MAIN))
    }
}

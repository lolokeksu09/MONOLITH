package dev.monolith.launcher

import android.content.Context
import dev.monolith.launcher.data.LaunchStats
import dev.monolith.launcher.data.LaunchStatsStore
import dev.monolith.launcher.data.LaunchableApp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/**
 * P5 checks the real persisted history plus the refresh version boundary.
 * These run on Robolectric; they do not simulate OEM HOME-role interactions.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class MainActivityRefreshTest {
    private val alpha = LaunchableApp("p/A", "Alpha", "p", "A")
    private val beta = LaunchableApp("p/B", "Beta", "p", "B")

    @Test fun storedLaunchHistorySurvivesReloadAndClearRemovesIt() {
        val context = RuntimeEnvironment.getApplication()
        context.getSharedPreferences(
            "monolith_launch_stats", Context.MODE_PRIVATE
        ).edit().clear().commit()
        val store = LaunchStatsStore(context)
        try {
            assertEquals(0L, store.read(listOf(alpha))[alpha.id]?.count)
            assertEquals(1L, store.record(alpha.id, at = 100L).count)
            assertEquals(2L, store.record(alpha.id, at = 200L).count)
            val reloaded = LaunchStatsStore(context).read(listOf(alpha, beta))
            assertEquals(LaunchStats(2L, 200L), reloaded[alpha.id])
            assertEquals(LaunchStats(0L, 0L), reloaded[beta.id])
            store.clear()
            assertEquals(LaunchStats(0L, 0L), store.read(listOf(alpha))[alpha.id])
        } finally {
            store.clear()
        }
    }

    @Test fun concurrentRefreshCannotUndoHistoryClear() {
        val stale = mapOf(alpha.id to LaunchStats(5L, 100L))
        val cleared = emptyMap<String, LaunchStats>()
        assertEquals(cleared, statsAfterCatalogRefresh(cleared, stale, 7L, 8L))
    }

    @Test fun concurrentRefreshCannotUndoNewLaunch() {
        val old = mapOf(alpha.id to LaunchStats(1L, 100L))
        val current = mapOf(alpha.id to LaunchStats(2L, 200L))
        assertEquals(current, statsAfterCatalogRefresh(current, old, 10L, 11L))
        assertEquals(old, statsAfterCatalogRefresh(current, old, 11L, 11L))
    }

    @Test fun freshRefreshCanPopulateInstalledAppStats() {
        val persisted = mapOf(beta.id to LaunchStats(3L, 800L))
        assertEquals(persisted, statsAfterCatalogRefresh(
            emptyMap(), persisted, 0L, 0L
        ))
        assertTrue(statsAfterCatalogRefresh(
            emptyMap(), emptyMap(), 0L, 0L
        ).isEmpty())
    }
}

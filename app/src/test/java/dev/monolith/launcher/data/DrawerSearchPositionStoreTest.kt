package dev.monolith.launcher.data

import android.content.Context
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/** Real SharedPreferences read/write, not just enum parsing. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class DrawerSearchPositionStoreTest {
    @Test
    fun optInBottomSurvivesReinstantiationWithoutChangingOtherSettings() {
        val context = RuntimeEnvironment.getApplication()
        val raw = context.getSharedPreferences("monolith_settings", Context.MODE_PRIVATE)
        raw.edit().clear().commit()

        val store = LauncherSettingsStore(context)
        assertEquals(DrawerSearchPosition.TOP, store.read().drawerSearchPosition)

        store.write(LauncherPreferences(
            drawerColumns = 6,
            showDrawerSearch = true,
            appSort = AppSort.FREQUENT,
            appIconStyle = AppIconStyle.MONO,
            orbitDensity = 8,
            drawerSearchPosition = DrawerSearchPosition.BOTTOM
        ))
        val restored = LauncherSettingsStore(context).read()
        assertEquals(DrawerSearchPosition.BOTTOM, restored.drawerSearchPosition)
        assertEquals(6, restored.drawerColumns)
        assertTrue(restored.showDrawerSearch)
        assertEquals(AppSort.FREQUENT, restored.appSort)
        assertEquals(AppIconStyle.MONO, restored.appIconStyle)
        assertEquals(8, restored.orbitDensity)

        raw.edit().putString("drawer_search_position", "unknown-value").commit()
        assertEquals(DrawerSearchPosition.TOP, LauncherSettingsStore(context).read().drawerSearchPosition)
    }

    @Test
    fun disabledDrawerSearchRemainsDisabledWithBottomPositionSaved() {
        val context = RuntimeEnvironment.getApplication()
        val raw = context.getSharedPreferences("monolith_settings", Context.MODE_PRIVATE)
        raw.edit().clear().commit()
        LauncherSettingsStore(context).write(LauncherPreferences(
            showDrawerSearch = false,
            drawerSearchPosition = DrawerSearchPosition.BOTTOM
        ))
        val stored = LauncherSettingsStore(context).read()
        assertFalse(stored.showDrawerSearch)
        assertEquals(DrawerSearchPosition.BOTTOM, stored.drawerSearchPosition)
        assertEquals("", effectiveDrawerQuery("stale", stored.showDrawerSearch))
    }
}

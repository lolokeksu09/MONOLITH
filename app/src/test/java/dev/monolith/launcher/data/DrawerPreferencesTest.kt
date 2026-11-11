package dev.monolith.launcher.data

import org.junit.Assert.assertEquals
import org.junit.Test

class DrawerPreferencesTest {
    @Test fun searchPositionKeepsExistingUpgradesOnTop() {
        assertEquals(DrawerSearchPosition.TOP, LauncherPreferences().drawerSearchPosition)
        assertEquals(DrawerSearchPosition.TOP, parseDrawerSearchPosition(null))
        assertEquals(DrawerSearchPosition.TOP, parseDrawerSearchPosition(""))
        assertEquals(DrawerSearchPosition.TOP, parseDrawerSearchPosition("unrecognized"))
        assertEquals(DrawerSearchPosition.TOP, parseDrawerSearchPosition("TOP"))
        assertEquals(DrawerSearchPosition.BOTTOM, parseDrawerSearchPosition("BOTTOM"))
    }

    @Test fun columnsStayWithinVisibleGridLimits() {
        assertEquals(3, normalizeColumns(-5))
        assertEquals(4, normalizeColumns(4))
        assertEquals(6, normalizeColumns(100))
    }

    @Test fun disabledSearchLeavesNoInvisibleQuery() {
        assertEquals("", effectiveDrawerQuery("secret", false))
        assertEquals("test", effectiveDrawerQuery("test", true))
    }
}

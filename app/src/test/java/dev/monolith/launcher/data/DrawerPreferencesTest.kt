package dev.monolith.launcher.data

import org.junit.Assert.assertEquals
import org.junit.Test

class DrawerPreferencesTest {
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

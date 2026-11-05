package dev.monolith.launcher.data

import org.junit.Assert.assertEquals
import org.junit.Test

class LegacyPinnedMigrationTest {
    @Test fun retainsLegacyPinOrder() {
        assertEquals(listOf("mail", "camera", "maps"), parseLegacyPinnedApps("mail|camera|maps"))
    }

    @Test fun removesDuplicatesWithoutReordering() {
        assertEquals(listOf("camera", "mail"), parseLegacyPinnedApps("camera|mail|camera"))
    }

    @Test fun missingOrEmptyPreferenceProducesNoPins() {
        assertEquals(emptyList<String>(), parseLegacyPinnedApps(null))
        assertEquals(emptyList<String>(), parseLegacyPinnedApps(""))
    }
}

package dev.monolith.launcher.data

import org.junit.Assert.assertEquals
import org.junit.Test

class AppCatalogTest {
    private val apps = listOf(
        LaunchableApp("a", "Camera", "com.example.camera", "A"),
        LaunchableApp("b", "Settings", "com.example.settings", "B"),
        LaunchableApp("c", "Камера", "com.example.ru", "C")
    )

    @Test fun emptyQueryReturnsEverything() {
        assertEquals(apps, filterApps(apps, "  "))
    }

    @Test fun matchesLabelWithoutCase() {
        assertEquals(listOf(apps[0]), filterApps(apps, "CAM"))
    }

    @Test fun matchesPackageName() {
        assertEquals(listOf(apps[1]), filterApps(apps, "EXAMPLE.SETTINGS"))
    }

    @Test fun matchesCyrillicLabel() {
        assertEquals(listOf(apps[2]), filterApps(apps, "КАМ"))
    }

    @Test fun noResults() {
        assertEquals(emptyList<LaunchableApp>(), filterApps(apps, "absent"))
    }
}

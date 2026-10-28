package dev.monolith.launcher.data

import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Test

class AppSortingTest {
    private fun app(name: String, id: String = name, installed: Long = 0L) =
        LaunchableApp(id, name, "pkg", "Activity", installed)

    private val apps = listOf(app("Бета"), app("Альфа"), app("Яндекс"))

    @Test fun alphabeticSortIsLocaleAwareAndReversible() {
        assertEquals(listOf("Альфа", "Бета", "Яндекс"),
            sortApps(apps, AppSort.A_Z, locale = Locale("ru")).map { it.label })
        assertEquals(listOf("Яндекс", "Бета", "Альфа"),
            sortApps(apps, AppSort.Z_A, locale = Locale("ru")).map { it.label })
    }

    @Test fun frequencySortUsesOnlyTrackedLaunches() {
        val stats = mapOf(
            "Бета" to LaunchStats(5, 100),
            "Альфа" to LaunchStats(2, 900),
            "Яндекс" to LaunchStats(5, 500)
        )
        assertEquals(listOf("Яндекс", "Бета", "Альфа"),
            sortApps(apps, AppSort.FREQUENT, stats, Locale("ru")).map { it.label })
        assertEquals(listOf("Альфа", "Яндекс", "Бета"),
            sortApps(apps, AppSort.LAST_OPENED, stats, Locale("ru")).map { it.label })
    }

    @Test fun installedOrderUsesFirstInstallTimestampAndUnknownLast() {
        val values = listOf(app("Old", installed=100), app("New", installed=900), app("Unknown"))
        assertEquals(listOf("New","Old","Unknown"),
            sortApps(values, AppSort.INSTALLED_NEWEST).map { it.label })
        assertEquals(listOf("Old","New","Unknown"),
            sortApps(values, AppSort.INSTALLED_OLDEST).map { it.label })
    }

    @Test fun tiesFallBackToAlphabeticalOrder() {
        val values = listOf(app("Beta"), app("Alpha"), app("Gamma"))
        assertEquals(listOf("Alpha","Beta","Gamma"),
            sortApps(values, AppSort.FREQUENT).map { it.label })
    }

    @Test fun unexpectedSavedSortFallsBackToAZ() {
        assertEquals(AppSort.A_Z, parseAppSort(null))
        assertEquals(AppSort.A_Z, parseAppSort("INVALID"))
        assertEquals(AppSort.INSTALLED_NEWEST, parseAppSort("INSTALLED_NEWEST"))
    }
}

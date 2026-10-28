package dev.monolith.launcher.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class HiddenAppsTest {
    private fun app(id: String) = LaunchableApp(id, id, "package", "Activity")

    @Test fun hideRemovesOnlyFromDrawerWithoutDeletingSourceList() {
        val apps = listOf(app("one"), app("two"), app("three"))
        val hidden = hideApp(emptySet(), "two")
        assertEquals(listOf("one", "three"), filterHiddenApps(apps, hidden).map { it.id })
        assertEquals(3, apps.size)
        assertEquals(listOf("one", "two", "three"),
            filterHiddenApps(apps, unhideApp(hidden, "two")).map { it.id })
    }

    @Test fun duplicateHideAndUnhideAreIdempotent() {
        val hidden = setOf("one")
        assertEquals(hidden, hideApp(hidden, "one"))
        assertEquals(hidden, unhideApp(hidden, "another"))
        assertTrue(unhideApp(hidden, "one").isEmpty())
        assertFalse(hideApp(emptySet(), "").isNotEmpty())
    }

    @Test fun emptyHiddenSetReturnsOriginalList() {
        val apps = listOf(app("one"))
        assertSame(apps, filterHiddenApps(apps, emptySet()))
    }

    @Test fun failedCatalogRefreshMustKeepExistingApps() {
        val apps = listOf(app("installed"))
        val failure = Result.failure<List<LaunchableApp>>(IllegalStateException("PackageManager error"))
        assertSame(apps, retainCatalogOnError(apps, failure))
        assertTrue(retainCatalogOnError(apps, Result.success(emptyList())).isEmpty())
    }
}

package dev.monolith.launcher.data

import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

/** The UI never exposes stale projections after search, hide or sort changes. */
class DrawerProjectionTest {
    private val alpha = LaunchableApp("alpha/A", "Альфа", "com.alpha", "A")
    private val beta = LaunchableApp("beta/B", "Бета", "com.beta", "B")
    private val gamma = LaunchableApp("gamma/C", "Гамма", "com.gamma", "C")

    private fun request(
        query: String = "",
        hidden: Set<String> = emptySet(),
        searchEnabled: Boolean = true,
        sort: AppSort = AppSort.A_Z
    ) = DrawerProjectionRequest(
        apps = listOf(gamma, beta, alpha),
        hiddenIds = hidden,
        settledQuery = query,
        searchEnabled = searchEnabled,
        sort = sort,
        stats = emptyMap(),
        locale = Locale.forLanguageTag("ru-RU")
    )

    @Test fun usesExistingFilterSortAndHiddenAppRules() {
        val all = request()
        assertEquals(listOf("Альфа", "Бета", "Гамма"),
            projectDrawerApps(all).map { it.label })

        assertEquals(listOf(beta), projectDrawerApps(request(query = "com.beta")))
        assertEquals(emptyList<LaunchableApp>(),
            projectDrawerApps(request(query = "com.beta", hidden = setOf(beta.id))))
        assertEquals(3,
            projectDrawerApps(request(query = "no-match", searchEnabled = false)).size)
        assertEquals(listOf("Гамма", "Бета", "Альфа"),
            projectDrawerApps(request(sort = AppSort.Z_A)).map { it.label })
    }

    @Test fun staleResultsCannotExposeRecentlyHiddenApps() {
        val before = request()
        val completed = DrawerProjectionResult(before, projectDrawerApps(before))
        assertEquals(3, matchingDrawerApps(before, completed)?.size)

        val after = request(hidden = setOf(beta.id))
        assertNull("Old projection must never show a newly hidden app",
            matchingDrawerApps(after, completed))
        val refreshed = DrawerProjectionResult(after, projectDrawerApps(after))
        val visible = matchingDrawerApps(after, refreshed)
        assertNotNull(visible)
        assertEquals(listOf(alpha, gamma), visible)
    }

    @Test fun staleSearchAndSortingOutputsAreRejected() {
        val before = request()
        val oldResult = DrawerProjectionResult(before, projectDrawerApps(before))
        assertNull(matchingDrawerApps(request(query = "beta"), oldResult))
        assertNull(matchingDrawerApps(request(sort = AppSort.Z_A), oldResult))
        // Equal data with a different generation still belongs to another request.
        assertNull(matchingDrawerApps(before.copy(), oldResult))
        assertNull(matchingDrawerApps(before, null))
    }

    @Test fun completedEmptyResultDiffersFromPending() {
        val noResults = request(query = "not-installed")
        val completed = DrawerProjectionResult(noResults, projectDrawerApps(noResults))
        assertEquals(emptyList<LaunchableApp>(), matchingDrawerApps(noResults, completed))
        assertNull(matchingDrawerApps(noResults, null))
    }
}

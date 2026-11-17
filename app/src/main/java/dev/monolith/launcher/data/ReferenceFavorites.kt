package dev.monolith.launcher.data

/**
 * The Reference orbit contains chosen shortcuts first, then the most-used installed
 * applications. Hidden apps never leak into home shortcuts, even if pinned earlier.
 */
fun chooseReferenceApps(
    apps: List<LaunchableApp>,
    hiddenIds: Set<String> = emptySet(),
    stats: Map<String, LaunchStats> = emptyMap(),
    limit: Int = ORBIT_CAPACITY,
    favoriteIds: List<String> = emptyList()
): List<LaunchableApp> {
    val installed = filterHiddenApps(apps, hiddenIds)
    val byId = installed.associateBy { it.id }
    val priority = normalizeOrbitFavorites(favoriteIds).mapNotNull(byId::get)
    val usedIds = priority.mapTo(mutableSetOf()) { it.id }
    val remaining = sortApps(installed.filterNot { it.id in usedIds }, AppSort.FREQUENT, stats)
    return (priority + remaining).take(limit.coerceIn(0, ORBIT_CAPACITY))
}

/**
 * Editorial shows actual apps recently launched through MONOLITH, never inferred
 * device-wide usage. A zero or absent timestamp is not evidence of recent use.
 * The caller may fall back to its current quick-access list on a fresh install.
 */
fun chooseRecentlyOpenedApps(
    apps: List<LaunchableApp>,
    hiddenIds: Set<String> = emptySet(),
    stats: Map<String, LaunchStats> = emptyMap(),
    limit: Int = 8
): List<LaunchableApp> {
    val eligible = filterHiddenApps(apps, hiddenIds)
        .distinctBy { it.id }
        .filter { (stats[it.id]?.lastOpened ?: 0L) > 0L }
    return sortApps(eligible, AppSort.LAST_OPENED, stats)
        .take(limit.coerceIn(0, 8))
}

// The two HOME scenes share one short, immediately visible row. Actual local
// recents lead; saved Orbit shortcuts and frequent apps fill vacant slots.
// Inputs are already filtered for hidden/uninstalled apps by their selectors.
data class HomeShortcutSelection(
    val apps: List<LaunchableApp>,
    val allRecent: Boolean
)

fun chooseHomeShortcuts(
    recent: List<LaunchableApp>,
    fallback: List<LaunchableApp>,
    limit: Int = 4
): HomeShortcutSelection {
    val uniqueRecent = recent.distinctBy { it.id }
    val selected = (uniqueRecent + fallback)
        .distinctBy { it.id }
        .take(limit.coerceIn(0, 4))
    val recentIds = uniqueRecent.mapTo(HashSet()) { it.id }
    return HomeShortcutSelection(
        apps = selected,
        allRecent = selected.isNotEmpty() && selected.all { it.id in recentIds }
    )
}

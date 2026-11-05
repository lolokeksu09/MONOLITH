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

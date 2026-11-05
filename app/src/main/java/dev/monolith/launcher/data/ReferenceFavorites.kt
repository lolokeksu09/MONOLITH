package dev.monolith.launcher.data

/**
 * Both Reference pages share the same visible launch targets.
 * Order by actual successful launches from MONOLITH; alphabetical on a new install.
 */
fun chooseReferenceApps(
    apps: List<LaunchableApp>,
    hiddenIds: Set<String> = emptySet(),
    stats: Map<String, LaunchStats> = emptyMap(),
    limit: Int = 15
): List<LaunchableApp> =
    sortApps(filterHiddenApps(apps, hiddenIds), AppSort.FREQUENT, stats)
        .take(limit.coerceAtLeast(0))

package dev.monolith.launcher.data

/** Order: existing Dock, pinned shortcuts, then visible installed apps. */
fun chooseReferenceApps(
    apps: List<LaunchableApp>,
    dockIds: List<String>,
    pinnedIds: List<String>,
    hiddenIds: Set<String> = emptySet(),
    limit: Int = 15
): List<LaunchableApp> {
    val byId = apps.associateBy { it.id }
    val favorites = (dockIds + pinnedIds).distinct().mapNotNull(byId::get)
    val used = favorites.mapTo(mutableSetOf()) { it.id }
    val available = apps.filter { it.id !in used && it.id !in hiddenIds }
    return (favorites + available).take(limit.coerceAtLeast(0))
}

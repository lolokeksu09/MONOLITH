package dev.monolith.launcher.data

/** A disabled search control must never leave an invisible active filter. */
fun effectiveDrawerQuery(query: String, searchEnabled: Boolean): String =
    if (searchEnabled) query else ""

package dev.monolith.launcher.data

import java.text.Collator
import java.util.Locale

/** Usage order is based only on successful launches made through MONOLITH. */
enum class AppSort {
    A_Z, Z_A, FREQUENT, LAST_OPENED, INSTALLED_NEWEST, INSTALLED_OLDEST
}

fun parseAppSort(value: String?): AppSort =
    AppSort.entries.firstOrNull { it.name == value } ?: AppSort.A_Z

data class LaunchStats(val count: Long = 0L, val lastOpened: Long = 0L)

/** Deterministic tie-breakers; installedAt=0 means Android has no installation timestamp. */
fun sortApps(
    apps: List<LaunchableApp>,
    sort: AppSort,
    stats: Map<String, LaunchStats> = emptyMap(),
    locale: Locale = Locale.getDefault()
): List<LaunchableApp> {
    val collator = Collator.getInstance(locale).apply { strength = Collator.PRIMARY }
    val alpha = Comparator<LaunchableApp> { a, b ->
        val byLabel = collator.compare(a.label, b.label)
        if (byLabel != 0) byLabel else a.id.compareTo(b.id)
    }
    val compare = Comparator<LaunchableApp> { a, b ->
        fun fallback(n: Int): Int = if (n != 0) n else alpha.compare(a, b)
        val aa = stats[a.id] ?: LaunchStats()
        val bb = stats[b.id] ?: LaunchStats()
        when (sort) {
            AppSort.A_Z -> alpha.compare(a, b)
            AppSort.Z_A -> -alpha.compare(a, b)
            AppSort.FREQUENT -> fallback(
                compareValues(bb.count, aa.count).takeIf { it != 0 }
                    ?: compareValues(bb.lastOpened, aa.lastOpened)
            )
            AppSort.LAST_OPENED -> fallback(compareValues(bb.lastOpened, aa.lastOpened))
            AppSort.INSTALLED_NEWEST -> fallback(compareValues(b.installedAt, a.installedAt))
            AppSort.INSTALLED_OLDEST -> {
                // Unknown dates always remain last in both installation modes.
                val x = if (a.installedAt <= 0) Long.MAX_VALUE else a.installedAt
                val y = if (b.installedAt <= 0) Long.MAX_VALUE else b.installedAt
                fallback(compareValues(x, y))
            }
        }
    }
    return apps.sortedWith(compare)
}

package dev.monolith.launcher.data

import android.content.Context

/**
 * Counts only launches through MONOLITH, not use from other launchers.
 * No Usage Access permission, accessibility service or background tracking.
 */
class LaunchStatsStore(context: Context) {
    private val prefs = context.getSharedPreferences("monolith_launch_stats", Context.MODE_PRIVATE)

    fun read(apps: List<LaunchableApp>): Map<String, LaunchStats> = apps.associate { app ->
        app.id to LaunchStats(
            count = prefs.getLong("count:" + app.id, 0L).coerceAtLeast(0L),
            lastOpened = prefs.getLong("last:" + app.id, 0L).coerceAtLeast(0L)
        )
    }

    fun record(id: String, at: Long = System.currentTimeMillis()): LaunchStats {
        val key = "count:$id"
        val count = prefs.getLong(key, 0L).coerceAtLeast(0L)
        val next = LaunchStats(
            count = if (count == Long.MAX_VALUE) count else count + 1L,
            lastOpened = at.coerceAtLeast(0L)
        )
        prefs.edit().putLong(key, next.count).putLong("last:$id", next.lastOpened).apply()
        return next
    }

    fun clear() {
        prefs.edit().clear().apply()
    }
}

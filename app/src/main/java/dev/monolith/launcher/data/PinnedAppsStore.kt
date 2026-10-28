package dev.monolith.launcher.data

import android.content.Context

class PinnedAppsStore(context: Context) {
    private val preferences = context.getSharedPreferences("monolith_home", Context.MODE_PRIVATE)

    fun read(): List<String> = preferences.getString("pinned", "")
        .orEmpty()
        .split('|')
        .filter { it.isNotBlank() }
        .distinct()

    fun write(ids: List<String>) {
        preferences.edit().putString("pinned", ids.distinct().joinToString("|")).apply()
    }
}

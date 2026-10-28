package dev.monolith.launcher.data

import android.content.Context

fun togglePinned(current: List<String>, id: String): List<String> =
    if (id in current) current.filterNot { it == id } else current + id

/** Returns the original order when the app is absent or the movement is out of bounds. */
fun movePinned(current: List<String>, id: String, delta: Int): List<String> {
    if (delta != -1 && delta != 1) return current
    val index = current.indexOf(id)
    val next = index + delta
    if (index < 0 || next !in current.indices) return current
    return current.toMutableList().apply {
        val item = removeAt(index)
        add(next, item)
    }
}

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

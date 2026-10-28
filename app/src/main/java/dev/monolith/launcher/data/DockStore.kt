package dev.monolith.launcher.data

import android.content.Context
import org.json.JSONArray

/** Up to five distinct app activities in a fixed bottom row. */
const val DOCK_CAPACITY = 5

fun addDockApp(dock: List<String>, appId: String): List<String> =
    if (appId.isBlank() || appId.length > 512 || appId in dock || dock.size >= DOCK_CAPACITY) dock
    else dock + appId

fun removeDockApp(dock: List<String>, appId: String): List<String> =
    if (appId !in dock) dock else dock.filterNot { it == appId }

fun moveDockApp(dock: List<String>, from: Int, to: Int): List<String> {
    if (from !in dock.indices || to !in dock.indices || from == to) return dock
    return dock.toMutableList().apply { add(to, removeAt(from)) }
}

/** Shared normalization for prefs and portable backups. Missing apps retain their slots. */
fun normalizeDock(ids: List<String>): List<String> =
    ids.filter { it.isNotBlank() && it.length <= 512 }.distinct().take(DOCK_CAPACITY)

class DockStore(context: Context) {
    private val prefs = context.getSharedPreferences("monolith_dock", Context.MODE_PRIVATE)

    fun read(): List<String> = runCatching {
        val raw = prefs.getString("ids", "[]") ?: "[]"
        val array = JSONArray(raw)
        require(array.length() <= 64) { "Invalid dock" }
        normalizeDock((0 until array.length()).map { array.getString(it) })
    }.getOrDefault(emptyList())

    fun write(ids: List<String>) {
        val array = JSONArray()
        normalizeDock(ids).forEach(array::put)
        prefs.edit().putString("ids", array.toString()).apply()
    }
}

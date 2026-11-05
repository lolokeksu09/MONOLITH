package dev.monolith.launcher.data

import android.content.Context
import org.json.JSONArray

const val ORBIT_CAPACITY = 15

fun normalizeOrbitFavorites(ids: List<String>): List<String> =
    ids.filter { it.isNotBlank() && it.length <= 512 }.distinct().take(ORBIT_CAPACITY)

fun moveOrbitFavorite(ids: List<String>, appId: String, delta: Int): List<String> {
    val result = normalizeOrbitFavorites(ids)
    if (delta != -1 && delta != 1) return result
    val at = result.indexOf(appId)
    if (at < 0 || at + delta !in result.indices) return result
    return result.toMutableList().apply { add(at + delta, removeAt(at)) }
}

fun toggleOrbitFavorite(ids: List<String>, appId: String): List<String> {
    val current = normalizeOrbitFavorites(ids)
    if (appId in current) return current.filterNot { it == appId }
    return normalizeOrbitFavorites(current + appId)
}

/**
 * Never overwrite the old Classic HOME, Dock, or hidden-app storage.
 * These IDs belong only to the Reference orbit.
 */
class OrbitFavoritesStore(context: Context) {
    private val prefs = context.getSharedPreferences("monolith_orbit", Context.MODE_PRIVATE)

    fun read(): List<String> = runCatching {
        val value = JSONArray(prefs.getString("favorite_ids", "[]") ?: "[]")
        require(value.length() <= 256)
        normalizeOrbitFavorites((0 until value.length()).map(value::getString))
    }.getOrDefault(emptyList())

    fun write(ids: List<String>) {
        val array = JSONArray()
        normalizeOrbitFavorites(ids).forEach(array::put)
        prefs.edit().putString("favorite_ids", array.toString()).apply()
    }
}

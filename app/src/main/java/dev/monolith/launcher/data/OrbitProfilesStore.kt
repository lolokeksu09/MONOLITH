package dev.monolith.launcher.data

import android.content.Context
import org.json.JSONArray

/** MAIN is the existing MONOLITH Orbit; other profiles are additive and isolated. */
enum class OrbitProfile { MAIN, WORK, GAMES }

fun parseOrbitProfile(value: String?): OrbitProfile =
    OrbitProfile.entries.firstOrNull { it.name == value } ?: OrbitProfile.MAIN

/**
 * Preserve pre-0.21 favorites in monolith_orbit/favorite_ids without changing
 * them until the user explicitly edits MAIN. Work and Games use new keys.
 */
class OrbitProfilesStore(context: Context) {
    private val legacy = OrbitFavoritesStore(context)
    private val prefs = context.getSharedPreferences(
        "monolith_orbit_profiles", Context.MODE_PRIVATE
    )

    fun readActive(): OrbitProfile =
        parseOrbitProfile(prefs.getString("active_profile", null))

    fun writeActive(profile: OrbitProfile) {
        prefs.edit().putString("active_profile", profile.name).apply()
    }

    fun readFavorites(profile: OrbitProfile): List<String> {
        if (profile == OrbitProfile.MAIN) return legacy.read()
        val key = profileKey(profile)
        return runCatching {
            val array = JSONArray(prefs.getString(key, "[]") ?: "[]")
            require(array.length() <= 256)
            val values = (0 until array.length()).map { index ->
                require(array.opt(index) is String)
                array.getString(index)
            }
            normalizeOrbitFavorites(values)
        }.getOrDefault(emptyList())
    }

    fun writeFavorites(profile: OrbitProfile, ids: List<String>) {
        val normalized = normalizeOrbitFavorites(ids)
        if (profile == OrbitProfile.MAIN) {
            legacy.write(normalized)
            return
        }
        val array = JSONArray()
        normalized.forEach(array::put)
        prefs.edit().putString(profileKey(profile), array.toString()).apply()
    }

    private fun profileKey(profile: OrbitProfile): String = when (profile) {
        OrbitProfile.MAIN -> error("MAIN always uses the legacy store")
        OrbitProfile.WORK -> "favorites_work"
        OrbitProfile.GAMES -> "favorites_games"
    }
}

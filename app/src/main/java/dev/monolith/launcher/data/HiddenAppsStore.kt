package dev.monolith.launcher.data

import android.content.Context
import org.json.JSONArray

/** Hiding changes only the MONOLITH app drawer; it never disables or uninstalls an app. */
fun filterHiddenApps(apps: List<LaunchableApp>, hidden: Set<String>): List<LaunchableApp> =
    if (hidden.isEmpty()) apps else apps.filterNot { it.id in hidden }

fun hideApp(hidden: Set<String>, appId: String): Set<String> =
    if (appId.isBlank() || appId.length > 512) hidden else hidden + appId

fun unhideApp(hidden: Set<String>, appId: String): Set<String> = hidden - appId

/** Isolated from the general launcher settings to avoid changing existing preference migrations. */
class HiddenAppsStore(context: Context) {
    private val prefs = context.getSharedPreferences("monolith_hidden_apps", Context.MODE_PRIVATE)

    fun read(): Set<String> = runCatching {
        val array = JSONArray(prefs.getString("ids", "[]") ?: "[]")
        require(array.length() <= 2048)
        buildSet {
            repeat(array.length()) { index ->
                val id = array.getString(index)
                if (id.isNotBlank() && id.length <= 512) add(id)
            }
        }
    }.getOrDefault(emptySet())

    fun write(ids: Set<String>) {
        val json = JSONArray()
        ids.filter { it.isNotBlank() && it.length <= 512 }.sorted().forEach(json::put)
        prefs.edit().putString("ids", json.toString()).apply()
    }
}

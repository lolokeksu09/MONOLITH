package dev.monolith.launcher.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

sealed interface HomeEntry {
    val key: String
    data class App(val appId: String) : HomeEntry {
        override val key: String get() = "app:$appId"
    }
    data class Folder(val id: String, val name: String, val appIds: List<String>) : HomeEntry {
        override val key: String get() = "folder:$id"
    }
}

data class HomeLayout(val entries: List<HomeEntry> = emptyList()) {
    val appIds: List<String> get() = entries.flatMap {
        when (it) {
            is HomeEntry.App -> listOf(it.appId)
            is HomeEntry.Folder -> it.appIds
        }
    }
}

fun pinToHome(layout: HomeLayout, appId: String): HomeLayout =
    if (appId in layout.appIds) layout
    else layout.copy(entries = layout.entries + HomeEntry.App(appId))

fun unpinFromHome(layout: HomeLayout, appId: String): HomeLayout =
    layout.copy(entries = layout.entries.mapNotNull {
        when (it) {
            is HomeEntry.App -> if (it.appId == appId) null else it
            is HomeEntry.Folder -> it.copy(appIds = it.appIds.filterNot { id -> id == appId })
        }
    })

fun createHomeFolder(layout: HomeLayout, appId: String, folderId: String, name: String): HomeLayout {
    val index = layout.entries.indexOfFirst { it is HomeEntry.App && it.appId == appId }
    if (index < 0 || layout.entries.any { it.key == "folder:$folderId" }) return layout
    return layout.copy(entries = layout.entries.toMutableList().apply {
        removeAt(index)
        add(index, HomeEntry.Folder(folderId, name.take(40).ifBlank { "Folder" }, listOf(appId)))
    })
}

fun addAppToFolder(layout: HomeLayout, appId: String, folderId: String): HomeLayout {
    if (layout.entries.none { it is HomeEntry.Folder && it.id == folderId }) return layout
    val without = unpinFromHome(layout, appId)
    return without.copy(entries = without.entries.map {
        if (it is HomeEntry.Folder && it.id == folderId) it.copy(appIds = it.appIds + appId) else it
    })
}

fun moveAppOutOfFolder(layout: HomeLayout, appId: String, folderId: String): HomeLayout {
    val folder = layout.entries.firstOrNull {
        it is HomeEntry.Folder && it.id == folderId
    } as? HomeEntry.Folder ?: return layout
    if (appId !in folder.appIds) return layout
    val without = unpinFromHome(layout, appId)
    return without.copy(entries = without.entries + HomeEntry.App(appId))
}

fun removeHomeFolder(layout: HomeLayout, folderId: String): HomeLayout {
    val index = layout.entries.indexOfFirst { it is HomeEntry.Folder && it.id == folderId }
    if (index < 0) return layout
    val folder = layout.entries[index] as HomeEntry.Folder
    return layout.copy(entries = layout.entries.toMutableList().apply {
        removeAt(index)
        addAll(index, folder.appIds.map { HomeEntry.App(it) })
    })
}

fun renameHomeFolder(layout: HomeLayout, folderId: String, name: String): HomeLayout {
    val cleaned = name.trim().take(40)
    if (cleaned.isEmpty()) return layout
    return layout.copy(entries = layout.entries.map {
        if (it is HomeEntry.Folder && it.id == folderId) it.copy(name = cleaned) else it
    })
}

fun reorderHome(layout: HomeLayout, from: Int, to: Int): HomeLayout {
    if (from !in layout.entries.indices || to !in layout.entries.indices || from == to) return layout
    return layout.copy(entries = layout.entries.toMutableList().apply {
        add(to, removeAt(from))
    })
}

class HomeLayoutStore(context: Context) {
    private val preferences = context.getSharedPreferences("monolith_home", Context.MODE_PRIVATE)

    fun read(): HomeLayout {
        val encoded = preferences.getString("layout_v1", null)
        if (encoded == null) {
            // Non-destructive migration: 0.2 stored pinned identifiers in this preference.
            val legacy = PinnedAppsStoreLegacyRead()
            return HomeLayout(legacy.map { HomeEntry.App(it) })
        }
        return try {
            val array = JSONArray(encoded)
            val entries = buildList {
                for (i in 0 until array.length()) {
                    val item = array.optJSONObject(i) ?: continue
                    when (item.optString("type")) {
                        "app" -> item.optString("id").takeIf { it.isNotBlank() }?.let { add(HomeEntry.App(it)) }
                        "folder" -> {
                            val id = item.optString("id")
                            if (id.isBlank()) continue
                            val ids = item.optJSONArray("apps") ?: JSONArray()
                            val apps = (0 until ids.length()).mapNotNull { ids.optString(it).takeIf(String::isNotBlank) }.distinct()
                            add(HomeEntry.Folder(id, item.optString("name", "Folder").take(40), apps))
                        }
                    }
                }
            }
            // Never permit duplicate app membership (including across folders).
            val seen = mutableSetOf<String>()
            HomeLayout(entries.mapNotNull { entry ->
                when (entry) {
                    is HomeEntry.App -> if (seen.add(entry.appId)) entry else null
                    is HomeEntry.Folder -> entry.copy(appIds = entry.appIds.filter(seen::add))
                }
            }.distinctBy { it.key })
        } catch (_: Exception) {
            HomeLayout(PinnedAppsStoreLegacyRead().map { HomeEntry.App(it) })
        }
    }

    private fun PinnedAppsStoreLegacyRead(): List<String> =
        preferences.getString("pinned", "").orEmpty().split('|').filter(String::isNotBlank).distinct()

    fun write(layout: HomeLayout) {
        val array = JSONArray()
        layout.entries.forEach { entry ->
            val obj = JSONObject()
            when (entry) {
                is HomeEntry.App -> obj.put("type", "app").put("id", entry.appId)
                is HomeEntry.Folder -> {
                    obj.put("type", "folder").put("id", entry.id).put("name", entry.name)
                    val apps = JSONArray()
                    entry.appIds.forEach { apps.put(it) }
                    obj.put("apps", apps)
                }
            }
            array.put(obj)
        }
        preferences.edit().putString("layout_v1", array.toString()).apply()
    }
}

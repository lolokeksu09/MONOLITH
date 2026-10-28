package dev.monolith.launcher.data

import org.json.JSONArray
import org.json.JSONObject

/**
 * Portable home-screen backup. Android AppWidgetHost IDs are intentionally excluded:
 * those IDs belong to one installation and cannot be restored on another device.
 */
data class HomeBackup(val layout: HomeLayout, val preferences: LauncherPreferences, val hiddenApps: Set<String> = emptySet())

private const val FORMAT = "monolith-home"
private const val VERSION = 1
private const val MAX_BACKUP_CHARS = 256 * 1024
private const val MAX_ENTRIES = 512
private const val MAX_FOLDER_APPS = 512

fun encodeHomeBackup(layout: HomeLayout, preferences: LauncherPreferences, hiddenApps: Set<String> = emptySet()): String {
    val entries = JSONArray()
    layout.entries.forEach { entry ->
        val item = JSONObject()
        when (entry) {
            is HomeEntry.App -> {
                item.put("type", "app")
                item.put("id", entry.appId)
            }
            is HomeEntry.Folder -> {
                item.put("type", "folder")
                item.put("id", entry.id)
                item.put("name", entry.name)
                val apps = JSONArray()
                entry.appIds.forEach(apps::put)
                item.put("apps", apps)
            }
        }
        entries.put(item)
    }
    return JSONObject()
        .put("format", FORMAT)
        .put("version", VERSION)
        .put("columns", normalizeColumns(preferences.columns))
        .put("showLabels", preferences.showLabels)
        .put("drawerColumns", normalizeColumns(preferences.drawerColumns))
        .put("drawerLabels", preferences.showDrawerLabels)
        .put("appSort", preferences.appSort.name)
        .put("drawerIconSize", normalizeDrawerIconSize(preferences.drawerIconSize))
        .put("pinDots", preferences.showPinDots)
        .put("hiddenApps", JSONArray().apply { hiddenApps.sorted().forEach { put(it) } })
        .put("entries", entries)
        .toString(2)
}

/** Parse completely before applying any changes. Invalid documents are rejected, not partially restored. */
fun decodeHomeBackup(raw: String): HomeBackup {
    require(raw.length <= MAX_BACKUP_CHARS) { "Backup exceeds size limit" }
    val root = JSONObject(raw)
    require(root.getString("format") == FORMAT && root.getInt("version") == VERSION) {
        "Not a supported MONOLITH backup"
    }
    val items = root.getJSONArray("entries")
    require(items.length() <= MAX_ENTRIES) { "Too many home tiles" }

    val usedKeys = mutableSetOf<String>()
    val usedApps = mutableSetOf<String>()
    val entries = buildList {
        repeat(items.length()) { index ->
            val item = items.getJSONObject(index)
            when (item.getString("type")) {
                "app" -> {
                    val id = item.getString("id")
                    require(id.isNotBlank() && id.length <= 512 && usedApps.add(id)) { "Invalid or duplicate app" }
                    add(HomeEntry.App(id))
                }
                "folder" -> {
                    val id = item.getString("id")
                    require(id.isNotBlank() && id.length <= 128) { "Invalid folder ID" }
                    val name = item.getString("name").trim()
                    require(name.isNotBlank() && name.length <= 40) { "Invalid folder name" }
                    val list = item.getJSONArray("apps")
                    require(list.length() <= MAX_FOLDER_APPS) { "Too many apps in folder" }
                    val appIds = buildList {
                        repeat(list.length()) { j ->
                            val appId = list.getString(j)
                            require(appId.isNotBlank() && appId.length <= 512 && usedApps.add(appId)) {
                                "Invalid or duplicate app"
                            }
                            add(appId)
                        }
                    }
                    add(HomeEntry.Folder(id, name, appIds))
                }
                else -> throw IllegalArgumentException("Unknown home tile type")
            }
        }
    }
    require(entries.all { usedKeys.add(it.key) }) { "Duplicate home tiles" }
    val columns = root.getInt("columns")
    require(columns in 3..6) { "Invalid grid columns" }
    val labels = root.getBoolean("showLabels")
    return HomeBackup(HomeLayout(entries), LauncherPreferences(
        columns = columns,
        showLabels = labels,
        drawerColumns = normalizeColumns(root.optInt("drawerColumns", 4)),
        showDrawerLabels = root.optBoolean("drawerLabels", true),
        appSort = parseAppSort(root.optString("appSort", AppSort.A_Z.name)),
        drawerIconSize = normalizeDrawerIconSize(root.optInt("drawerIconSize", 48)),
        showPinDots = root.optBoolean("pinDots", true)
    ), parseHiddenBackupIds(root.optJSONArray("hiddenApps")))
}

/** Old backups have no hidden-app field; invalid files must be rejected. */
private fun parseHiddenBackupIds(array: JSONArray?): Set<String> {
    if (array == null) return emptySet()
    require(array.length() <= MAX_ENTRIES) { "Too many hidden apps" }
    return buildSet {
        repeat(array.length()) { index ->
            val id = array.getString(index)
            require(id.isNotBlank() && id.length <= 512 && add(id)) {
                "Invalid or duplicate hidden app ID"
            }
        }
    }
}

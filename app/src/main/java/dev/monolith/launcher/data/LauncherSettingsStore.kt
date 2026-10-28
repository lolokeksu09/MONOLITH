package dev.monolith.launcher.data

import android.content.Context

data class LauncherPreferences(
    val columns: Int = 4,
    val showLabels: Boolean = true,
    val drawerColumns: Int = 4,
    val showDrawerLabels: Boolean = true,
    val appSort: AppSort = AppSort.A_Z,
    val drawerIconSize: Int = 48,
    val showPinDots: Boolean = true,
    val showWallpaper: Boolean = false,
    val showDrawerSearch: Boolean = true,
    val drawerOpacity: Int = 65
)

fun normalizeDrawerOpacity(value: Int): Int = value.coerceIn(25, 100)

fun normalizeColumns(value: Int): Int = value.coerceIn(3, 6)
fun normalizeDrawerIconSize(value: Int): Int = if (value in setOf(40, 48, 56)) value else 48

class LauncherSettingsStore(context: Context) {
    private val preferences = context.getSharedPreferences("monolith_settings", Context.MODE_PRIVATE)

    fun read() = LauncherPreferences(
        columns = normalizeColumns(preferences.getInt("columns", 4)),
        showLabels = preferences.getBoolean("show_labels", true),
        drawerColumns = normalizeColumns(preferences.getInt("drawer_columns", 4)),
        showDrawerLabels = preferences.getBoolean("drawer_labels", true),
        appSort = parseAppSort(preferences.getString("app_sort", AppSort.A_Z.name)),
        drawerIconSize = normalizeDrawerIconSize(preferences.getInt("drawer_icon_size", 48)),
        showPinDots = preferences.getBoolean("pin_dots", true),
        showWallpaper = preferences.getBoolean("show_wallpaper", false),
        showDrawerSearch = preferences.getBoolean("show_drawer_search", true),
        drawerOpacity = normalizeDrawerOpacity(preferences.getInt("drawer_opacity", 65))
    )

    fun write(value: LauncherPreferences) {
        preferences.edit()
            .putInt("columns", normalizeColumns(value.columns))
            .putBoolean("show_labels", value.showLabels)
            .putInt("drawer_columns", normalizeColumns(value.drawerColumns))
            .putBoolean("drawer_labels", value.showDrawerLabels)
            .putString("app_sort", value.appSort.name)
            .putInt("drawer_icon_size", normalizeDrawerIconSize(value.drawerIconSize))
            .putBoolean("pin_dots", value.showPinDots)
            .putBoolean("show_wallpaper", value.showWallpaper)
            .putBoolean("show_drawer_search", value.showDrawerSearch)
            .putInt("drawer_opacity", normalizeDrawerOpacity(value.drawerOpacity))
            .apply()
    }
}

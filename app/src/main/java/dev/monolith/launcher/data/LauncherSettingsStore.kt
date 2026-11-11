package dev.monolith.launcher.data

import android.content.Context

enum class AppIconStyle { ORIGINAL, MONO, INK }

/** Drawer search remains at the top for existing installations; bottom is opt-in. */
enum class DrawerSearchPosition { TOP, BOTTOM }

fun parseDrawerSearchPosition(value: String?): DrawerSearchPosition =
    DrawerSearchPosition.entries.firstOrNull { it.name == value } ?: DrawerSearchPosition.TOP

data class LauncherPreferences(
    val drawerColumns: Int = 4,
    val showDrawerSearch: Boolean = true,
    val drawerSearchPosition: DrawerSearchPosition = DrawerSearchPosition.TOP,
    val appSort: AppSort = AppSort.A_Z,
    val appIconStyle: AppIconStyle = AppIconStyle.ORIGINAL,
    val orbitDensity: Int = ORBIT_CAPACITY
)

fun normalizeColumns(value: Int): Int = value.coerceIn(3, 6)

fun normalizeOrbitDensity(value: Int): Int = when (value) {
    8, 12, ORBIT_CAPACITY -> value
    else -> ORBIT_CAPACITY
}

fun parseAppIconStyle(value: String?): AppIconStyle =
    AppIconStyle.entries.firstOrNull { it.name == value } ?: AppIconStyle.ORIGINAL

class LauncherSettingsStore(context: Context) {
    private val prefs =
        context.getSharedPreferences("monolith_settings", Context.MODE_PRIVATE)

    fun read() = LauncherPreferences(
        drawerColumns = normalizeColumns(prefs.getInt("drawer_columns", 4)),
        showDrawerSearch = prefs.getBoolean("show_drawer_search", true),
        drawerSearchPosition = parseDrawerSearchPosition(
            prefs.getString("drawer_search_position", null)
        ),
        appSort = parseAppSort(prefs.getString("app_sort", AppSort.A_Z.name)),
        appIconStyle = parseAppIconStyle(
            prefs.getString("app_icon_style", AppIconStyle.ORIGINAL.name)
        ),
        orbitDensity = normalizeOrbitDensity(
            prefs.getInt("orbit_density", ORBIT_CAPACITY)
        )
    )

    fun write(value: LauncherPreferences) {
        prefs.edit()
            .putInt("drawer_columns", normalizeColumns(value.drawerColumns))
            .putBoolean("show_drawer_search", value.showDrawerSearch)
            .putString("drawer_search_position", value.drawerSearchPosition.name)
            .putString("app_sort", value.appSort.name)
            .putString("app_icon_style", value.appIconStyle.name)
            .putInt("orbit_density", normalizeOrbitDensity(value.orbitDensity))
            .apply()
    }
}

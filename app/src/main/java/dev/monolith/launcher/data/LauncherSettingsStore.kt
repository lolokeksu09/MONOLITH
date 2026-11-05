package dev.monolith.launcher.data

import android.content.Context

/** Only settings actively used by the Reference HOME and the app drawer. */
data class LauncherPreferences(
    val drawerColumns: Int = 4,
    val showDrawerSearch: Boolean = true,
    val appSort: AppSort = AppSort.A_Z
)

fun normalizeColumns(value: Int): Int = value.coerceIn(3, 6)

/**
 * Keep the existing preference file and keys to preserve 0.11 drawer settings.
 * Old desktop keys are not erased: this update must not destroy user data.
 */
class LauncherSettingsStore(context: Context) {
    private val prefs = context.getSharedPreferences("monolith_settings", Context.MODE_PRIVATE)

    fun read() = LauncherPreferences(
        drawerColumns = normalizeColumns(prefs.getInt("drawer_columns", 4)),
        showDrawerSearch = prefs.getBoolean("show_drawer_search", true),
        appSort = parseAppSort(prefs.getString("app_sort", AppSort.A_Z.name))
    )

    fun write(value: LauncherPreferences) {
        prefs.edit()
            .putInt("drawer_columns", normalizeColumns(value.drawerColumns))
            .putBoolean("show_drawer_search", value.showDrawerSearch)
            .putString("app_sort", value.appSort.name)
            .apply()
    }
}

package dev.monolith.launcher.data

import android.content.Context

data class LauncherPreferences(
    val columns: Int = 4,
    val showLabels: Boolean = true
)

fun normalizeColumns(value: Int): Int = value.coerceIn(3, 6)

class LauncherSettingsStore(context: Context) {
    private val preferences = context.getSharedPreferences("monolith_settings", Context.MODE_PRIVATE)

    fun read() = LauncherPreferences(
        columns = normalizeColumns(preferences.getInt("columns", 4)),
        showLabels = preferences.getBoolean("show_labels", true)
    )

    fun write(value: LauncherPreferences) {
        preferences.edit()
            .putInt("columns", normalizeColumns(value.columns))
            .putBoolean("show_labels", value.showLabels)
            .apply()
    }
}

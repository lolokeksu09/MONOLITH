package dev.monolith.launcher.data

import android.content.Context

fun parseWidgetIds(value: String): List<Int> =
    value.split(',').mapNotNull(String::toIntOrNull).filter { it > 0 }.distinct()

class WidgetIdsStore(context: Context) {
    private val preferences = context.getSharedPreferences("monolith_widgets", Context.MODE_PRIVATE)

    fun read(): List<Int> = parseWidgetIds(preferences.getString("ids", "").orEmpty())

    fun write(ids: List<Int>) {
        preferences.edit().putString("ids", ids.filter { it > 0 }.distinct().joinToString(",")).apply()
    }
}

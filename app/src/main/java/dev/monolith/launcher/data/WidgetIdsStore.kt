package dev.monolith.launcher.data

import android.content.Context

fun parseWidgetIds(value: String): List<Int> =
    value.split(',').mapNotNull(String::toIntOrNull).filter { it > 0 }.distinct()

/** Reorder registered AppWidgetHost IDs without reallocating their bindings. */
fun moveWidget(ids: List<Int>, widgetId: Int, direction: Int): List<Int> {
    if (widgetId <= 0 || direction !in -1..1 || direction == 0) return ids
    val from = ids.indexOf(widgetId)
    if (from < 0) return ids
    val target = from + direction
    if (target !in ids.indices) return ids
    return ids.toMutableList().apply { add(target, removeAt(from)) }
}

class WidgetIdsStore(context: Context) {
    private val preferences = context.getSharedPreferences("monolith_widgets", Context.MODE_PRIVATE)

    fun read(): List<Int> = parseWidgetIds(preferences.getString("ids", "").orEmpty())

    fun write(ids: List<Int>) {
        preferences.edit().putString("ids", ids.filter { it > 0 }.distinct().joinToString(",")).apply()
    }
}

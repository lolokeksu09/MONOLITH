package dev.monolith.launcher.data

import android.content.Context

const val DEFAULT_WIDGET_HEIGHT_DP = 192
const val MIN_WIDGET_HEIGHT_DP = 128
const val MAX_WIDGET_HEIGHT_DP = 512
const val WIDGET_HEIGHT_STEP_DP = 64

/** Widget dimensions are bounded to keep the HOME screen recoverable. */
fun normalizeWidgetHeight(value: Int): Int =
    value.coerceIn(MIN_WIDGET_HEIGHT_DP, MAX_WIDGET_HEIGHT_DP)

/** Only user requests move a widget's size, in predictable 64dp increments. */
fun stepWidgetHeight(height: Int, direction: Int): Int {
    val current = normalizeWidgetHeight(height)
    if (direction == 0) return current
    val remainder = current % WIDGET_HEIGHT_STEP_DP
    return when {
        direction > 0 -> normalizeWidgetHeight(
            current + if (remainder == 0) WIDGET_HEIGHT_STEP_DP else WIDGET_HEIGHT_STEP_DP - remainder
        )
        else -> normalizeWidgetHeight(
            current - if (remainder == 0) WIDGET_HEIGHT_STEP_DP else remainder
        )
    }
}

/** Settings are local to the installation: widget host IDs must never be imported from backups. */
class WidgetSizeStore(context: Context) {
    private val prefs = context.getSharedPreferences("monolith_widget_sizes", Context.MODE_PRIVATE)

    fun read(widgetId: Int): Int =
        if (widgetId <= 0) DEFAULT_WIDGET_HEIGHT_DP
        else normalizeWidgetHeight(runCatching {
            prefs.getInt("height:$widgetId", DEFAULT_WIDGET_HEIGHT_DP)
        }.getOrDefault(DEFAULT_WIDGET_HEIGHT_DP))

    fun write(widgetId: Int, heightDp: Int) {
        if (widgetId <= 0) return
        prefs.edit().putInt("height:$widgetId", normalizeWidgetHeight(heightDp)).apply()
    }

    fun delete(widgetId: Int) {
        if (widgetId > 0) prefs.edit().remove("height:$widgetId").apply()
    }
}

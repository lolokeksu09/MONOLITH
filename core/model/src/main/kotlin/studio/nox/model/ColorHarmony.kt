package studio.nox.model

import kotlin.math.abs
import kotlin.math.roundToInt

/** Generates matching secondary colors without mutating the selected scene's other properties. */
object ColorHarmony {
    enum class Mode(val title: String, val description: String, val degrees: Float) {
        ANALOGOUS("Рядом", "Соседний оттенок", 28f),
        COMPLEMENTARY("Контраст", "Противоположный оттенок", 180f),
        TRIADIC("Триада", "Выразительный третий цвет", 120f)
    }

    fun apply(scene: SceneConfig, mode: Mode): SceneConfig {
        val primary = scene.primary and 0xFFFFFF
        val r = ((primary shr 16) and 0xFF) / 255f
        val g = ((primary shr 8) and 0xFF) / 255f
        val b = (primary and 0xFF) / 255f
        val max = maxOf(r, g, b)
        val min = minOf(r, g, b)
        val delta = max - min
        val rawHue = when {
            delta == 0f -> 0f
            max == r -> 60f * ((g - b) / delta)
            max == g -> 60f * ((b - r) / delta + 2f)
            else -> 60f * ((r - g) / delta + 4f)
        }
        val hue = (rawHue + mode.degrees + 360f) % 360f
        val saturation = maxOf(if (max == 0f) 0f else delta / max, 0.52f)
        val value = maxOf(max, 0.60f)
        val chroma = value * saturation
        val x = chroma * (1f - abs((hue / 60f) % 2f - 1f))
        val m = value - chroma
        val (nr, ng, nb) = when {
            hue < 60f -> Triple(chroma, x, 0f)
            hue < 120f -> Triple(x, chroma, 0f)
            hue < 180f -> Triple(0f, chroma, x)
            hue < 240f -> Triple(0f, x, chroma)
            hue < 300f -> Triple(x, 0f, chroma)
            else -> Triple(chroma, 0f, x)
        }
        fun byte(component: Float): Int = ((component + m) * 255f).roundToInt().coerceIn(0, 255)
        val secondary = (byte(nr) shl 16) or (byte(ng) shl 8) or byte(nb)
        return scene.copy(secondary = secondary)
    }
}

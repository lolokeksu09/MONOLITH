package dev.lolokeksu.contextcapsule

/** Explicit ARGB tokens prevent unreadable default Text colors on AMOLED backgrounds. */
object PremiumPalette {
    const val Background = 0xFF05070BL
    const val Panel = 0xFF101722L
    const val PanelElevated = 0xFF172236L
    const val Title = 0xFFF5F8FFL
    const val Secondary = 0xFFB4C1D4L
    const val Muted = 0xFF8493ABL
    const val Blue = 0xFF73ACFFL
    const val BlueDeep = 0xFF315DBAL
    const val Outline = 0xFF26354BL
    const val Teal = 0xFF79D7C1L

    private fun luminance(rgb: Long): Double {
        fun linear(channel: Int): Double {
            val c = ((rgb shr channel) and 255L).toDouble() / 255.0
            return if (c <= 0.04045) c / 12.92 else Math.pow((c + 0.055) / 1.055, 2.4)
        }
        return 0.2126 * linear(16) + 0.7152 * linear(8) + 0.0722 * linear(0)
    }

    fun contrastRatio(foreground: Long, background: Long): Double {
        val a = luminance(foreground)
        val b = luminance(background)
        return (maxOf(a, b) + 0.05) / (minOf(a, b) + 0.05)
    }
}

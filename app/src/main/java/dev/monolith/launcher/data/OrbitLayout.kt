package dev.monolith.launcher.data

data class OrbitPoint(val x: Float, val y: Float)

/** 58dp staggered grid; each 48dp touch target stays independently tappable. */
private val ORBIT_GEOMETRY = listOf(
    OrbitPoint(-29f, -116f), OrbitPoint(29f, -116f),
    OrbitPoint(-87f, -58f), OrbitPoint(-29f, -58f),
    OrbitPoint(29f, -58f), OrbitPoint(87f, -58f),
    OrbitPoint(-87f, 0f), OrbitPoint(-29f, 0f),
    OrbitPoint(29f, 0f), OrbitPoint(87f, 0f),
    OrbitPoint(-58f, 58f), OrbitPoint(0f, 58f), OrbitPoint(58f, 58f),
    OrbitPoint(-29f, 116f), OrbitPoint(29f, 116f)
)

fun orbitPositions(count: Int): List<OrbitPoint> = ORBIT_GEOMETRY.take(count.coerceIn(0, ORBIT_CAPACITY))

/**
 * Collapsed and still-overlapping icons may animate, but must never receive a tap.
 * At 90% expansion the closest 58dp centers are over 52dp apart, clearing 48dp targets.
 */
fun orbitHitTargetsEnabled(expanded: Boolean, progress: Float = 1f): Boolean =
    expanded && progress >= .90f

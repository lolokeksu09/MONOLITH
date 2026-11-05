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

/** Collapsed icons may animate out, but must never receive a tap. */
fun orbitHitTargetsEnabled(expanded: Boolean): Boolean = expanded

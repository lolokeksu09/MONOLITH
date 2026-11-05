package dev.monolith.launcher.data

data class OrbitPoint(val x: Float, val y: Float)

const val ORBIT_CAPACITY = 15

private val ORBIT_GEOMETRY_8 = listOf(
    OrbitPoint(-58f, -87f), OrbitPoint(58f, -87f),
    OrbitPoint(-87f, -29f), OrbitPoint(87f, -29f),
    OrbitPoint(-87f, 29f), OrbitPoint(87f, 29f),
    OrbitPoint(-58f, 87f), OrbitPoint(58f, 87f)
)

private val ORBIT_GEOMETRY_12 = listOf(
    OrbitPoint(-87f, -58f), OrbitPoint(-29f, -58f),
    OrbitPoint(29f, -58f), OrbitPoint(87f, -58f),
    OrbitPoint(-87f, 0f), OrbitPoint(-29f, 0f),
    OrbitPoint(29f, 0f), OrbitPoint(87f, 0f),
    OrbitPoint(-87f, 58f), OrbitPoint(-29f, 58f),
    OrbitPoint(29f, 58f), OrbitPoint(87f, 58f)
)

private val ORBIT_GEOMETRY_15 = listOf(
    OrbitPoint(-29f, -116f), OrbitPoint(29f, -116f),
    OrbitPoint(-87f, -58f), OrbitPoint(-29f, -58f),
    OrbitPoint(29f, -58f), OrbitPoint(87f, -58f),
    OrbitPoint(-87f, 0f), OrbitPoint(-29f, 0f),
    OrbitPoint(29f, 0f), OrbitPoint(87f, 0f),
    OrbitPoint(-58f, 58f), OrbitPoint(0f, 58f),
    OrbitPoint(58f, 58f), OrbitPoint(-29f, 116f),
    OrbitPoint(29f, 116f)
)

fun orbitPositions(count: Int): List<OrbitPoint> {
    val bounded = count.coerceIn(0, ORBIT_CAPACITY)
    val geometry = when (bounded) {
        8 -> ORBIT_GEOMETRY_8
        in 9..12 -> ORBIT_GEOMETRY_12
        else -> ORBIT_GEOMETRY_15
    }
    return geometry.take(bounded)
}

fun orbitHitTargetsEnabled(expanded: Boolean, progress: Float = 1f): Boolean =
    expanded && progress >= .90f

package dev.monolith.launcher.data

data class OrbitPoint(val x: Float, val y: Float)

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

// Keep incomplete compact clusters arranged around the trigger rather than at the
// top edge of the 15-item grid. All neighbors remain at least 51dp apart.
private val ORBIT_GEOMETRY_6 = listOf(
    OrbitPoint(0f, -58f), OrbitPoint(58f, -29f),
    OrbitPoint(58f, 29f), OrbitPoint(0f, 58f),
    OrbitPoint(-58f, 29f), OrbitPoint(-58f, -29f)
)

// Partial 15-grid selections have a slightly displaced vertical centroid.
// Translate uniformly to preserve app ordering and icon spacing.
private fun centerOrbit(points: List<OrbitPoint>): List<OrbitPoint> {
    val centerX = points.map { it.x.toDouble() }.average().toFloat()
    val centerY = points.map { it.y.toDouble() }.average().toFloat()
    return points.map { OrbitPoint(it.x - centerX, it.y - centerY) }
}

/**
 * 48dp targets keep at least 51dp between icon centers at every population.
 * Existing complete 8/12/15 grids remain byte-for-byte identical.
 */
fun orbitPositions(count: Int): List<OrbitPoint> = when (val size = count.coerceIn(0, ORBIT_CAPACITY)) {
    0 -> emptyList()
    1 -> listOf(OrbitPoint(0f, 0f))
    2 -> listOf(OrbitPoint(-29f, 0f), OrbitPoint(29f, 0f))
    3 -> listOf(OrbitPoint(0f, -38f), OrbitPoint(-33f, 19f), OrbitPoint(33f, 19f))
    4 -> listOf(
        OrbitPoint(-29f, -29f), OrbitPoint(29f, -29f),
        OrbitPoint(-29f, 29f), OrbitPoint(29f, 29f)
    )
    5 -> listOf(
        OrbitPoint(0f, 0f),
        OrbitPoint(-58f, -58f), OrbitPoint(58f, -58f),
        OrbitPoint(-58f, 58f), OrbitPoint(58f, 58f)
    )
    6 -> ORBIT_GEOMETRY_6
    7 -> listOf(OrbitPoint(0f, 0f)) + ORBIT_GEOMETRY_6
    8 -> ORBIT_GEOMETRY_8
    9 -> ORBIT_GEOMETRY_8 + OrbitPoint(0f, 0f)
    10 -> listOf(
        OrbitPoint(-87f, -58f), OrbitPoint(-29f, -58f),
        OrbitPoint(29f, -58f), OrbitPoint(87f, -58f),
        OrbitPoint(-29f, 0f), OrbitPoint(29f, 0f),
        OrbitPoint(-87f, 58f), OrbitPoint(-29f, 58f),
        OrbitPoint(29f, 58f), OrbitPoint(87f, 58f)
    )
    11 -> listOf(
        OrbitPoint(-87f, -58f), OrbitPoint(-29f, -58f),
        OrbitPoint(29f, -58f), OrbitPoint(87f, -58f),
        OrbitPoint(-58f, 0f), OrbitPoint(0f, 0f), OrbitPoint(58f, 0f),
        OrbitPoint(-87f, 58f), OrbitPoint(-29f, 58f),
        OrbitPoint(29f, 58f), OrbitPoint(87f, 58f)
    )
    12 -> ORBIT_GEOMETRY_12
    13 -> centerOrbit(ORBIT_GEOMETRY_15.filterIndexed { index, _ -> index != 6 && index != 9 })
    14 -> centerOrbit(ORBIT_GEOMETRY_15.filterIndexed { index, _ -> index != 11 })
    else -> ORBIT_GEOMETRY_15
}

/** Collapsed and still-overlapping icons must never receive a tap. */
fun orbitHitTargetsEnabled(expanded: Boolean, progress: Float = 1f): Boolean =
    expanded && progress >= .90f

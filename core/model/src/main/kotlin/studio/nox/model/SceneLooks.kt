package studio.nox.model

/** Curated looks never change the source scene, its motion profile or identity. */
data class SceneLook(
    val id: String,
    val label: String,
    val primary: Int,
    val secondary: Int,
    val glow: Float,
    val density: Float
)

object SceneLooks {
    val looks = listOf(
        SceneLook("abyss", "Abyss", 0x30D9FF, 0x2857EA, 0.68f, 0.52f),
        SceneLook("ion", "Ion Violet", 0xB6A0FF, 0x6748FF, 0.76f, 0.48f),
        SceneLook("solar", "Solar", 0xFFB066, 0xFF467E, 0.72f, 0.54f),
        SceneLook("arctic", "Arctic", 0xE3F6FF, 0x68AFFF, 0.57f, 0.43f),
        SceneLook("chroma", "Chroma", 0x40FFC7, 0x22A9FF, 0.71f, 0.64f)
    )

    fun apply(scene: SceneConfig, look: SceneLook): SceneConfig =
        scene.copy(
            primary = look.primary,
            secondary = look.secondary,
            glow = look.glow,
            density = look.density
        ).normalized()

    /** Stable for the same input, while successive taps produce new seeded effects. */
    fun shuffle(scene: SceneConfig): SceneConfig {
        val nextSeed = if (scene.seed == Int.MAX_VALUE) 1 else scene.seed + 1
        val value = ((nextSeed.toLong() * 1103515245L + 12345L) and 0x7fffffffL).toFloat() / Int.MAX_VALUE
        val look = looks[(nextSeed.toLong() and 0x7fffffffL).rem(looks.size.toLong()).toInt()]
        return apply(scene.copy(seed = nextSeed, speed = 0.25f + 0.55f * value), look)
    }
}

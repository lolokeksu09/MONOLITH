package studio.nox.model

/** Named color compositions for new shader scenes; no automatic DB inserts. */
data class NamedScenePreset(val name: String, val config: SceneConfig)

object ScenePresets {
    private fun variation(id: String, name: String, primary: Int, secondary: Int,
                          seed: Int, glow: Float, density: Float): NamedScenePreset {
        val source=SceneCatalog.default(id)
        return NamedScenePreset(name,source.copy(
            primary=primary,secondary=secondary,seed=seed,glow=glow,density=density))
    }
    private val definitions: Map<String,List<NamedScenePreset>> = mapOf(
        "AURORA" to listOf(
            variation("AURORA","Polar",0x6DEBFF,0x59FFA9,117,0.65f,0.46f),
            variation("AURORA","Emerald",0x36F5B6,0x1476A9,247,0.72f,0.57f),
            variation("AURORA","Violet Mist",0xAE83FF,0x57DBFF,337,0.55f,0.41f)
        ),
        "PULSE" to listOf(
            variation("PULSE","Cyan Beat",0x30D9FF,0x315EFF,413,0.67f,0.45f),
            variation("PULSE","Crimson Core",0xFF648F,0xFF8E63,587,0.73f,0.54f),
            variation("PULSE","Neon Echo",0xA578FF,0x43FFC5,673,0.62f,0.39f)
        ),
        "COMET" to listOf(
            variation("COMET","Deep Space",0x589DFF,0xA6CFFF,757,0.66f,0.40f),
            variation("COMET","White Tail",0xF1F8FF,0x6FDFFF,839,0.54f,0.36f),
            variation("COMET","Blue Drift",0x30D9FF,0x3952E8,997,0.70f,0.49f)
        )
    )
    fun forScene(sceneId: String): List<NamedScenePreset> = definitions[sceneId].orEmpty()
}

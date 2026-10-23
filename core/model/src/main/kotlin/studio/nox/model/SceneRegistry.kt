package studio.nox.model

/** Immutable scene metadata. Add new modes here and update the shared GPU renderer. */
enum class SceneCategory { SIGNATURE, AMBIENT, TECH, COSMIC }

data class SceneDefinition(val id: String, val category: SceneCategory, val isNew: Boolean = false)

object SceneRegistry {
    val entries: List<SceneDefinition> = listOf(
        SceneDefinition("FLUX",SceneCategory.SIGNATURE),
        SceneDefinition("HALO",SceneCategory.SIGNATURE),
        SceneDefinition("NEBULA",SceneCategory.COSMIC),
        SceneDefinition("AURORA",SceneCategory.AMBIENT,true),
        SceneDefinition("PULSE",SceneCategory.TECH,true),
        SceneDefinition("COMET",SceneCategory.COSMIC,true)
    )
    fun get(id: String): SceneDefinition? = entries.firstOrNull { it.id==id }
}

package studio.nox.model

/**
 * Immutable, bounded editing history. Slider drags remain live in Compose;
 * a completed gesture records only its final scene here.
 */
data class EditHistory private constructor(
    val current: SceneConfig,
    val past: List<SceneConfig>,
    val future: List<SceneConfig>,
    private val maxEntries: Int
) {
    val canUndo: Boolean get() = past.isNotEmpty()
    val canRedo: Boolean get() = future.isNotEmpty()

    fun record(next: SceneConfig): EditHistory {
        val normalized = next.normalized()
        if (normalized == current) return this
        return copy(current = normalized, past = (past + current).takeLast(maxEntries), future = emptyList())
    }

    fun undo(): EditHistory =
        if (!canUndo) this else copy(
            current = past.last(), past = past.dropLast(1),
            future = listOf(current) + future
        )

    fun redo(): EditHistory =
        if (!canRedo) this else copy(
            current = future.first(), past = (past + current).takeLast(maxEntries),
            future = future.drop(1)
        )

    companion object {
        fun start(scene: SceneConfig, maxEntries: Int = 40): EditHistory {
            require(maxEntries in 1..200) { "History limit must be between 1 and 200" }
            return EditHistory(scene.normalized(), emptyList(), emptyList(), maxEntries)
        }
    }
}

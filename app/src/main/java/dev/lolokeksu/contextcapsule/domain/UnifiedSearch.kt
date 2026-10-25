package dev.lolokeksu.contextcapsule.domain

enum class SearchCategory { ALL, NOTES, TASKS }

data class SearchHit(
    val recordId: String,
    val projectId: String,
    val projectTitle: String,
    val text: String,
    val category: SearchCategory,
    val createdAt: Long,
    val pinned: Boolean = false,
    val confirmed: Boolean = false,
    val completed: Boolean = false
)

/** Local-only search. Never indexes or sends project content to an external service. */
object UnifiedSearch {
    fun find(
        projects: List<Project>, notes: List<Decision>, tasks: List<ProjectTask>,
        query: String, category: SearchCategory, includeArchived: Boolean,
        limit: Int = 80
    ): List<SearchHit> {
        val term = query.trim()
        if (term.isEmpty() || limit <= 0) return emptyList()
        val visible = projects.filter { includeArchived || !it.archived }.associateBy { it.id }
        val matchingNotes = if (category == SearchCategory.TASKS) emptyList() else
            notes.mapNotNull { note ->
                val project = visible[note.projectId] ?: return@mapNotNull null
                if (!note.text.contains(term, ignoreCase = true)) return@mapNotNull null
                SearchHit(note.id, project.id, project.title, note.text, SearchCategory.NOTES,
                    note.createdAt, note.pinned, note.evidence == EvidenceKind.OWNER_CONFIRMED)
            }
        val matchingTasks = if (category == SearchCategory.NOTES) emptyList() else
            tasks.mapNotNull { task ->
                val project = visible[task.projectId] ?: return@mapNotNull null
                if (!task.title.contains(term, ignoreCase = true)) return@mapNotNull null
                SearchHit(task.id, project.id, project.title, task.title, SearchCategory.TASKS,
                    task.createdAt, completed = task.completed)
            }
        return (matchingNotes + matchingTasks)
            .sortedWith(compareByDescending<SearchHit> { it.createdAt }
                .thenBy { it.projectId }.thenBy { it.recordId })
            .take(limit)
    }
}

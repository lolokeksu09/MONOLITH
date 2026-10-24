package dev.lolokeksu.contextcapsule.domain

enum class TaskScope { OPEN, ALL, COMPLETED }

data class WorkspaceStats(
    val openTasks: Int, val completedTasks: Int,
    val confirmedNotes: Int, val pendingNotes: Int, val pinnedNotes: Int
) {
    val progressPercent: Int
        get() = if (openTasks + completedTasks == 0) 0
            else completedTasks * 100 / (openTasks + completedTasks)
}

object WorkspaceV9 {
    fun tasks(items: List<ProjectTask>, scope: TaskScope): List<ProjectTask> =
        items.filter {
            when (scope) {
                TaskScope.OPEN -> !it.completed
                TaskScope.COMPLETED -> it.completed
                TaskScope.ALL -> true
            }
        }.sortedWith(compareBy<ProjectTask> { it.completed }.thenBy { it.createdAt }.thenBy { it.id })

    fun summary(tasks: List<ProjectTask>, notes: List<Decision>): WorkspaceStats =
        WorkspaceStats(
            openTasks = tasks.count { !it.completed },
            completedTasks = tasks.count { it.completed },
            confirmedNotes = notes.count { it.evidence == EvidenceKind.OWNER_CONFIRMED },
            pendingNotes = notes.count { it.evidence != EvidenceKind.OWNER_CONFIRMED },
            pinnedNotes = notes.count { it.pinned }
        )
}

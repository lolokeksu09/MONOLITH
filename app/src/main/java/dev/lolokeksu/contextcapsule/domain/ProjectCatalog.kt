package dev.lolokeksu.contextcapsule.domain

/** Pure local project filtering and lifecycle validation. Never alters private records. */
object ProjectCatalog {
    fun visible(items: List<Project>, archived: Boolean, query: String): List<Project> {
        val match = query.trim()
        return items.filter {
            it.archived == archived && (
                match.isEmpty() || it.title.contains(match, ignoreCase = true) ||
                    it.repository?.contains(match, ignoreCase = true) == true
            )
        }.sortedWith(compareByDescending<Project> { it.createdAt }.thenBy { it.id })
    }

    fun repositoryChanged(before: String?, after: String?): Boolean = before != after
    fun confirmDelete(title: String, typed: String): Boolean =
        title.isNotBlank() && title == typed
}

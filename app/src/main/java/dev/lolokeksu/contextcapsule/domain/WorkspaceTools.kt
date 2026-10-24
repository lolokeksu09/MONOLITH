package dev.lolokeksu.contextcapsule.domain

/** Pure, offline-first search and untrusted-share validation. No data mutation. */
object WorkspaceTools {
    fun filterProjects(items: List<Project>, query: String): List<Project> {
        val term = query.trim()
        if (term.isEmpty()) return items
        return items.filter {
            it.title.contains(term, ignoreCase = true) ||
                (it.repository?.contains(term, ignoreCase = true) == true)
        }
    }

    fun filterDecisions(items: List<Decision>, query: String, pendingOnly: Boolean): List<Decision> {
        val term = query.trim()
        return items.filter {
            (!pendingOnly || it.evidence != EvidenceKind.OWNER_CONFIRMED) &&
                (term.isEmpty() || it.text.contains(term, ignoreCase = true))
        }
    }

    fun normalizeSharedText(raw: CharSequence?): String? {
        val text = raw?.toString()?.trim() ?: return null
        return text.takeIf { it.isNotEmpty() && it.length <= 20_000 }
    }
}

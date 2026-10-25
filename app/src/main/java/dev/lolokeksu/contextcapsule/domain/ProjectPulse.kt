package dev.lolokeksu.contextcapsule.domain

/** Explicitly describes changes between two verified HEAD snapshots of one branch. */
data class HeadComparison(
    val code: String, val headline: String, val hasNewCommits: Boolean,
    val oldSha: String?, val newSha: String
)

object ProjectPulse {
    fun compare(oldSha: String?, oldBranch: String?, newSha: String, newBranch: String): HeadComparison {
        val code = when {
            oldSha.isNullOrBlank() -> "first"
            oldBranch != newBranch -> "different_branch"
            oldSha == newSha -> "same"
            else -> "changed"
        }
        val headline = when (code) {
            "first" -> "Первый снимок GitHub"
            "different_branch" -> "Ветка изменилась — сравнение недоступно"
            "same" -> "HEAD не изменился с прошлого обновления"
            else -> "Обнаружены изменения HEAD"
        }
        return HeadComparison(code, headline, code == "changed", oldSha, newSha)
    }

    /** Suggested tasks are opt-in and limited to evidence in the current snapshot. */
    fun suggestions(ciStatus: String?, openPRs: Int?, openIssues: Int?): List<String> = buildList {
        when (ciStatus?.lowercase()) {
            "failure", "timed_out", "cancelled", "action_required" -> add("Проверить ошибки последнего CI")
            "queued", "in_progress", "pending", "waiting", "requested" -> add("Проверить текущий CI")
        }
        if ((openPRs ?: 0) > 0) add("Разобрать открытые PR")
        if ((openIssues ?: 0) > 0) add("Просмотреть открытые Issues")
    }
}

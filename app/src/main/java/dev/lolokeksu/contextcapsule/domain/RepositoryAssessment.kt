package dev.lolokeksu.contextcapsule.domain

data class RepositoryAssessment(
    val code: String,
    val headline: String,
    val openPRs: Int?,
    val openIssues: Int?,
    val recentCommits: Int?
) {
    companion object {
        fun assess(ci: String?, openPRs: Int?, openIssues: Int?, recentCommits: Int?): RepositoryAssessment {
            val code = when (ci?.lowercase()) {
                "failure", "timed_out", "cancelled", "action_required" -> "needs_attention"
                "in_progress", "queued", "waiting", "pending", "requested" -> "in_progress"
                "success" -> "ci_passed"
                else -> "unknown"
            }
            val text = when(code) {
                "needs_attention" -> "Последняя проверка CI требует внимания"
                "in_progress" -> "Проверка CI выполняется"
                "ci_passed" -> "Последний запуск CI успешен"
                else -> "По CI пока нет данных"
            }
            return RepositoryAssessment(code, text, openPRs, openIssues, recentCommits)
        }
    }
}

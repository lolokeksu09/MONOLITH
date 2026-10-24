package dev.lolokeksu.contextcapsule.domain

/** GitHub compare status is not interchangeable with a count of new commits. */
data class ComparisonReading(val headline: String, val filesLabel: String, val warning: Boolean)

object CommitComparison {
    fun missingLabel(): String = "Недоступно"

    fun explain(status: String, ahead: Int, behind: Int,
                total: Int, filesShown: Int, truncated: Boolean): ComparisonReading {
        val state = status.lowercase()
        val headline = when (state) {
            "ahead" -> "Новые коммиты: ${ahead.coerceAtLeast(0)}"
            "identical" -> "Коммиты не изменились"
            "behind" -> "HEAD откатился назад"
            "diverged" -> "Истории ветки разошлись"
            else -> "Статус сравнения неизвестен"
        }
        return ComparisonReading(headline, filesShown.coerceAtLeast(0).toString(),
            truncated || state in setOf("behind", "diverged") ||
                state !in setOf("ahead", "identical", "behind", "diverged"))
    }
}

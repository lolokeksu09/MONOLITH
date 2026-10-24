package dev.lolokeksu.contextcapsule.github

import org.json.JSONObject

data class OverviewEntry(val title: String, val url: String)
data class RepositoryOverview(
    val description: String?,
    val readme: String?,
    val lastCommit: String?,
    val ciStatus: String?,
    val ciLabel: String?,
    val prCount: Int?,
    val issueCount: Int?,
    val commits: List<OverviewEntry>,
    val pullRequests: List<OverviewEntry>,
    val issues: List<OverviewEntry>,
    val previousSha: String? = null,
    val previousBranch: String? = null,
    val previousCheckedAt: Long? = null
)

object RepositoryOverviewCodec {
    private const val MARKER = "cc-overview-v1"
    fun encode(overview: JSONObject): String = overview.put("format", MARKER).toString()
    /** Persist the immediately preceding snapshot without changing the Room schema. */
    fun withPrevious(raw: String, previous: dev.lolokeksu.contextcapsule.domain.GithubSnapshot?): String {
        if (previous == null) return raw
        val objectValue = JSONObject(raw)
        if (objectValue.optString("format") != MARKER) return raw
        objectValue.put("previousSha", previous.sha)
        objectValue.put("previousBranch", previous.branch)
        objectValue.put("previousCheckedAt", previous.checkedAt)
        return objectValue.toString()
    }

    /** Turns the saved structured snapshot into a human handoff, keeping unknowns explicit. */
    fun readable(raw: String): String {
        val overview = decode(raw) ?: return raw
        return buildString {
            overview.description?.let { appendLine("Описание: $it") }
            overview.readme?.let { appendLine("README: $it") }
            overview.ciLabel?.let { appendLine("Последний workflow: $it") }
            appendLine("CI status: ${overview.ciStatus ?: "UNKNOWN"} (не доказательство готовности продукта)")
            appendLine("Открытые PR: ${overview.prCount?.toString() ?: "UNKNOWN"}")
            appendLine("Открытые Issues: ${overview.issueCount?.toString() ?: "UNKNOWN"}")
            overview.lastCommit?.let { appendLine("Последний коммит: $it") }
            for (entry in overview.commits.take(4)) appendLine("Коммит: ${entry.title} — ${entry.url}")
            for (entry in overview.pullRequests.take(3)) appendLine("PR: ${entry.title} — ${entry.url}")
            for (entry in overview.issues.take(3)) appendLine("Issue: ${entry.title} — ${entry.url}")
            appendLine("Значения подсчитаны по доступным ответам API; для более чем 100 открытых элементов данные неполные.")
        }.trim()
    }

    fun decode(raw: String): RepositoryOverview? = runCatching {
        val json = JSONObject(raw)
        if (json.optString("format") != MARKER) return null
        fun readEntries(key: String): List<OverviewEntry> {
            val array = json.optJSONArray(key) ?: return emptyList()
            return (0 until minOf(array.length(), 6)).map { i ->
                val entry = array.getJSONObject(i)
                OverviewEntry(entry.optString("title"), entry.optString("url"))
            }
        }
        RepositoryOverview(
            description = json.optString("description").takeIf { it.isNotBlank() },
            readme = json.optString("readme").takeIf { it.isNotBlank() },
            lastCommit = json.optString("lastCommit").takeIf { it.isNotBlank() },
            ciStatus = json.optString("ciStatus").takeIf { it.isNotBlank() },
            ciLabel = json.optString("ciLabel").takeIf { it.isNotBlank() },
            prCount = if (json.has("prCount") && !json.isNull("prCount")) json.getInt("prCount") else null,
            issueCount = if (json.has("issueCount") && !json.isNull("issueCount")) json.getInt("issueCount") else null,
            commits = readEntries("commits"), pullRequests = readEntries("pullRequests"),
            issues = readEntries("issues"),
            previousSha = json.optString("previousSha").takeIf { it.isNotBlank() },
            previousBranch = json.optString("previousBranch").takeIf { it.isNotBlank() },
            previousCheckedAt = if (json.has("previousCheckedAt") && !json.isNull("previousCheckedAt")) json.getLong("previousCheckedAt") else null
        )
    }.getOrNull()
}

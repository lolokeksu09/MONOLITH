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
    val issues: List<OverviewEntry>
)

object RepositoryOverviewCodec {
    private const val MARKER = "cc-overview-v1"
    fun encode(overview: JSONObject): String = overview.put("format", MARKER).toString()
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
            issues = readEntries("issues")
        )
    }.getOrNull()
}

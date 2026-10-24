package dev.lolokeksu.contextcapsule.github

import dev.lolokeksu.contextcapsule.domain.GithubSnapshot
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class PublicGithubClient {
    private fun get(url: String): String {
        val conn = URL(url).openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        conn.connectTimeout = 9000
        conn.readTimeout = 9000
        conn.setRequestProperty("Accept", "application/vnd.github+json")
        conn.setRequestProperty("User-Agent", "ContextCapsule-Android")
        try {
            val status = conn.responseCode
            if (status !in 200..299) throw IllegalStateException("GitHub HTTP $status. Check repository, access and API limits.")
            return conn.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
        } finally { conn.disconnect() }
    }

    suspend fun sync(projectId: String, repository: String): GithubSnapshot = withContext(Dispatchers.IO) {
        require(Regex("[A-Za-z0-9_.-]+/[A-Za-z0-9_.-]+").matches(repository)) { "Invalid GitHub owner/repository" }
        val base = "https://api.github.com/repos/$repository"
        val branch = JSONObject(get(base)).getString("default_branch")
        val head = JSONObject(get("$base/branches/$branch")).getJSONObject("commit").getString("sha")
        val warnings = mutableListOf<String>()
        val summary = mutableListOf<String>()
        fun requestArray(path: String, key: String? = null): JSONArray? =
            try {
                val raw = get("$base/$path")
                if (key == null) JSONArray(raw) else JSONObject(raw).getJSONArray(key)
            } catch (ex: Exception) { warnings.add("$path unavailable: ${ex.message}"); null }

        requestArray("commits?per_page=5")?.let { values ->
            val recent = (0 until values.length()).map { i ->
                val obj = values.getJSONObject(i)
                obj.getString("sha").take(8) + " " +
                    obj.getJSONObject("commit").getString("message").lineSequence().first().take(90)
            }
            summary.add("Recent commits: ${recent.joinToString(" | ")}")
        }
        requestArray("pulls?state=open&per_page=10")?.let { values ->
            summary.add("Open PRs (first 10): ${values.length()}")
        }
        requestArray("actions/runs?per_page=5", "workflow_runs")?.let { values ->
            val runs = (0 until values.length()).map { i ->
                val run = values.getJSONObject(i)
                "${run.optString("name")}: ${run.optString("conclusion", run.optString("status"))}"
            }
            summary.add("Workflow runs: ${runs.joinToString(" | ")}")
        }
        GithubSnapshot(projectId, branch, head, "https://github.com/$repository/commit/$head",
            System.currentTimeMillis(), summary.joinToString("\n"),
            warnings.takeIf { it.isNotEmpty() }?.joinToString("; "))
    }
}

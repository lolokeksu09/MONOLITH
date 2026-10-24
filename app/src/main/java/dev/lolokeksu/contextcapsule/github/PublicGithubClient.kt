package dev.lolokeksu.contextcapsule.github

import android.util.Base64
import dev.lolokeksu.contextcapsule.domain.GithubSnapshot
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
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
            if (status !in 200..299) throw IllegalStateException("GitHub HTTP $status. Проверь адрес и лимиты API.")
            return conn.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
        } finally { conn.disconnect() }
    }

    suspend fun sync(
        projectId: String, repository: String, previous: GithubSnapshot? = null
    ): GithubSnapshot = withContext(Dispatchers.IO) {
        require(Regex("[A-Za-z0-9_.-]+/[A-Za-z0-9_.-]+").matches(repository)) { "Неверный GitHub owner/repository" }
        val base = "https://api.github.com/repos/$repository"
        val root = JSONObject(get(base))
        val branch = root.getString("default_branch")
        val head = JSONObject(get("$base/branches/${URLEncoder.encode(branch, "UTF-8")}"))
            .getJSONObject("commit").getString("sha")
        val warnings = mutableListOf<String>()
        val data = JSONObject()
        data.put("description", root.optString("description").takeIf { it != "null" } ?: "")

        fun array(path: String, key: String? = null): JSONArray? = try {
            val raw = get("$base/$path")
            if (key == null) JSONArray(raw) else JSONObject(raw).getJSONArray(key)
        } catch (ex: Exception) {
            warnings.add("Не удалось загрузить $path: ${ex.message}")
            null
        }
        fun entry(title: String, url: String): JSONObject =
            JSONObject().put("title", title.take(180)).put("url", url)

        val commits = array("commits?sha=${URLEncoder.encode(branch, "UTF-8")}&per_page=6")
        if (commits != null) {
            val list = JSONArray()
            for (i in 0 until commits.length()) {
                val item = commits.getJSONObject(i)
                val title = item.getJSONObject("commit").getString("message").lineSequence().first().take(140)
                if (i == 0) data.put("lastCommit", title)
                list.put(entry(title, item.optString("html_url")))
            }
            data.put("commits", list)
        }

        val pulls = array("pulls?state=open&per_page=100")
        if (pulls != null) {
            data.put("prCount", pulls.length())
            data.put("pullRequests", JSONArray().apply {
                for (i in 0 until minOf(3, pulls.length())) {
                    val item = pulls.getJSONObject(i)
                    put(entry("#${item.optInt("number")} ${item.optString("title")}", item.optString("html_url")))
                }
            })
            if (pulls.length() == 100) warnings.add("PR: показаны первые 100, полный подсчёт недоступен")
        }

        val issues = array("issues?state=open&per_page=100")
        if (issues != null) {
            val realIssues = (0 until issues.length()).map { issues.getJSONObject(it) }
                .filterNot { it.has("pull_request") }
            data.put("issueCount", realIssues.size)
            data.put("issues", JSONArray().apply {
                realIssues.take(3).forEach { put(entry("#${it.optInt("number")} ${it.optString("title")}", it.optString("html_url"))) }
            })
            if (issues.length() == 100) warnings.add("Issues: показаны первые 100, полный подсчёт недоступен")
        }

        val runs = array("actions/runs?branch=${URLEncoder.encode(branch, "UTF-8")}&per_page=5", "workflow_runs")
        if (runs != null && runs.length() > 0) {
            val first = runs.getJSONObject(0)
            data.put("ciLabel", first.optString("name"))
            val state = first.optString("status")
            val conclusion = first.optString("conclusion")
            data.put("ciStatus", if (state == "completed") conclusion.takeIf { it.isNotBlank() && it != "null" } ?: "unknown" else state)
        }

        // Read-only GitHub comparison: only valid SHA values on the same branch.
        // Never claim two diverged histories are ordinary new commits.
        val old = previous?.sha
        if (old != null && previous.branch == branch && old != head &&
            Regex("[a-fA-F0-9]{40}").matches(old) && Regex("[a-fA-F0-9]{40}").matches(head)) {
            try {
                val compare = JSONObject(get("$base/compare/$old...$head"))
                val reportedFiles = compare.optJSONArray("files")
                val shown = reportedFiles?.length() ?: 0
                val sample = JSONArray()
                if (reportedFiles != null) {
                    for (i in 0 until minOf(8, shown)) {
                        val obj = reportedFiles.getJSONObject(i)
                        sample.put(JSONObject()
                            .put("path", obj.optString("filename").take(240))
                            .put("status", obj.optString("status").take(40))
                            .put("added", obj.optInt("additions", 0).coerceAtLeast(0))
                            .put("deleted", obj.optInt("deletions", 0).coerceAtLeast(0)))
                    }
                }
                data.put("comparison", JSONObject()
                    .put("status", compare.optString("status", "unknown"))
                    .put("aheadBy", compare.optInt("ahead_by", 0).coerceAtLeast(0))
                    .put("behindBy", compare.optInt("behind_by", 0).coerceAtLeast(0))
                    .put("filesShown", shown)
                    .put("truncated", shown >= 300)
                    .put("url", "https://github.com/$repository/compare/$old...$head")
                    .put("files", sample))
                if (shown >= 300) warnings.add("GitHub ограничивает список изменённых файлов; показана часть")
            } catch (ex: Exception) {
                warnings.add("Сравнение двух коммитов недоступно: ${ex.message}")
            }
        }

        try {
            val readme = JSONObject(get("$base/readme"))
            val raw = readme.optString("content").replace("\n", "")
            if (raw.length < 180_000 && readme.optString("encoding") == "base64") {
                val decoded = String(Base64.decode(raw, Base64.DEFAULT), Charsets.UTF_8)
                val first = decoded.lineSequence().map { it.trim() }
                    .firstOrNull { it.isNotBlank() && !it.startsWith("#") && !it.startsWith("![") &&
                        !it.startsWith("[![") && !it.startsWith("<!--") && !it.startsWith("<") &&
                        !it.startsWith("---") && it.length > 12 }
                if (first != null) data.put("readme", first.take(210))
            }
        } catch (ex: Exception) {
            warnings.add("README недоступен: ${ex.message}")
        }

        GithubSnapshot(projectId, branch, head,
            "https://github.com/$repository/commit/$head", System.currentTimeMillis(),
            RepositoryOverviewCodec.encode(data),
            warnings.takeIf { it.isNotEmpty() }?.joinToString("; "))
    }
}

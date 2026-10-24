package dev.lolokeksu.contextcapsule.domain

import java.net.URI

data class ProjectDraft(val title: String, val repository: String?)

/** Pure validation shared by the UI and persistence boundary. */
object ProjectInput {
    const val REPOSITORY_ERROR = "Укажи GitHub в виде owner/repo или https://github.com/owner/repo"
    private val slugPattern = Regex("[A-Za-z0-9_.-]+/[A-Za-z0-9_.-]+")

    fun parse(rawTitle: String, rawRepository: String): ProjectDraft {
        val title = rawTitle.trim()
        require(title.isNotEmpty() && title.length <= 100) {
            "Название проекта должно содержать от 1 до 100 символов"
        }
        val input = rawRepository.trim()
        if (input.isEmpty()) return ProjectDraft(title, null)

        val path = if (input.startsWith("https://", ignoreCase = true)) {
            val uri = runCatching { URI(input) }.getOrNull()
            require(
                uri != null && uri.scheme.equals("https", ignoreCase = true) &&
                    uri.host?.lowercase() in setOf("github.com", "www.github.com") &&
                    uri.userInfo == null && uri.port == -1 &&
                    uri.rawQuery == null && uri.rawFragment == null
            ) { REPOSITORY_ERROR }
            uri!!.path.trim('/').removeSuffix(".git")
        } else if (input.startsWith("github.com/", ignoreCase = true)) {
            input.substringAfter('/').trim('/').removeSuffix(".git")
        } else {
            input.trim('/').removeSuffix(".git")
        }
        require(slugPattern.matches(path) && !path.contains("..")) { REPOSITORY_ERROR }
        return ProjectDraft(title, path)
    }
}

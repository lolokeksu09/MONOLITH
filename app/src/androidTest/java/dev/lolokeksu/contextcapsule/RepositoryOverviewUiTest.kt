package dev.lolokeksu.contextcapsule

import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.lolokeksu.contextcapsule.domain.GithubSnapshot
import dev.lolokeksu.contextcapsule.github.RepositoryOverviewCodec
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RepositoryOverviewUiTest {
    @get:Rule val rule = createComposeRule()

    @Test fun rendersVerifiedGitHubSummaryAndKeepsMissingDataUnknown() {
        val json = JSONObject()
            .put("description", "Community project")
            .put("ciStatus", "success")
            .put("ciLabel", "Android")
            .put("prCount", 2)
            .put("issueCount", 4)
            .put("lastCommit", "Improve routing")
            .put("commits", JSONArray().put(JSONObject().put("title", "Improve routing")
                .put("url", "https://github.com/o/r/commit/abc123")))
        val snapshot = GithubSnapshot("p", "main", "abc123", "https://github.com/o/r/commit/abc123",
            1000L, RepositoryOverviewCodec.encode(json), null)
        rule.setContent {
            RepositoryOverviewPanel(snapshot, true, false, {})
        }
        rule.onNodeWithText("Последний запуск CI успешен").assertExists()
        rule.onNodeWithText("Community project").assertExists()
        rule.onNodeWithText("2").assertExists()
        rule.onNodeWithText("4").assertExists()
    }

    @Test fun missingSnapshotIsNotCalledHealthy() {
        rule.setContent { RepositoryOverviewPanel(null, true, false, {}) }
        rule.onNodeWithText("Данных ещё нет").assertExists()
    }
}

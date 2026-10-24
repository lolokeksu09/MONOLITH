package dev.lolokeksu.contextcapsule

import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.lolokeksu.contextcapsule.domain.Project
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProjectManagementUiTest {
    @get:Rule val rule = createComposeRule()
    private val project = Project("p", "My Project", "owner/repo", 1)

    @Test fun deletionRequiresExactNameAndExplicitAcknowledgement() {
        var deleted: String? = null
        rule.setContent { ProjectDeleteDialog(project, onDelete = { deleted = it }, onDismiss = {}) }
        rule.onNodeWithTag("confirm_delete_project").assertIsNotEnabled()
        rule.onNodeWithTag("confirm_project_name").performTextReplacement("My Project")
        rule.onNodeWithTag("confirm_delete_project").assertIsNotEnabled()
        rule.onNodeWithTag("delete_acknowledgement").performClick()
        rule.onNodeWithTag("confirm_delete_project").performClick()
        rule.runOnIdle { assertEquals("My Project", deleted) }
    }

    @Test fun settingsNormalizePublicGithubLinkWithoutDeletingContent() {
        var saved: Pair<String, String>? = null
        rule.setContent {
            ProjectSettingsDialog(project, false, onSave = { name, repo -> saved = name to repo },
                onArchive = {}, onBackup = {}, onRequestDelete = {}, onDismiss = {})
        }
        rule.onNodeWithTag("settings_project_name").performTextReplacement("Updated")
        rule.onNodeWithTag("settings_project_repo")
            .performTextReplacement("https://github.com/newowner/newrepo")
        rule.onNodeWithTag("settings_save").performClick()
        rule.runOnIdle { assertEquals("Updated" to "newowner/newrepo", saved) }
    }
}

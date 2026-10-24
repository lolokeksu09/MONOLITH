package dev.lolokeksu.contextcapsule

import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.onAllNodes
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CreateProjectSmokeTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()

    @Test fun createLocalProjectPersistAndShowInDashboard() {
        val project = "Smoke ${System.currentTimeMillis()}"
        rule.onNodeWithText("Создать проект").performClick()
        rule.onAllNodes(hasSetTextAction())[0].performTextInput(project)
        rule.onNodeWithText("Создать", useUnmergedTree = true).performClick()
        rule.waitUntil(20_000) {
            rule.onAllNodesWithText(project).fetchSemanticsNodes().isNotEmpty()
        }
        rule.onNodeWithText("Все проекты").performClick()
        rule.waitUntil(15_000) {
            rule.onAllNodesWithText(project).fetchSemanticsNodes().isNotEmpty()
        }
        rule.onNodeWithText(project).assertExists()
    }

    @Test fun invalidGithubUrlMustKeepFormVisibleWithError() {
        rule.onNodeWithText("Создать проект").performClick()
        rule.onAllNodes(hasSetTextAction())[0].performTextInput("Bad repo")
        rule.onAllNodes(hasSetTextAction())[1].performTextInput("https://evil.example/name/repo")
        rule.onNodeWithText("Создать", useUnmergedTree = true).performClick()
        rule.onNodeWithText("Новый проект").assertExists()
        rule.onNodeWithText("Укажи GitHub в виде owner/repo или https://github.com/owner/repo").assertExists()
    }
}

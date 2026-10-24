package dev.lolokeksu.contextcapsule

import androidx.compose.ui.test.onNodeWithTag
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
        rule.onNodeWithTag("project_name").performTextInput(project)
        rule.onNodeWithTag("confirm_create").performClick()
        // Project name already exists in the form input; wait for actual navigation.
        rule.waitUntil(20_000) {
            rule.onAllNodesWithText("Все проекты").fetchSemanticsNodes().isNotEmpty()
        }
        rule.onNodeWithText("Все проекты").performClick()
        rule.waitUntil(15_000) {
            rule.onAllNodesWithText(project).fetchSemanticsNodes().isNotEmpty()
        }
        rule.onNodeWithText(project).assertExists()
    }

    @Test fun projectScreenSeparatesOverviewAndMemoryButKeepsHandoffAccessible() {
        val project = "Design ${System.currentTimeMillis()}"
        rule.onNodeWithText("Создать проект").performClick()
        rule.onNodeWithTag("project_name").performTextInput(project)
        rule.onNodeWithTag("confirm_create").performClick()
        rule.waitUntil(20_000) {
            rule.onAllNodesWithText("Все проекты").fetchSemanticsNodes().isNotEmpty()
        }
        rule.onNodeWithText("Обзор", useUnmergedTree = true).assertExists()
        rule.onNodeWithText("Память", useUnmergedTree = true).assertExists()
        rule.onNodeWithText("Важные решения").assertDoesNotExist()
        rule.onNodeWithTag("handoff_copy").assertExists()
        rule.onNodeWithText("Память", useUnmergedTree = true).performClick()
        rule.onNodeWithText("Важные решения").assertExists()
        rule.onNodeWithText("Следующие шаги").assertDoesNotExist()
        rule.onNodeWithTag("handoff_copy").assertExists()
        rule.onNodeWithText("План", useUnmergedTree = true).performClick()
        rule.onNodeWithText("Следующие шаги").assertExists()
        rule.onNodeWithTag("handoff_copy").assertExists()
    }

    @Test fun invalidGithubUrlMustKeepFormVisibleWithError() {
        rule.onNodeWithText("Создать проект").performClick()
        rule.onNodeWithTag("project_name").performTextInput("Bad repo")
        rule.onNodeWithTag("project_repo").performTextInput("https://evil.example/name/repo")
        rule.onNodeWithTag("confirm_create").performClick()
        rule.onNodeWithText("Новый проект").assertExists()
        rule.onNodeWithText("Укажи GitHub в виде owner/repo или https://github.com/owner/repo").assertExists()
    }
}

package dev.lolokeksu.contextcapsule

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.lolokeksu.contextcapsule.domain.ProjectTask
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TaskBoardTest {
    @get:Rule val rule = createComposeRule()

    @Test fun openAndDoneTasksAreSeparateAndTaskCreationIsExplicit() {
        val records = listOf(
            ProjectTask("a", "p", "Исправить баг", false, 1),
            ProjectTask("b", "p", "Прошлый релиз", true, 2)
        )
        var added: String? = null
        rule.setContent {
            TaskBoard(records, false, onAdd = { added = it },
                onToggle = { _, _ -> }, onEdit = { _, _ -> }, onDelete = {})
        }
        rule.onNodeWithText("Исправить баг").assertExists()
        rule.onNodeWithText("Прошлый релиз").assertDoesNotExist()
        rule.onNodeWithText("Готово", useUnmergedTree = true).performClick()
        rule.onNodeWithText("Прошлый релиз").assertExists()
        rule.onNodeWithText("Исправить баг").assertDoesNotExist()
        rule.onNodeWithText("Открыто", useUnmergedTree = true).performClick()
        rule.onNodeWithTag("task_quick_add").performTextInput("Новая задача")
        rule.onNodeWithTag("task_submit").performClick()
        rule.runOnIdle { org.junit.Assert.assertEquals("Новая задача", added) }
    }
}

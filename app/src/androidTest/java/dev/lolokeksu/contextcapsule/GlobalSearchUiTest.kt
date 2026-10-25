package dev.lolokeksu.contextcapsule

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.lolokeksu.contextcapsule.domain.SearchCategory
import dev.lolokeksu.contextcapsule.domain.SearchHit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GlobalSearchUiTest {
    @get:Rule val rule = createComposeRule()

    @Test fun searchResultOpensExactProjectAndRecordOnExplicitTap() {
        val hit = SearchHit(
            "note-1", "project-1", "Проект А", "Проверить дизайн",
            SearchCategory.NOTES, 12L, confirmed = false
        )
        var selected: SearchHit? = null
        rule.setContent {
            GlobalSearchDialog(
                results = listOf(hit),
                onSearch = { _, _, _ -> },
                onPick = { selected = it },
                onDismiss = {}
            )
        }
        rule.onNodeWithTag("global_search_field").performTextInput("дизайн")
        rule.onNodeWithText("Проект А").assertExists()
        rule.onNodeWithTag("global_result_note-1").performClick()
        rule.runOnIdle { assertEquals("note-1", selected?.recordId) }
        rule.runOnIdle { assertEquals("project-1", selected?.projectId) }
    }

    @Test fun resultCannotBeSelectedBeforeQueryIsEntered() {
        var selected: SearchHit? = null
        rule.setContent {
            GlobalSearchDialog(
                results = listOf(SearchHit("task", "p", "Project", "Task",
                    SearchCategory.TASKS, 1L)),
                onSearch = { _, _, _ -> },
                onPick = { selected = it },
                onDismiss = {}
            )
        }
        rule.onNodeWithTag("global_result_task").assertDoesNotExist()
        rule.runOnIdle { assertNull(selected) }
    }
}

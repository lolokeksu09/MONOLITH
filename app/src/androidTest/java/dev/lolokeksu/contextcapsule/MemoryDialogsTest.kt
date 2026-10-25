package dev.lolokeksu.contextcapsule

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MemoryDialogsTest {
    @get:Rule val compose = createComposeRule()

    @Test fun cancelDeleteKeepsDataUntouched() {
        var removed = false
        var cancelled = false
        compose.setContent {
            MemoryDeleteDialog("заметку", "Важный текст", "confirm_delete_note",
                onDismiss = { cancelled = true }, onDelete = { removed = true })
        }
        compose.onNodeWithText("Удалить заметку?").assertExists()
        compose.onNodeWithText("Отмена").performClick()
        compose.runOnIdle {
            assertFalse(removed)
            assertTrue(cancelled)
        }
    }

    @Test fun deleteOnlyOnExplicitConfirmation() {
        var removed = false
        compose.setContent {
            MemoryDeleteDialog("задачу", "Задача", "confirm_delete_task",
                onDismiss = {}, onDelete = { removed = true })
        }
        compose.runOnIdle { assertFalse(removed) }
        compose.onNodeWithTag("confirm_delete_task").performClick()
        compose.runOnIdle { assertTrue(removed) }
    }

    @Test fun editRequiresNewNonblankTextAndSendsValidatedValue() {
        var saved = ""
        compose.setContent {
            MemoryEditDialog("Изменить заметку", "Исходный текст", 20_000,
                "confirm_edit_note", onDismiss = {}, onSave = { saved = it })
        }
        compose.onNodeWithTag("confirm_edit_note").assertIsNotEnabled()
        compose.onNodeWithTag("edit_field").performTextReplacement("  Новый текст  ")
        compose.onNodeWithTag("confirm_edit_note").performClick()
        compose.runOnIdle { assertEquals("Новый текст", saved) }
    }
}

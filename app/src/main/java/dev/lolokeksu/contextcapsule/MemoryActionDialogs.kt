package dev.lolokeksu.contextcapsule

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

@Composable
fun MemoryEditDialog(
    heading: String,
    initial: String,
    maxLength: Int,
    tag: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var draft by remember(initial) { mutableStateOf(initial) }
    val normalized = draft.trim()
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(22.dp),
        title = { Text(heading) },
        text = {
            Column {
                OutlinedTextField(
                    value = draft,
                    onValueChange = { if (it.length <= maxLength) draft = it },
                    modifier = Modifier.fillMaxWidth().testTag("edit_field"),
                    label = { Text("Текст записи") },
                    minLines = if (maxLength > 500) 3 else 1,
                    maxLines = if (maxLength > 500) 7 else 3
                )
                Spacer(Modifier.height(6.dp))
                Text("${draft.length} / $maxLength", style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (maxLength > 500) {
                    Spacer(Modifier.height(8.dp))
                    Text("Если изменить подтверждённое решение, его потребуется подтвердить заново.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(normalized) },
                enabled = normalized.isNotEmpty() && normalized != initial && normalized.length <= maxLength,
                modifier = Modifier.testTag(tag)
            ) { Text("Сохранить") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }
    )
}

@Composable
fun MemoryDeleteDialog(
    itemType: String,
    preview: String,
    tag: String,
    onDismiss: () -> Unit,
    onDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(22.dp),
        title = { Text("Удалить $itemType?") },
        text = {
            Column {
                Text(preview.take(160), style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(10.dp))
                Text("Это действие нельзя отменить. Данные будут удалены только после подтверждения.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        confirmButton = {
            TextButton(onClick = onDelete, modifier = Modifier.testTag(tag)) {
                Text("Удалить", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }
    )
}

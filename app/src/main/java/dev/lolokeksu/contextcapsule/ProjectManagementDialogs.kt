package dev.lolokeksu.contextcapsule

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.lolokeksu.contextcapsule.domain.Project
import dev.lolokeksu.contextcapsule.domain.ProjectCatalog
import dev.lolokeksu.contextcapsule.domain.ProjectInput

@Composable
fun ProjectSettingsDialog(
    project: Project,
    busy: Boolean,
    onSave: (String, String) -> Unit,
    onArchive: (Boolean) -> Unit,
    onBackup: () -> Unit,
    onRequestDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember(project.id) { mutableStateOf(project.title) }
    var repository by remember(project.id) { mutableStateOf(project.repository.orEmpty()) }
    var error by remember(project.id) { mutableStateOf<String?>(null) }
    val edited = title.trim() != project.title ||
        repository.trim() != project.repository.orEmpty()
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(23.dp),
        title = { Text("Управление проектом", fontWeight = FontWeight.SemiBold) },
        text = {
            Column(Modifier.heightIn(max = 480.dp).verticalScroll(rememberScrollState())) {
                Text("ОСНОВНЫЕ ДАННЫЕ", color = Color(PremiumPalette.Blue.toInt()),
                    fontSize = 11.sp, letterSpacing = 1.2.sp)
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = title, onValueChange = { title = it; error = null },
                    label = { Text("Название") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("settings_project_name")
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = repository, onValueChange = { repository = it; error = null },
                    label = { Text("Публичный GitHub (необязательно)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("settings_project_repo")
                )
                error?.let {
                    Spacer(Modifier.height(7.dp))
                    Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }
                Spacer(Modifier.height(9.dp))
                Text("При смене репозитория старый снимок GitHub сбрасывается. Заметки и задачи остаются.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(22.dp))
                HorizontalDivider()
                Spacer(Modifier.height(14.dp))
                Text("ХРАНЕНИЕ И АРХИВ", color = Color(PremiumPalette.Blue.toInt()),
                    fontSize = 11.sp, letterSpacing = 1.2.sp)
                Spacer(Modifier.height(10.dp))
                OutlinedButton(
                    onClick = onBackup,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(13.dp),
                    border = BorderStroke(1.dp, Color(PremiumPalette.Outline.toInt()))
                ) { Text("Сохранить JSON-копию") }
                Spacer(Modifier.height(7.dp))
                OutlinedButton(
                    onClick = { onArchive(!project.archived); onDismiss() },
                    enabled = !busy,
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("settings_archive"),
                    shape = RoundedCornerShape(13.dp)
                ) { Text(if (project.archived) "Вернуть из архива" else "Переместить в архив") }
                Spacer(Modifier.height(9.dp))
                TextButton(
                    onClick = { onDismiss(); onRequestDelete() },
                    enabled = !busy,
                    modifier = Modifier.fillMaxWidth().testTag("settings_delete")
                ) { Text("Удалить проект навсегда", color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            TextButton(
                enabled = !busy && edited && title.isNotBlank(),
                modifier = Modifier.testTag("settings_save"),
                onClick = {
                    runCatching { ProjectInput.parse(title, repository) }
                        .onSuccess {
                            onSave(it.title, it.repository.orEmpty())
                            onDismiss()
                        }
                        .onFailure { error = it.message ?: "Недопустимые данные" }
                }
            ) { Text("Сохранить") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Закрыть") } }
    )
}

@Composable
fun ProjectDeleteDialog(project: Project, onDelete: (String) -> Unit, onDismiss: () -> Unit) {
    var name by remember(project.id) { mutableStateOf("") }
    var understood by remember(project.id) { mutableStateOf(false) }
    val ready = ProjectCatalog.confirmDelete(project.title, name) && understood
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(22.dp),
        title = { Text("Удалить проект и все данные?") },
        text = {
            Column {
                Text("Будут удалены заметки, задачи и снимок GitHub. Это действие нельзя отменить.",
                    style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(12.dp))
                Text("Сначала сохрани резервную JSON-копию. Для подтверждения введи полное название:",
                    style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(7.dp))
                Text(project.title, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = name, onValueChange = { name = it },
                    singleLine = true, label = { Text("Название проекта") },
                    modifier = Modifier.fillMaxWidth().testTag("confirm_project_name")
                )
                Spacer(Modifier.height(12.dp))
                Row {
                    Checkbox(checked = understood, onCheckedChange = { understood = it },
                        modifier = Modifier.testTag("delete_acknowledgement"))
                    Spacer(Modifier.width(5.dp))
                    Text("Я понимаю, что локальные данные будут удалены без возможности отмены.",
                        style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onDelete(name) }, enabled = ready,
                modifier = Modifier.testTag("confirm_delete_project")) {
                Text("Удалить навсегда", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }
    )
}

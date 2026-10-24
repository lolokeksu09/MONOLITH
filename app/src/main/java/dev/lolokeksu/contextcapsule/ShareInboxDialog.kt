package dev.lolokeksu.contextcapsule

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.LibraryAdd
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.lolokeksu.contextcapsule.domain.Project

/** Shared text is only persisted after explicit project selection. */
@Composable
fun ShareInboxDialog(
    text: String,
    projects: List<Project>,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val blue = Color(PremiumPalette.Blue.toInt())
    val white = Color(PremiumPalette.Title.toInt())
    val secondary = Color(PremiumPalette.Secondary.toInt())
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(PremiumPalette.Panel.toInt()),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LibraryAdd, contentDescription = null, tint = blue)
                Spacer(Modifier.width(10.dp))
                Text("Сохранить в память", fontWeight = FontWeight.SemiBold, color = white)
            }
        },
        text = {
            Column {
                Text("Отправленный текст", color = secondary, fontSize = 12.sp)
                Spacer(Modifier.height(8.dp))
                Surface(
                    color = Color(PremiumPalette.PanelElevated.toInt()),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text, color = white, maxLines = 5, overflow = TextOverflow.Ellipsis,
                        fontSize = 13.sp, lineHeight = 19.sp, modifier = Modifier.padding(13.dp))
                }
                Spacer(Modifier.height(17.dp))
                Text("Выбери проект", color = secondary, fontSize = 12.sp)
                Spacer(Modifier.height(5.dp))
                if (projects.isEmpty()) {
                    Text("Пока нет проектов. Создай проект в приложении и повтори отправку.",
                        color = white, fontSize = 13.sp)
                } else {
                    Column(Modifier.heightIn(max = 230.dp).verticalScroll(rememberScrollState())) {
                        projects.forEach { project ->
                            TextButton(
                                onClick = { onSave(project.id) },
                                modifier = Modifier.fillMaxWidth(),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp)
                            ) {
                                Column(Modifier.weight(1f), horizontalAlignment = Alignment.Start) {
                                    Text(project.title, color = white, maxLines = 1,
                                        overflow = TextOverflow.Ellipsis, fontSize = 15.sp)
                                    Text(project.repository ?: "Локальный проект", color = secondary,
                                        maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 11.sp)
                                }
                                Icon(Icons.Default.ArrowForward, contentDescription = "Сохранить сюда", tint = blue)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                Text("Новая заметка будет помечена как неподтверждённая.",
                    color = secondary, fontSize = 11.sp, lineHeight = 15.sp)
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Отмена", color = secondary) } }
    )
}

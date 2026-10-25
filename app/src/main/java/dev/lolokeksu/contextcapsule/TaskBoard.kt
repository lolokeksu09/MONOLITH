package dev.lolokeksu.contextcapsule

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.lolokeksu.contextcapsule.domain.ProjectTask
import dev.lolokeksu.contextcapsule.domain.TaskScope
import dev.lolokeksu.contextcapsule.domain.WorkspaceV9

private val PlanBlue = Color(PremiumPalette.Blue.toInt())
private val PlanInk = Color(PremiumPalette.Background.toInt())
private val PlanWhite = Color(PremiumPalette.Title.toInt())
private val PlanSecondary = Color(PremiumPalette.Secondary.toInt())
private val PlanLine = Color(PremiumPalette.Outline.toInt())

@Composable
fun TaskBoard(
    tasks: List<ProjectTask>,
    busy: Boolean,
    onAdd: (String) -> Unit,
    onToggle: (String, Boolean) -> Unit,
    onEdit: (String, String) -> Unit,
    onDelete: (String) -> Unit,
    focusTaskId: String? = null
) {
    var scope by remember(focusTaskId) {
        mutableStateOf(if (focusTaskId == null) TaskScope.OPEN else TaskScope.ALL)
    }
    var focusOnly by remember(focusTaskId) { mutableStateOf(focusTaskId != null) }
    var draft by remember { mutableStateOf("") }
    var editing by remember { mutableStateOf<ProjectTask?>(null) }
    var deleting by remember { mutableStateOf<ProjectTask?>(null) }
    val open = tasks.count { !it.completed }
    val done = tasks.count { it.completed }
    val total = tasks.size
    val visible = WorkspaceV9.tasks(tasks, scope).let { selected ->
        if (focusOnly && focusTaskId != null)
            tasks.filter { it.id == focusTaskId }
        else selected
    }
    Column(Modifier.fillMaxWidth()) {
        Text("ПЛАН РАБОТЫ", color = PlanBlue, letterSpacing = 1.5.sp, fontSize = 11.sp,
            fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(7.dp))
        Text("Следующие шаги", color = PlanWhite, fontSize = 25.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(7.dp))
        Text("Отслеживай задачи отдельно от решений. Отметки и изменения сохраняются на устройстве.",
            color = PlanSecondary, fontSize = 13.sp, lineHeight = 19.sp)
        Spacer(Modifier.height(18.dp))
        Surface(shape = RoundedCornerShape(20.dp),
            color = Color(0xFF162439), border = BorderStroke(1.dp, PlanLine)) {
            Column(Modifier.fillMaxWidth().padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Выполнено", color = PlanSecondary, fontSize = 12.sp)
                        Text("$done из $total", color = PlanWhite, fontSize = 26.sp,
                            fontWeight = FontWeight.SemiBold)
                    }
                    Text(if (total == 0) "Нет задач" else "${done * 100 / total}%",
                        color = PlanBlue, fontSize = 23.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { if (total == 0) 0f else done.toFloat() / total },
                    modifier = Modifier.fillMaxWidth().height(7.dp),
                    color = Color(PremiumPalette.Teal.toInt()),
                    trackColor = Color(0xFF2D3C52)
                )
            }
        }
        Spacer(Modifier.height(20.dp))
        Surface(color = Color(PremiumPalette.Panel.toInt()),
            shape = RoundedCornerShape(19.dp), border = BorderStroke(1.dp, PlanLine)) {
            Column(Modifier.padding(15.dp)) {
                Text("БЫСТРАЯ ЗАДАЧА", color = PlanSecondary, fontSize = 11.sp, letterSpacing = 1.sp)
                Spacer(Modifier.height(7.dp))
                OutlinedTextField(
                    value = draft, onValueChange = { if (it.length <= 500) draft = it },
                    label = { Text("Что сделать дальше?") },
                    modifier = Modifier.fillMaxWidth().testTag("task_quick_add"),
                    shape = RoundedCornerShape(12.dp), singleLine = true
                )
                Spacer(Modifier.height(9.dp))
                Button(
                    onClick = { onAdd(draft.trim()); draft = "" },
                    enabled = draft.isNotBlank() && !busy,
                    modifier = Modifier.fillMaxWidth().height(46.dp).testTag("task_submit"),
                    shape = RoundedCornerShape(13.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PlanBlue, contentColor = PlanInk)
                ) { Text("Добавить задачу", fontWeight = FontWeight.SemiBold) }
            }
        }
        Spacer(Modifier.height(20.dp))
        if (focusOnly && focusTaskId != null) {
            Surface(
                color = Color(0xFF182C45), shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, PlanLine)
            ) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 13.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Text("Найденная задача", color = PlanWhite, fontSize = 12.sp,
                        modifier = Modifier.weight(1f))
                    TextButton(
                        onClick = { focusOnly = false },
                        modifier = Modifier.testTag("show_all_tasks")
                    ) { Text("Все задачи", color = PlanBlue) }
                }
            }
            Spacer(Modifier.height(11.dp))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(7.dp), modifier = Modifier.fillMaxWidth()) {
            listOf(
                TaskScope.OPEN to "Открыто",
                TaskScope.ALL to "Все",
                TaskScope.COMPLETED to "Готово"
            ).forEach { (value, title) ->
                FilterChip(
                    selected = scope == value,
                    onClick = { scope = value },
                    label = { Text(title) },
                    modifier = Modifier.weight(1f),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF274364), selectedLabelColor = PlanWhite
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true, selected = scope == value,
                        borderColor = PlanLine, selectedBorderColor = PlanBlue
                    )
                )
            }
        }
        Spacer(Modifier.height(11.dp))
        Text(
            when (scope) {
                TaskScope.OPEN -> "Активные · $open"
                TaskScope.ALL -> "Все задачи · $total"
                TaskScope.COMPLETED -> "Завершённые · $done"
            },
            color = PlanSecondary, fontSize = 12.sp
        )
        Spacer(Modifier.height(10.dp))
        if (visible.isEmpty()) {
            Surface(color = Color(PremiumPalette.Panel.toInt()),
                shape = RoundedCornerShape(18.dp), border = BorderStroke(1.dp, PlanLine)) {
                Column(Modifier.fillMaxWidth().padding(21.dp)) {
                    Text(if (total == 0) "План пока пуст" else "В этом разделе задач нет",
                        color = PlanWhite, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(5.dp))
                    Text("Добавь следующую задачу или переключи фильтр.",
                        color = PlanSecondary, fontSize = 12.sp)
                }
            }
        }
        visible.forEach { item ->
            Surface(
                color = Color(PremiumPalette.Panel.toInt()),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, PlanLine),
                modifier = Modifier.fillMaxWidth().padding(bottom = 9.dp)
            ) {
                Column(Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = item.completed, enabled = !busy,
                            onCheckedChange = { onToggle(item.id, it) }
                        )
                        Spacer(Modifier.width(3.dp))
                        Text(item.title,
                            color = if (item.completed) PlanSecondary else PlanWhite,
                            fontSize = 14.sp, lineHeight = 20.sp,
                            modifier = Modifier.weight(1f))
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = { editing = item }, enabled = !busy,
                            modifier = Modifier.testTag("task_edit_${item.id}")
                        ) { Text("Изменить", color = PlanBlue, fontSize = 12.sp) }
                        TextButton(
                            onClick = { deleting = item }, enabled = !busy,
                            modifier = Modifier.testTag("task_delete_${item.id}")
                        ) { Text("Удалить", color = Color(0xFFFFCE83), fontSize = 12.sp) }
                    }
                }
            }
        }
    }
    editing?.let { selected ->
        MemoryEditDialog(
            heading = "Изменить задачу", initial = selected.title, maxLength = 500,
            tag = "confirm_edit_task",
            onDismiss = { editing = null },
            onSave = { onEdit(selected.id, it); editing = null }
        )
    }
    deleting?.let { selected ->
        MemoryDeleteDialog(
            itemType = "задачу", preview = selected.title, tag = "confirm_delete_task",
            onDismiss = { deleting = null },
            onDelete = { onDelete(selected.id); deleting = null }
        )
    }
}

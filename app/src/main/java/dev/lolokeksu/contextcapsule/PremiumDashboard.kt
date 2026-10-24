package dev.lolokeksu.contextcapsule

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Close
import dev.lolokeksu.contextcapsule.domain.WorkspaceTools
import dev.lolokeksu.contextcapsule.domain.ProjectInput
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val B = Color(PremiumPalette.Background.toInt())
private val P = Color(PremiumPalette.Panel.toInt())
private val White = Color(PremiumPalette.Title.toInt())
private val MutedText = Color(PremiumPalette.Secondary.toInt())
private val SoftText = Color(PremiumPalette.Muted.toInt())
private val BlueLight = Color(PremiumPalette.Blue.toInt())
private val StrokeBlue = Color(PremiumPalette.Outline.toInt())

@Composable
fun PremiumHomeContent(
    state: CapsuleUiState,
    onOpen: (String?) -> Unit,
    onCreate: (String, String) -> Unit,
    onImport: () -> Unit
) {
    var showCreate by remember { mutableStateOf(false) }
    var title by remember { mutableStateOf("") }
    var repository by remember { mutableStateOf("") }
    var formError by remember { mutableStateOf<String?>(null) }
    var search by remember { mutableStateOf("") }
    val visibleProjects = WorkspaceTools.filterProjects(state.projects, search)
    Column(
        modifier = Modifier.fillMaxSize().background(B).verticalScroll(rememberScrollState())
            .padding(start = 24.dp, end = 24.dp, top = 21.dp, bottom = 32.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier.size(44.dp).clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF172B4A))
                    .border(1.dp, Color(0xFF3C6090), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = BlueLight, modifier = Modifier.size(23.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text("CAPSULE", color = White, fontSize = 16.sp, letterSpacing = 2.sp, fontWeight = FontWeight.Bold)
                Text("Память проектов", color = SoftText, fontSize = 12.sp)
            }
            Spacer(Modifier.weight(1f))
            Box(
                modifier = Modifier.border(1.dp, StrokeBlue, RoundedCornerShape(50))
                    .padding(horizontal = 12.dp, vertical = 7.dp)
            ) { Text("LOCAL", color = Color(PremiumPalette.Teal.toInt()), fontSize = 10.sp, letterSpacing = 1.5.sp, fontWeight = FontWeight.SemiBold) }
        }
        Spacer(Modifier.height(if (state.projects.isEmpty()) 35.dp else 22.dp))
        Text(
            if (state.projects.isEmpty()) "Контекст, который\nне потеряется." else "С возвращением.",
            color = White, fontSize = if (state.projects.isEmpty()) 34.sp else 29.sp,
            lineHeight = if (state.projects.isEmpty()) 39.sp else 34.sp,
            fontWeight = FontWeight.SemiBold, letterSpacing = (-0.9).sp
        )
        Spacer(Modifier.height(11.dp))
        Text(
            if (state.projects.isEmpty())
                "Решения, задачи и факты из GitHub — в одном месте. Продолжай работу в новом чате без повторений."
            else "Все проекты и решения под рукой. Найди нужный контекст или сохрани новый.",
            color = MutedText, fontSize = 14.sp, lineHeight = 21.sp
        )
        Spacer(Modifier.height(if (state.projects.isEmpty()) 26.dp else 17.dp))
        if (state.projects.isEmpty()) {
            CapsuleShowcase()
            Spacer(Modifier.height(20.dp))
        }
        Button(
            onClick = { showCreate = true },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BlueLight, contentColor = B)
        ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(21.dp))
            Spacer(Modifier.width(8.dp))
            Text("Создать проект", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.weight(1f))
            Icon(Icons.Default.ArrowForward, contentDescription = null)
        }
        Spacer(Modifier.height(10.dp))
        OutlinedButton(
            onClick = onImport,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, StrokeBlue),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MutedText)
        ) {
            Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(19.dp))
            Spacer(Modifier.width(9.dp))
            Text("Импортировать резервную копию", fontSize = 13.sp)
        }
        Spacer(Modifier.height(32.dp))
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text("Проекты", color = White, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.width(10.dp))
            Box(
                Modifier.clip(RoundedCornerShape(40.dp)).background(P).padding(horizontal = 10.dp, vertical = 4.dp)
            ) { Text(state.projects.size.toString(), color = MutedText, fontSize = 12.sp) }
        }
        Spacer(Modifier.height(14.dp))
        if (state.projects.isNotEmpty()) {
            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(15.dp),
                label = { Text("Поиск по проектам") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = SoftText) },
                trailingIcon = {
                    if (search.isNotBlank()) {
                        IconButton(onClick = { search = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Очистить поиск", tint = MutedText)
                        }
                    }
                }
            )
            Spacer(Modifier.height(14.dp))
        }
        if (state.projects.isEmpty()) {
            FirstProjectPanel()
        } else if (visibleProjects.isEmpty()) {
            Column(
                Modifier.fillMaxWidth().background(P, RoundedCornerShape(20.dp)).padding(20.dp)
            ) {
                Text("Ничего не найдено", color = White, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(5.dp))
                Text("Попробуй другое название или часть адреса GitHub.", color = MutedText, fontSize = 13.sp)
                TextButton(onClick = { search = "" }) { Text("Сбросить поиск", color = BlueLight) }
            }
        } else {
            visibleProjects.forEachIndexed { index, project ->
                if (index != 0) Spacer(Modifier.height(10.dp))
                Card(
                    onClick = { onOpen(project.id) },
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, StrokeBlue),
                    colors = CardDefaults.cardColors(containerColor = P)
                ) {
                    Column(Modifier.fillMaxWidth().padding(19.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier.size(44.dp).clip(RoundedCornerShape(13.dp))
                                    .background(Color(0xFF1B3457)),
                                contentAlignment = Alignment.Center
                            ) { Icon(Icons.Default.FolderOpen, null, tint = BlueLight) }
                            Spacer(Modifier.width(13.dp))
                            Column(Modifier.weight(1f)) {
                                Text(project.title, color = White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold,
                                    maxLines = 2, overflow = TextOverflow.Ellipsis)
                                Text(
                                    project.repository ?: "Локальный проект",
                                    color = MutedText, fontSize = 12.sp,
                                    maxLines = 1, overflow = TextOverflow.Ellipsis
                                )
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = "Открыть", tint = BlueLight)
                        }
                        Spacer(Modifier.height(14.dp))
                        Text(
                            if (project.repository == null) "Память хранится на устройстве" else "GitHub • синхронизация по запросу",
                            color = SoftText, fontSize = 11.sp
                        )
                    }
                }
            }
        }
        if (state.busy) { Spacer(Modifier.height(18.dp)); LinearProgressIndicator(Modifier.fillMaxWidth()) }
        state.notice?.let {
            Spacer(Modifier.height(14.dp))
            Text(it, color = BlueLight, fontSize = 13.sp)
        }
        Spacer(Modifier.height(26.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Key, contentDescription = null, tint = SoftText, modifier = Modifier.size(15.dp))
            Spacer(Modifier.width(9.dp))
            Text("Без платного ИИ API. Данные под твоим контролем.", color = SoftText,
                fontSize = 11.sp, lineHeight = 15.sp)
        }
    }
    if (showCreate) {
        AlertDialog(
            onDismissRequest = { showCreate = false },
            containerColor = P,
            shape = RoundedCornerShape(24.dp),
            title = { Text("Новый проект", color = White, fontWeight = FontWeight.SemiBold) },
            text = {
                Column {
                    Text("Создай отдельную память для разработки или личного проекта.", color = MutedText, fontSize = 13.sp)
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = title, onValueChange = { title = it; formError = null }, singleLine = true,
                        label = { Text("Название проекта") }, modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = repository, onValueChange = { repository = it; formError = null }, singleLine = true,
                        label = { Text("GitHub owner/repo или ссылка (необязательно)") },
                        isError = formError != null,
                        modifier = Modifier.fillMaxWidth()
                    )
                    formError?.let { error ->
                        Spacer(Modifier.height(8.dp))
                        Text(error, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                    }
                    state.notice?.let { message ->
                        Spacer(Modifier.height(8.dp))
                        Text(message, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        runCatching { ProjectInput.parse(title, repository) }
                            .onSuccess { draft ->
                                formError = null
                                onCreate(draft.title, draft.repository.orEmpty())
                                // Keep the dialog visible until the ViewModel actually opens the new project.
                                // On failure, its notice remains visible here rather than disappearing.
                            }
                            .onFailure { formError = it.message ?: "Не удалось проверить данные" }
                    }, enabled = title.isNotBlank() && !state.busy
                ) { Text("Создать", color = BlueLight) }
            },
            dismissButton = { TextButton(onClick = { showCreate = false }) { Text("Отмена", color = MutedText) } }
        )
    }
}

@Composable
private fun CapsuleShowcase() {
    Box(
        modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(23.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFF172B4C), Color(0xFF111B30), Color(0xFF0B1422))
                )
            )
            .border(1.dp, Color(0xFF2A4265), RoundedCornerShape(23.dp))
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val center = Offset(size.width * .77f, size.height * .48f)
            val r = minOf(size.width, size.height) * .29f
            drawCircle(Color(0xFF294D8E).copy(alpha = .42f), radius = r * 1.60f, center = center)
            listOf(1.72f, 1.38f, 1.06f).forEachIndexed { i, multiple ->
                val radius = r * multiple
                drawArc(
                    color = BlueLight.copy(alpha = if (i == 0) .20f else .47f),
                    startAngle = 205f + i * 40f, sweepAngle = 254f,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = (if (i == 1) 1.5.dp else 1.dp).toPx())
                )
            }
            drawCircle(BlueLight.copy(alpha = .13f), radius = r, center = center)
            drawCircle(BlueLight.copy(alpha = .90f), radius = r * .44f, center = center, style = Stroke(width = 2.dp.toPx()))
            drawLine(Color(0xFF9FC6FF), Offset(center.x - r * .2f, center.y), Offset(center.x + r * .2f, center.y), strokeWidth = 3.dp.toPx())
            drawCircle(Color.White, radius = 3.dp.toPx(), center = center)
        }
        Column(Modifier.align(Alignment.TopStart).padding(21.dp)) {
            Text("ТВОЯ КАПСУЛА", color = BlueLight, fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold, letterSpacing = 1.8.sp)
            Spacer(Modifier.height(12.dp))
            Text("Одна память\nдля каждого\nпроекта.", color = White, fontSize = 21.sp,
                lineHeight = 26.sp, fontWeight = FontWeight.SemiBold)
        }
        Row(Modifier.align(Alignment.BottomStart).padding(21.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(6.dp).clip(RoundedCornerShape(10.dp)).background(Color(PremiumPalette.Teal.toInt())))
            Spacer(Modifier.width(8.dp))
            Text("Сохраняй. Проверяй. Продолжай.", color = MutedText, fontSize = 11.sp)
        }
    }
}

@Composable
private fun FirstProjectPanel() {
    Column(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(21.dp)).background(P)
            .border(1.dp, StrokeBlue, RoundedCornerShape(21.dp)).padding(19.dp)
    ) {
        Text("Твой первый проект начинается здесь", color = White,
            fontWeight = FontWeight.Medium, fontSize = 16.sp)
        Spacer(Modifier.height(8.dp))
        Text("Три шага, чтобы больше не пересказывать историю работы.", color = MutedText,
            fontSize = 12.sp, lineHeight = 17.sp)
        Spacer(Modifier.height(18.dp))
        listOf(
            "01" to "Добавь проект и ссылку на GitHub",
            "02" to "Зафиксируй решения и задачи",
            "03" to "Скопируй контекст в новый чат"
        ).forEachIndexed { index, (number, label) ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.width(36.dp).height(29.dp).clip(RoundedCornerShape(9.dp))
                    .background(Color(0xFF233A5B)), contentAlignment = Alignment.Center) {
                    Text(number, color = BlueLight, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.width(12.dp))
                Text(label, color = White, fontSize = 12.sp)
            }
            if (index != 2) Spacer(Modifier.height(11.dp))
        }
    }
}

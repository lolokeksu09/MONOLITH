package dev.lolokeksu.contextcapsule

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Close
import dev.lolokeksu.contextcapsule.domain.WorkspaceTools
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.lolokeksu.contextcapsule.domain.EvidenceKind
import dev.lolokeksu.contextcapsule.domain.Decision
import dev.lolokeksu.contextcapsule.domain.ProjectTask
import dev.lolokeksu.contextcapsule.domain.SearchCategory
import dev.lolokeksu.contextcapsule.domain.SearchHit

private val InkBlack = Color(PremiumPalette.Background.toInt())
private val WhiteText = Color(PremiumPalette.Title.toInt())
private val BodyText = Color(PremiumPalette.Secondary.toInt())
private val SubText = Color(PremiumPalette.Muted.toInt())
private val Electric = Color(PremiumPalette.Blue.toInt())
private val Plate = Color(PremiumPalette.Panel.toInt())
private val Line = Color(PremiumPalette.Outline.toInt())
private val Positive = Color(PremiumPalette.Teal.toInt())
private val Caution = Color(0xFFFFCE83)

@Composable
fun PremiumProjectContent(
    state: CapsuleUiState,
    onBack: () -> Unit,
    onSync: () -> Unit,
    onNote: (String) -> Unit,
    onConfirm: (String) -> Unit,
    onTask: (String) -> Unit,
    onToggle: (String, Boolean) -> Unit,
    onEditDecision: (String, String) -> Unit,
    onPinDecision: (String, Boolean) -> Unit,
    onDeleteDecision: (String) -> Unit,
    onEditTask: (String, String) -> Unit,
    onDeleteTask: (String) -> Unit,
    onUpdateProject: (String, String) -> Unit,
    onArchiveProject: (Boolean) -> Unit,
    onDeleteProject: (String) -> Unit,
    onCopy: (Boolean) -> Unit,
    onShare: (Boolean) -> Unit,
    onExport: (Boolean, Boolean) -> Unit,
    focus: SearchHit? = null
) {
    val project = state.active ?: return
    var note by remember(project.id) { mutableStateOf("") }
    var task by remember(project.id) { mutableStateOf("") }
    var preview by remember(project.id) { mutableStateOf(false) }
    var compactBrief by remember(project.id) { mutableStateOf(true) }
    var decisionQuery by remember(project.id, focus?.recordId) {
        mutableStateOf(
            if (focus?.projectId == project.id && focus.category == SearchCategory.NOTES)
                focus.text else ""
        )
    }
    var pendingOnly by remember(project.id) { mutableStateOf(false) }
    var selectedSection by remember(project.id, focus?.recordId) {
        mutableIntStateOf(
            if (focus?.projectId != project.id) 0
            else if (focus.category == SearchCategory.NOTES) 1
            else 2
        )
    }
    var editingNote by remember(project.id) { mutableStateOf<Decision?>(null) }
    var deletingNote by remember(project.id) { mutableStateOf<Decision?>(null) }
    var showSettings by remember(project.id) { mutableStateOf(false) }
    var showProjectDelete by remember(project.id) { mutableStateOf(false) }
    val shownDecisions = WorkspaceTools.filterDecisions(state.decisions, decisionQuery, pendingOnly)

    Column(Modifier.fillMaxSize().background(InkBlack)) {
        Column(
            Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState())
                .padding(horizontal = 23.dp, vertical = 18.dp)
        ) {
        Row(verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            TextButton(onClick = onBack, contentPadding = PaddingValues(0.dp)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = BodyText)
                Spacer(Modifier.width(7.dp))
                Text("Все проекты", color = BodyText, fontSize = 13.sp)
            }
            OutlinedButton(
                onClick = { showSettings = true }, enabled = !state.busy,
                modifier = Modifier.height(45.dp).testTag("project_settings"),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, Line),
                contentPadding = PaddingValues(horizontal = 13.dp)
            ) { Text("Настроить", color = Electric, fontSize = 12.sp) }
        }
        Spacer(Modifier.height(18.dp))
        Text("РАБОЧЕЕ ПРОСТРАНСТВО", color = Electric, fontSize = 11.sp,
            fontWeight = FontWeight.Bold, letterSpacing = 1.6.sp)
        Spacer(Modifier.height(6.dp))
        Text(
            project.title, color = WhiteText, fontSize = 31.sp, lineHeight = 35.sp,
            fontWeight = FontWeight.SemiBold
        )
        if (project.archived) {
            Spacer(Modifier.height(8.dp))
            Text("ПРОЕКТ В АРХИВЕ", color = Caution, fontSize = 11.sp,
                letterSpacing = 1.1.sp, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Folder, contentDescription = null, tint = SubText, modifier = Modifier.size(15.dp))
            Spacer(Modifier.width(7.dp))
            Text(project.repository ?: "Локальный проект", color = BodyText, fontSize = 12.sp, maxLines = 2)
        }
        Spacer(Modifier.height(18.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            listOf("Обзор", "Память", "План").forEachIndexed { index, label ->
                val active = selectedSection == index
                FilledTonalButton(
                    onClick = { selectedSection = index },
                    modifier = Modifier.weight(1f).height(48.dp).testTag("section_$index"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = if (active) Color(0xFF243F63) else Plate,
                        contentColor = if (active) WhiteText else BodyText
                    )
                ) {
                    Text(label, fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal,
                        fontSize = 12.sp)
                }
            }
        }
        Spacer(Modifier.height(22.dp))
        if (selectedSection == 0) {
            RepositoryOverviewPanel(state.snapshot, project.repository != null, state.busy, onSync, onTask, state.tasks)
        } else if (selectedSection == 1) {
        Column(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(23.dp))
                .background(Brush.linearGradient(listOf(Color(0xFF1E3961), Color(0xFF111C30))))
                .border(1.dp, Color(0xFF3A5883), RoundedCornerShape(23.dp))
                .padding(21.dp)
        ) {
            Text("ПЕРЕДАЧА КОНТЕКСТА", fontSize = 10.sp, fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.6.sp, color = Electric)
            Spacer(Modifier.height(8.dp))
            Text("Продолжить с того же места", fontSize = 20.sp, color = WhiteText,
                fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(6.dp))
            Text("Готовый пакет решений, задач и подтверждённых данных GitHub для нового чата.",
                color = BodyText, fontSize = 12.sp, lineHeight = 18.sp)
            Spacer(Modifier.height(20.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                StatBlock(state.decisions.count { it.evidence == EvidenceKind.OWNER_CONFIRMED }.toString(), "Решений", Modifier.weight(1f))
                StatBlock(state.tasks.count { !it.completed }.toString(), "Задач", Modifier.weight(1f))
                StatBlock(if (state.snapshot == null) "—" else "✓", "GitHub", Modifier.weight(1f))
            }
            Spacer(Modifier.height(19.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = { onShare(compactBrief) }, contentPadding = PaddingValues(0.dp)) {
                    Icon(Icons.Default.IosShare, contentDescription = null, tint = BodyText, modifier = Modifier.size(17.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Отправить", color = BodyText, fontSize = 13.sp)
                }
                TextButton(onClick = { preview = !preview }, contentPadding = PaddingValues(0.dp)) {
                    Text(if (preview) "Скрыть текст" else "Просмотр", color = Electric, fontSize = 13.sp)
                }
            }
        }
        if (preview) {
            Spacer(Modifier.height(12.dp))
            ProjectPlate {
                SelectionContainer {
                    Text(
                        dev.lolokeksu.contextcapsule.domain.CapsuleComposer().toMarkdown(
                            dev.lolokeksu.contextcapsule.domain.CapsuleInput(
                                project, state.decisions, state.tasks,
                                state.snapshot?.copy(summary = dev.lolokeksu.contextcapsule.github.RepositoryOverviewCodec.readable(state.snapshot.summary))
                            ), compact = compactBrief
                        ),
                        color = BodyText, fontSize = 12.sp, lineHeight = 19.sp
                    )
                }
            }
        }

        Spacer(Modifier.height(29.dp))
        SectionTitle("Важные решения", "${state.decisions.size}")
        Spacer(Modifier.height(12.dp))
        ProjectPlate {
            OutlinedTextField(
                value = note, onValueChange = { note = it },
                label = { Text("Что необходимо помнить?") },
                minLines = 2, maxLines = 5, modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            )
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = { onNote(note); note = "" },
                enabled = note.isNotBlank(), shape = RoundedCornerShape(13.dp),
                modifier = Modifier.align(Alignment.End)
            ) { Text("Сохранить заметку") }
        }
        if (state.decisions.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = decisionQuery,
                onValueChange = { decisionQuery = it },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                label = { Text("Найти решение или заметку") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = SubText)
                },
                trailingIcon = {
                    if (decisionQuery.isNotBlank()) IconButton(onClick = { decisionQuery = "" }) {
                        Icon(Icons.Default.Close, contentDescription = "Очистить поиск", tint = SubText)
                    }
                }
            )
            Spacer(Modifier.height(7.dp))
            FilterChip(
                selected = pendingOnly,
                onClick = { pendingOnly = !pendingOnly },
                label = { Text("На проверку · ${state.decisions.count { it.evidence != EvidenceKind.OWNER_CONFIRMED }}") }
            )
            if (shownDecisions.isEmpty()) {
                Spacer(Modifier.height(9.dp))
                Text("Под эти условия заметок нет.", color = BodyText, fontSize = 12.sp)
            }
        } else {
            Spacer(Modifier.height(9.dp))
            Text("Сохрани первое решение или поделись текстом из другого приложения.",
                color = BodyText, fontSize = 12.sp, lineHeight = 18.sp)
        }
        shownDecisions.forEach { d ->
            Spacer(Modifier.height(9.dp))
            ProjectPlate {
                Text(d.text, color = WhiteText, fontSize = 14.sp, lineHeight = 20.sp)
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val confirmed = d.evidence == EvidenceKind.OWNER_CONFIRMED
                    Icon(Icons.Default.CheckCircle, contentDescription = null,
                        tint = if (confirmed) Positive else Caution, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(if (confirmed) "Подтверждено" else "Нужно подтвердить",
                        color = if (confirmed) Positive else Caution, fontSize = 11.sp)
                }
                if (d.evidence != EvidenceKind.OWNER_CONFIRMED) {
                    TextButton(onClick = { onConfirm(d.id) }, enabled = !state.busy) {
                        Text("Подтвердить решение", color = Electric, fontSize = 13.sp)
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = { onPinDecision(d.id, !d.pinned) },
                        enabled = !state.busy,
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("note_pin_${d.id}")
                    ) {
                        Text(if (d.pinned) "Открепить" else "Закрепить",
                            color = if (d.pinned) Positive else Electric, fontSize = 12.sp)
                    }
                    TextButton(
                        onClick = { editingNote = d },
                        enabled = !state.busy,
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("note_edit_${d.id}")
                    ) { Text("Изменить", color = BodyText, fontSize = 12.sp) }
                    TextButton(
                        onClick = { deletingNote = d },
                        enabled = !state.busy,
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("note_delete_${d.id}")
                    ) { Text("Удалить", color = Caution, fontSize = 12.sp) }
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        Text("Резервные копии содержат личные заметки. Храни их только в надёжном месте.",
            color = SubText, fontSize = 11.sp, lineHeight = 16.sp)
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            TextButton(onClick = { onExport(false, compactBrief) }) {
                Icon(Icons.Default.HistoryEdu, contentDescription = null, tint = Electric, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Markdown", color = Electric)
            }
            TextButton(onClick = { onExport(true, compactBrief) }) {
                Icon(Icons.Default.FileDownload, contentDescription = null, tint = BodyText, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Резервная копия", color = BodyText)
            }
        }
        state.notice?.let {
            Spacer(Modifier.height(10.dp))
            Text(it, color = Caution, fontSize = 12.sp)
        }
        if (state.busy) { Spacer(Modifier.height(10.dp)); LinearProgressIndicator(Modifier.fillMaxWidth()) }
        Spacer(Modifier.height(32.dp))
        } else {
            TaskBoard(
                tasks = state.tasks,
                busy = state.busy,
                onAdd = onTask,
                onToggle = onToggle,
                onEdit = onEditTask,
                onDelete = onDeleteTask,
                focusTaskId = focus?.takeIf {
                    it.projectId == project.id && it.category == SearchCategory.TASKS
                }?.recordId
            )
        }
        }
        HorizontalDivider(color = Line.copy(alpha = 0.8f))
        Column(
            Modifier.fillMaxWidth().background(InkBlack)
                .padding(horizontal = 23.dp, vertical = 9.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(9.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Капсула:", color = BodyText, fontSize = 12.sp)
                FilterChip(
                    selected = compactBrief,
                    onClick = { compactBrief = true },
                    label = { Text("Кратко") },
                    modifier = Modifier.testTag("brief_compact"),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF274364),
                        selectedLabelColor = WhiteText
                    )
                )
                FilterChip(
                    selected = !compactBrief,
                    onClick = { compactBrief = false },
                    label = { Text("Полностью") },
                    modifier = Modifier.testTag("brief_full"),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF274364),
                        selectedLabelColor = WhiteText
                    )
                )
            }
            Button(
                onClick = { onCopy(compactBrief) },
                modifier = Modifier.fillMaxWidth().height(50.dp).testTag("handoff_copy"),
                shape = RoundedCornerShape(15.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Electric, contentColor = InkBlack)
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(10.dp))
                Text("Скопировать контекст", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            }
        }
    }
    if (showSettings) {
        ProjectSettingsDialog(
            project = project, busy = state.busy,
            onSave = onUpdateProject,
            onArchive = onArchiveProject,
            onBackup = {
                showSettings = false
                onExport(true, false)
            },
            onRequestDelete = { showProjectDelete = true },
            onDismiss = { showSettings = false }
        )
    }
    if (showProjectDelete) {
        ProjectDeleteDialog(
            project = project, onDelete = {
                onDeleteProject(it)
                showProjectDelete = false
            }, onDismiss = { showProjectDelete = false }
        )
    }
    editingNote?.let { selected ->
        MemoryEditDialog(
            heading = "Изменить заметку", initial = selected.text, maxLength = 20_000,
            tag = "confirm_edit_note",
            onDismiss = { editingNote = null },
            onSave = { updated ->
                onEditDecision(selected.id, updated)
                editingNote = null
            }
        )
    }
    deletingNote?.let { selected ->
        MemoryDeleteDialog(
            itemType = "заметку", preview = selected.text, tag = "confirm_delete_note",
            onDismiss = { deletingNote = null },
            onDelete = {
                onDeleteDecision(selected.id)
                deletingNote = null
            }
        )
    }
}

@Composable
private fun StatBlock(value: String, label: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.clip(RoundedCornerShape(13.dp)).background(Color(0xFF142944))
            .padding(horizontal = 12.dp, vertical = 11.dp)
    ) {
        Text(value, color = WhiteText, fontWeight = FontWeight.Bold, fontSize = 22.sp)
        Text(label, color = BodyText, fontSize = 11.sp)
    }
}

@Composable
private fun SectionTitle(title: String, count: String) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(title, color = WhiteText, fontSize = 19.sp, fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f))
        Text(count, color = SubText, fontSize = 12.sp)
    }
}

@Composable
private fun ProjectPlate(content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(19.dp)).background(Plate)
            .border(1.dp, Line, RoundedCornerShape(19.dp)).padding(17.dp),
        content = content
    )
}

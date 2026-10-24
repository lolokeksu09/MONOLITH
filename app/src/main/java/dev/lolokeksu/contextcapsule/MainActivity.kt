package dev.lolokeksu.contextcapsule

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.lolokeksu.contextcapsule.domain.EvidenceKind

private val Ink = Color(0xFF050609)
private val Surface = Color(0xFF10141C)
private val Accent = Color(0xFF6BA8FF)
private val Muted = Color(0xFF96A2B5)

class MainActivity : ComponentActivity() {
    private val vm: CapsuleViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = android.graphics.Color.BLACK
        window.navigationBarColor = android.graphics.Color.BLACK
        setContent { CapsuleApp(vm) }
    }
}

@Composable
private fun CapsuleApp(vm: CapsuleViewModel) {
    val context = LocalContext.current
    val state by vm.state.collectAsStateWithLifecycle()
    var exportText by remember { mutableStateOf("") }
    var showBackupWarning by remember { mutableStateOf(false) }
    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/plain")) { uri ->
        if (uri != null) runCatching {
            context.contentResolver.openOutputStream(uri)?.use { it.write(exportText.toByteArray(Charsets.UTF_8)) }
                ?: error("Unable to open file")
        }.onFailure { Toast.makeText(context, it.message ?: "Export failed", Toast.LENGTH_LONG).show() }
    }
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) runCatching {
            context.contentResolver.openInputStream(uri)?.bufferedReader(Charsets.UTF_8)?.use { reader ->
                reader.readText().take(1_000_001).takeIf { it.length <= 1_000_000 } ?: error("File is too large")
            } ?: error("Unable to open document")
        }.onSuccess(vm::importBackup)
            .onFailure { Toast.makeText(context, it.message ?: "Import failed", Toast.LENGTH_LONG).show() }
    }
    val colors = darkColorScheme(primary = Accent, onPrimary = Ink, background = Ink,
        surface = Surface, onSurface = Color(0xFFF1F5FF), onBackground = Color(0xFFF1F5FF),
        surfaceVariant = Color(0xFF1A2230), onSurfaceVariant = Muted)
    MaterialTheme(colorScheme = colors) {
        Column(Modifier.fillMaxSize().background(Ink).statusBarsPadding().navigationBarsPadding()) {
            if (state.active == null) {
                HomeContent(state, onOpen = vm::open, onCreate = vm::create,
                    onImport = { importLauncher.launch(arrayOf("application/json", "text/plain", "*/*")) })
            } else {
                ProjectContent(state, onBack = { vm.open(null) }, onSync = vm::sync,
                    onNote = vm::addDecision, onConfirm = vm::confirmDecision,
                    onTask = vm::addTask, onToggle = vm::toggleTask,
                    onCopy = {
                        val clip = android.content.ClipData.newPlainText("Context Capsule", vm.capsule())
                        (context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager).setPrimaryClip(clip)
                        Toast.makeText(context, "Capsule copied", Toast.LENGTH_SHORT).show()
                    },
                    onShare = {
                        val send = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, vm.capsule())
                        }
                        context.startActivity(Intent.createChooser(send, "Continue in ChatGPT"))
                    },
                    onExport = { backup ->
                        if (backup) showBackupWarning = true
                        else {
                            exportText = vm.capsule()
                            exportLauncher.launch("context-capsule.md")
                        }
                    })
            }
        }
        if (showBackupWarning) AlertDialog(
            onDismissRequest = { showBackupWarning = false },
            title = { Text("Unencrypted backup") },
            text = { Text("This JSON file contains your private project notes in plain text. Save it only to a trusted location.") },
            confirmButton = { TextButton(onClick = {
                showBackupWarning = false
                exportText = vm.backup()
                exportLauncher.launch("context-capsule-backup.json")
            }) { Text("Export backup") } },
            dismissButton = { TextButton(onClick = { showBackupWarning = false }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun HomeContent(state: CapsuleUiState, onOpen: (String?) -> Unit,
                        onCreate: (String, String) -> Unit, onImport: () -> Unit) {
    var showCreate by remember { mutableStateOf(false) }
    var title by remember { mutableStateOf("") }
    var repository by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 22.dp, vertical = 26.dp)) {
        Text("PRIVATE WORKSPACE / 01", color = Accent, fontSize = 11.sp, letterSpacing = 2.sp)
        Spacer(Modifier.height(12.dp))
        Text("Context\nCapsule", fontSize = 43.sp, lineHeight = 46.sp, fontWeight = FontWeight.Light)
        Spacer(Modifier.height(14.dp))
        Text("A durable memory for your projects. No paid AI API. Your data stays here.", color = Muted, fontSize = 14.sp)
        Spacer(Modifier.height(28.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(onClick = { showCreate = true }, shape = RoundedCornerShape(14.dp)) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("New project")
            }
            OutlinedButton(onClick = onImport, shape = RoundedCornerShape(14.dp)) {
                Icon(Icons.Default.FileOpen, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("Import backup")
            }
        }
        Spacer(Modifier.height(34.dp))
        Text("YOUR PROJECTS", color = Muted, letterSpacing = 2.sp, fontSize = 11.sp)
        Spacer(Modifier.height(12.dp))
        if (state.projects.isEmpty()) {
            SurfaceCard {
                Text("Nothing to remember yet", fontSize = 19.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(6.dp))
                Text("Create a project, add decisions, then build your first handoff capsule.", color = Muted)
            }
        }
        state.projects.forEach { p ->
            Spacer(Modifier.height(10.dp))
            Card(onClick = { onOpen(p.id) }, shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Surface)) {
                Column(Modifier.fillMaxWidth().padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Folder, contentDescription = null, tint = Accent)
                        Spacer(Modifier.width(12.dp))
                        Text(p.title, fontSize = 21.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f),
                            maxLines = 2, overflow = TextOverflow.Ellipsis)
                        Icon(Icons.Default.ChevronRight, contentDescription = "Open", tint = Muted)
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(p.repository ?: "Local-only project", color = Muted, fontSize = 13.sp)
                    Spacer(Modifier.height(8.dp))
                    Text("Open project memory", color = Accent, fontSize = 12.sp)
                }
            }
        }
        Spacer(Modifier.height(24.dp))
        state.notice?.let { Text(it, color = Accent) }
        if (state.busy) LinearProgressIndicator(Modifier.fillMaxWidth())
    }
    if (showCreate) AlertDialog(
        onDismissRequest = { showCreate = false },
        title = { Text("New project") },
        text = {
            Column {
                OutlinedTextField(title, { title = it }, label = { Text("Project name") }, singleLine = true)
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(repository, { repository = it }, label = { Text("GitHub owner/repo (optional)") }, singleLine = true)
            }
        },
        confirmButton = { TextButton(onClick = { onCreate(title, repository); showCreate = false; title = ""; repository = "" }) { Text("Create") } },
        dismissButton = { TextButton(onClick = { showCreate = false }) { Text("Cancel") } }
    )
}

@Composable
private fun ProjectContent(state: CapsuleUiState, onBack: () -> Unit, onSync: () -> Unit,
    onNote: (String) -> Unit, onConfirm: (String) -> Unit, onTask: (String) -> Unit,
    onToggle: (String, Boolean) -> Unit, onCopy: () -> Unit, onShare: () -> Unit,
    onExport: (Boolean) -> Unit) {
    val p = state.active ?: return
    var note by remember(p.id) { mutableStateOf("") }
    var task by remember(p.id) { mutableStateOf("") }
    var preview by remember(p.id) { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 18.dp)) {
        TextButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null); Text("Projects") }
        Spacer(Modifier.height(8.dp))
        Text(p.title, fontSize = 32.sp, fontWeight = FontWeight.Medium)
        Text(p.repository ?: "Local project", color = Muted, fontSize = 13.sp)
        Spacer(Modifier.height(18.dp))
        if (p.repository != null) {
            Button(onClick = onSync, enabled = !state.busy, shape = RoundedCornerShape(12.dp)) {
                Icon(Icons.Default.Sync, contentDescription = null)
                Spacer(Modifier.width(7.dp))
                Text(if (state.busy) "Syncing…" else "Sync GitHub")
            }
        }
        state.snapshot?.let { s ->
            Spacer(Modifier.height(12.dp))
            SurfaceCard {
                Text("GITHUB SNAPSHOT", color = Accent, fontSize = 11.sp, letterSpacing = 1.5.sp)
                Spacer(Modifier.height(8.dp))
                Text(s.sha.take(12), fontSize = 21.sp, fontWeight = FontWeight.Bold)
                Text("Branch ${s.branch} • Checked at ${java.text.DateFormat.getDateTimeInstance().format(s.checkedAt)}", color = Muted, fontSize = 12.sp)
                if (s.warning != null) Text("INCOMPLETE: ${s.warning}", color = Color(0xFFFFBC76), fontSize = 12.sp)
                if (s.summary.isNotBlank()) Text(s.summary, color = Muted, fontSize = 12.sp)
            }
        }
        Spacer(Modifier.height(24.dp))
        Text("DECISIONS & NOTES", fontSize = 15.sp, letterSpacing = 1.2.sp, color = Accent)
        Spacer(Modifier.height(9.dp))
        OutlinedTextField(note, { note = it }, label = { Text("Paste an important decision") },
            minLines = 2, maxLines = 5, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = { onNote(note); note = "" }, enabled = note.isNotBlank()) { Text("Save as unverified") }
        state.decisions.forEach { d ->
            Spacer(Modifier.height(9.dp))
            SurfaceCard {
                Text(d.text, fontSize = 14.sp)
                Spacer(Modifier.height(8.dp))
                Text(d.evidence.name, color = if (d.evidence == EvidenceKind.OWNER_CONFIRMED) Color(0xFF79DCBD) else Color(0xFFFFBC76), fontSize = 11.sp)
                if (d.evidence != EvidenceKind.OWNER_CONFIRMED) TextButton(onClick = { onConfirm(d.id) }) { Text("Confirm my decision") }
            }
        }
        Spacer(Modifier.height(26.dp))
        Text("NEXT ACTIONS", fontSize = 15.sp, letterSpacing = 1.2.sp, color = Accent)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(task, { task = it }, label = { Text("Task") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = { onTask(task); task = "" }, enabled = task.isNotBlank()) { Text("Add task") }
        state.tasks.forEach { t ->
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = t.completed, onCheckedChange = { onToggle(t.id, it) })
                Text(t.title, color = if (t.completed) Muted else Color.White)
            }
        }
        Spacer(Modifier.height(26.dp))
        Text("HANDOFF CAPSULE", fontSize = 15.sp, letterSpacing = 1.2.sp, color = Accent)
        Spacer(Modifier.height(8.dp))
        Text("Includes decisions, GitHub evidence, open tasks, and uncertainty labels.", color = Muted, fontSize = 13.sp)
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Button(onClick = onCopy) { Text("Copy") }
            OutlinedButton(onClick = onShare) { Text("Share") }
            OutlinedButton(onClick = { preview = !preview }) { Text(if (preview) "Hide" else "Preview") }
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = { onExport(false) }) { Text("Save Markdown") }
            TextButton(onClick = { onExport(true) }) { Text("Backup JSON") }
        }
        if (preview) {
            Spacer(Modifier.height(12.dp))
            SurfaceCard {
                SelectionContainer {
                    Text(dev.lolokeksu.contextcapsule.domain.CapsuleComposer().toMarkdown(
                        dev.lolokeksu.contextcapsule.domain.CapsuleInput(p, state.decisions, state.tasks, state.snapshot)
                    ), fontSize = 12.sp, lineHeight = 18.sp)
                }
            }
        }
        state.notice?.let { Spacer(Modifier.height(16.dp)); Text(it, color = Color(0xFFFFBC76), fontSize = 13.sp) }
        if (state.busy) LinearProgressIndicator(Modifier.fillMaxWidth())
        Spacer(Modifier.height(40.dp))
    }
}

@Composable
private fun SurfaceCard(content: @Composable ColumnScope.() -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = Surface), shape = RoundedCornerShape(20.dp)) {
        Column(Modifier.fillMaxWidth().padding(18.dp), content = content)
    }
}

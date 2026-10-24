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
import dev.lolokeksu.contextcapsule.domain.WorkspaceTools
import kotlinx.coroutines.flow.MutableStateFlow

private val Ink = Color(PremiumPalette.Background.toInt())
private val Surface = Color(PremiumPalette.Panel.toInt())
private val Accent = Color(0xFF6BA8FF)
private val Muted = Color(0xFF96A2B5)

class MainActivity : ComponentActivity() {
    private val vm: CapsuleViewModel by viewModels()
    private val incomingText = MutableStateFlow<String?>(null)

    private fun readShare(intent: Intent?) {
        if (intent?.action == Intent.ACTION_SEND && intent.type == "text/plain") {
            incomingText.value = WorkspaceTools.normalizeSharedText(
                intent.getCharSequenceExtra(Intent.EXTRA_TEXT)
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        readShare(intent)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = android.graphics.Color.BLACK
        window.navigationBarColor = android.graphics.Color.BLACK
        readShare(intent)
        setContent {
            val shared by incomingText.collectAsStateWithLifecycle()
            CapsuleApp(vm, shared, onShareDismiss = { incomingText.value = null })
        }
    }
}

@Composable
private fun CapsuleApp(vm: CapsuleViewModel, incoming: String?, onShareDismiss: () -> Unit) {
    val context = LocalContext.current
    val state by vm.state.collectAsStateWithLifecycle()
    var exportText by remember { mutableStateOf("") }
    var showBackupWarning by remember { mutableStateOf(false) }
    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/plain")) { uri ->
        if (uri != null) runCatching {
            context.contentResolver.openOutputStream(uri)?.use { it.write(exportText.toByteArray(Charsets.UTF_8)) }
                ?: error("Unable to open file")
        }.onFailure { Toast.makeText(context, it.message ?: "Не удалось сохранить файл", Toast.LENGTH_LONG).show() }
    }
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) runCatching {
            context.contentResolver.openInputStream(uri)?.bufferedReader(Charsets.UTF_8)?.use { reader ->
                reader.readText().take(1_000_001).takeIf { it.length <= 1_000_000 } ?: error("File is too large")
            } ?: error("Unable to open document")
        }.onSuccess(vm::importBackup)
            .onFailure { Toast.makeText(context, it.message ?: "Не удалось импортировать файл", Toast.LENGTH_LONG).show() }
    }
    val colors = darkColorScheme(primary = Accent, onPrimary = Ink, background = Ink,
        surface = Surface, onSurface = Color(0xFFF1F5FF), onBackground = Color(0xFFF1F5FF),
        surfaceVariant = Color(0xFF1A2230), onSurfaceVariant = Muted)
    MaterialTheme(colorScheme = colors) {
        CompositionLocalProvider(LocalContentColor provides Color(PremiumPalette.Title.toInt())) {
        Column(Modifier.fillMaxSize().background(Ink).statusBarsPadding().navigationBarsPadding()) {
            if (state.active == null) {
                PremiumHomeContent(state, onOpen = vm::open, onCreate = vm::create,
                    onImport = { importLauncher.launch(arrayOf("application/json", "text/plain", "*/*")) })
            } else {
                PremiumProjectContent(state, onBack = { vm.open(null) }, onSync = vm::sync,
                    onNote = vm::addDecision, onConfirm = vm::confirmDecision,
                    onTask = vm::addTask, onToggle = vm::toggleTask,
                    onEditDecision = vm::editDecision, onPinDecision = vm::pinDecision,
                    onDeleteDecision = vm::deleteDecision,
                    onEditTask = vm::editTask, onDeleteTask = vm::deleteTask,
                    onUpdateProject = vm::updateProject,
                    onArchiveProject = vm::setArchive,
                    onDeleteProject = vm::deleteActiveProject,
                    onCopy = { brief ->
                        val clip = android.content.ClipData.newPlainText("Context Capsule", vm.capsule(compact = brief))
                        (context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager).setPrimaryClip(clip)
                        Toast.makeText(context, "Капсула скопирована", Toast.LENGTH_SHORT).show()
                    },
                    onShare = { brief ->
                        val send = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, vm.capsule(compact = brief))
                        }
                        context.startActivity(Intent.createChooser(send, "Передать контекст в ChatGPT"))
                    },
                    onExport = { backup, brief ->
                        if (backup) showBackupWarning = true
                        else {
                            exportText = vm.capsule(compact = brief)
                            exportLauncher.launch("context-capsule.md")
                        }
                    })
            }
        }
        if (incoming != null) ShareInboxDialog(
            text = incoming,
            projects = state.projects,
            onSave = { id ->
                vm.captureSharedText(id, incoming)
                onShareDismiss()
            },
            onDismiss = onShareDismiss
        )
        if (showBackupWarning) AlertDialog(
            onDismissRequest = { showBackupWarning = false },
            title = { Text("Незашифрованная резервная копия") },
            text = { Text("Файл JSON содержит личные заметки в открытом виде. Сохраняй его только в надёжном месте.") },
            confirmButton = { TextButton(onClick = {
                showBackupWarning = false
                exportText = vm.backup()
                exportLauncher.launch("context-capsule-backup.json")
            }) { Text("Сохранить копию") } },
            dismissButton = { TextButton(onClick = { showBackupWarning = false }) { Text("Отмена") } }
        )
        }
    }
}


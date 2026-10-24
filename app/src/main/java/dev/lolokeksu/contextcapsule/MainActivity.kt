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

private val Ink = Color(PremiumPalette.Background.toInt())
private val Surface = Color(PremiumPalette.Panel.toInt())
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
        CompositionLocalProvider(LocalContentColor provides Color(PremiumPalette.Title.toInt())) {
        Column(Modifier.fillMaxSize().background(Ink).statusBarsPadding().navigationBarsPadding()) {
            if (state.active == null) {
                PremiumHomeContent(state, onOpen = vm::open, onCreate = vm::create,
                    onImport = { importLauncher.launch(arrayOf("application/json", "text/plain", "*/*")) })
            } else {
                PremiumProjectContent(state, onBack = { vm.open(null) }, onSync = vm::sync,
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
                        context.startActivity(Intent.createChooser(send, "Передать контекст в ChatGPT"))
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
}


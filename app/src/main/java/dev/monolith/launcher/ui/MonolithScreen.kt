package dev.monolith.launcher.ui

import android.content.ComponentName
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import dev.monolith.launcher.R
import dev.monolith.launcher.data.LaunchableApp
import dev.monolith.launcher.data.LauncherPreferences
import dev.monolith.launcher.data.filterApps
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.delay

private val Background = Color(0xFF030303)
private val Panel = Color(0xFF171717)
private val Muted = Color(0xFFACACAC)
private val MainText = Color(0xFFF5F5F5)
private val Outline = Color(0xFF333333)
private enum class Screen { HOME, ALL_APPS, SETTINGS }

@Composable
fun MonolithScreen(
    apps: List<LaunchableApp>,
    pinnedIds: List<String>,
    preferences: LauncherPreferences,
    loading: Boolean,
    loadError: Boolean,
    isDefaultHome: Boolean,
    onMakeDefaultHome: () -> Unit,
    onRetry: () -> Unit,
    onLaunch: (LaunchableApp) -> Unit,
    onTogglePin: (LaunchableApp) -> Unit,
    onMovePinned: (LaunchableApp, Int) -> Unit,
    onAppInfo: (LaunchableApp) -> Unit,
    onPreferencesChange: (LauncherPreferences) -> Unit
) {
    var screen by rememberSaveable { mutableStateOf(Screen.HOME) }
    var query by rememberSaveable { mutableStateOf("") }
    var selectedApp by remember { mutableStateOf<LaunchableApp?>(null) }
    BackHandler(enabled = screen != Screen.HOME) {
        screen = Screen.HOME
        query = ""
    }
    val pinned = remember(apps, pinnedIds) {
        val byId = apps.associateBy { it.id }
        pinnedIds.mapNotNull(byId::get)
    }
    val visibleApps = remember(apps, query) { filterApps(apps, query) }

    MaterialTheme(
        colorScheme = darkColorScheme(
            background = Background, surface = Background,
            primary = MainText, onPrimary = Background, onSurface = MainText
        )
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = Background) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Header(screen, onSettings = { screen = Screen.SETTINGS }, onHome = { screen = Screen.HOME })
                Spacer(Modifier.height(25.dp))
                when (screen) {
                    Screen.HOME -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .pointerInput(Unit) {
                                    var drag = 0f
                                    detectVerticalDragGestures(
                                        onDragStart = { drag = 0f },
                                        onVerticalDrag = { change, amount ->
                                            drag += amount
                                            change.consume()
                                        },
                                        onDragEnd = { if (drag < -100f) screen = Screen.ALL_APPS }
                                    )
                                }
                        ) {
                            Clock()
                            Spacer(Modifier.height(34.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(stringResource(R.string.pinned_apps), color = Muted, letterSpacing = 2.sp, fontSize = 12.sp)
                                Text("${pinned.size}", color = Muted, fontSize = 12.sp)
                            }
                            Spacer(Modifier.height(14.dp))
                            when {
                                loadError -> ErrorState(onRetry)
                                loading && apps.isEmpty() -> Text(stringResource(R.string.loading), color = Muted)
                                pinned.isEmpty() -> {
                                    Box(
                                        Modifier.fillMaxWidth().background(Panel, RoundedCornerShape(18.dp)).padding(22.dp)
                                    ) { Text(stringResource(R.string.empty_pinned), color = Muted, fontSize = 14.sp) }
                                }
                                else -> AppGrid(
                                    pinned, pinnedIds, preferences, onLaunch,
                                    { selectedApp = it }, Modifier.weight(1f)
                                )
                            }
                            if (pinned.isEmpty() || loadError || (loading && apps.isEmpty())) Spacer(Modifier.weight(1f))
                            if (!isDefaultHome) {
                                OutlinedButton(
                                    onClick = onMakeDefaultHome, modifier = Modifier.fillMaxWidth()
                                ) { Text(stringResource(R.string.make_default)) }
                                Spacer(Modifier.height(8.dp))
                            }
                            MonolithButton(stringResource(R.string.all_apps)) { screen = Screen.ALL_APPS }
                            Spacer(Modifier.height(8.dp))
                            Text(
                                stringResource(R.string.swipe_hint),
                                modifier = Modifier.fillMaxWidth(),
                                color = Muted, fontSize = 11.sp, textAlign = TextAlign.Center
                            )
                        }
                    }
                    Screen.ALL_APPS -> {
                        Text(stringResource(R.string.all_apps), fontSize = 27.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(
                            value = query,
                            onValueChange = { query = it },
                            label = { Text(stringResource(R.string.search_apps)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            stringResource(R.string.apps_count, visibleApps.size),
                            color = Muted, fontSize = 12.sp
                        )
                        Spacer(Modifier.height(12.dp))
                        when {
                            loadError -> {
                                ErrorState(onRetry)
                                Spacer(Modifier.weight(1f))
                            }
                            loading && apps.isEmpty() -> {
                                Text(stringResource(R.string.loading), color = Muted)
                                Spacer(Modifier.weight(1f))
                            }
                            visibleApps.isEmpty() -> {
                                Text(stringResource(R.string.no_results), color = Muted)
                                Spacer(Modifier.weight(1f))
                            }
                            else -> AppGrid(
                                visibleApps, pinnedIds, preferences, onLaunch,
                                { selectedApp = it }, Modifier.weight(1f)
                            )
                        }
                        Spacer(Modifier.height(10.dp))
                        MonolithButton(stringResource(R.string.back_home)) {
                            screen = Screen.HOME
                            query = ""
                        }
                    }
                    Screen.SETTINGS -> SettingsPage(
                        preferences = preferences,
                        isDefaultHome = isDefaultHome,
                        onPreferencesChange = onPreferencesChange,
                        onMakeDefaultHome = onMakeDefaultHome,
                        onBack = { screen = Screen.HOME }
                    )
                }
            }
        }
        selectedApp?.let { app ->
            AppActionSheet(
                app = app,
                pinnedIndex = pinnedIds.indexOf(app.id),
                pinnedCount = pinnedIds.size,
                onDismiss = { selectedApp = null },
                onTogglePin = { selectedApp = null; onTogglePin(app) },
                onMove = { delta -> selectedApp = null; onMovePinned(app, delta) },
                onAppInfo = { selectedApp = null; onAppInfo(app) }
            )
        }
    }
}

@Composable
private fun Header(screen: Screen, onSettings: () -> Unit, onHome: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("MONOLITH", fontSize = 17.sp, letterSpacing = 3.sp, fontWeight = FontWeight.Bold)
        if (screen == Screen.SETTINGS) {
            TextButton(onClick = onHome) { Text(stringResource(R.string.back_home)) }
        } else {
            TextButton(onClick = onSettings) { Text(stringResource(R.string.settings)) }
        }
    }
}

@Composable
private fun Clock() {
    var now by remember { mutableStateOf(LocalDateTime.now()) }
    val locale = Locale.getDefault()
    LaunchedEffect(Unit) {
        while (true) {
            now = LocalDateTime.now()
            delay(20_000L)
        }
    }
    Text(now.format(DateTimeFormatter.ofPattern("HH:mm", locale)), fontSize = 76.sp, fontWeight = FontWeight.Light)
    Text(
        now.format(DateTimeFormatter.ofPattern("EEEE, d MMMM", locale)),
        color = Muted, fontSize = 14.sp
    )
}

@Composable
private fun ErrorState(onRetry: () -> Unit) {
    Column(Modifier.fillMaxWidth().background(Panel, RoundedCornerShape(16.dp)).padding(16.dp)) {
        Text(stringResource(R.string.catalog_failed), color = MainText)
        Spacer(Modifier.height(8.dp))
        TextButton(onClick = onRetry) { Text(stringResource(R.string.retry)) }
    }
}

@Composable
private fun SettingsPage(
    preferences: LauncherPreferences,
    isDefaultHome: Boolean,
    onPreferencesChange: (LauncherPreferences) -> Unit,
    onMakeDefaultHome: () -> Unit,
    onBack: () -> Unit
) {
    Text(stringResource(R.string.settings), fontSize = 29.sp, fontWeight = FontWeight.SemiBold)
    Spacer(Modifier.height(28.dp))
    Text(stringResource(R.string.grid_columns), color = Muted)
    Spacer(Modifier.height(12.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        (3..6).forEach { columns ->
            val selected = preferences.columns == columns
            if (selected) {
                Button(onClick = { onPreferencesChange(preferences.copy(columns = columns)) }) {
                    Text("$columns")
                }
            } else {
                OutlinedButton(onClick = { onPreferencesChange(preferences.copy(columns = columns)) }) {
                    Text("$columns")
                }
            }
        }
    }
    Spacer(Modifier.height(24.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(stringResource(R.string.show_labels))
        Switch(
            checked = preferences.showLabels,
            onCheckedChange = { onPreferencesChange(preferences.copy(showLabels = it)) }
        )
    }
    Spacer(Modifier.height(28.dp))
    Text(
        stringResource(R.string.settings_note),
        color = Muted, fontSize = 13.sp
    )
    Spacer(Modifier.height(26.dp))
    if (!isDefaultHome) {
        OutlinedButton(onClick = onMakeDefaultHome, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.make_default))
        }
        Spacer(Modifier.height(12.dp))
    }
    MonolithButton(stringResource(R.string.back_home), onBack)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppActionSheet(
    app: LaunchableApp,
    pinnedIndex: Int,
    pinnedCount: Int,
    onDismiss: () -> Unit,
    onTogglePin: () -> Unit,
    onMove: (Int) -> Unit,
    onAppInfo: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = Panel) {
        Column(modifier = Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, bottom = 26.dp)) {
            Text(app.label, fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
            Text(app.packageName, color = Muted, fontSize = 12.sp)
            Spacer(Modifier.height(12.dp))
            SheetAction(
                if (pinnedIndex >= 0) stringResource(R.string.unpin_app) else stringResource(R.string.pin_app),
                onTogglePin
            )
            if (pinnedIndex > 0) SheetAction(stringResource(R.string.move_left), { onMove(-1) })
            if (pinnedIndex >= 0 && pinnedIndex < pinnedCount - 1) {
                SheetAction(stringResource(R.string.move_right), { onMove(1) })
            }
            SheetAction(stringResource(R.string.app_info), onAppInfo)
        }
    }
}

@Composable
private fun SheetAction(label: String, onClick: () -> Unit) {
    TextButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Text(label, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Start, color = MainText)
    }
}

@Composable
private fun MonolithButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(55.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Panel),
        shape = RoundedCornerShape(18.dp)
    ) {
        Text(text, color = MainText)
    }
}

@Composable
private fun AppGrid(
    apps: List<LaunchableApp>,
    pinnedIds: List<String>,
    preferences: LauncherPreferences,
    onLaunch: (LaunchableApp) -> Unit,
    onLongClick: (LaunchableApp) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(preferences.columns),
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
        contentPadding = PaddingValues(vertical = 12.dp)
    ) {
        items(apps, key = { it.id }) { app ->
            AppTile(
                app, app.id in pinnedIds, preferences.showLabels,
                { onLaunch(app) }, { onLongClick(app) }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AppTile(
    app: LaunchableApp,
    pinned: Boolean,
    showLabel: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val pm = LocalContext.current.packageManager
    val openLabel = stringResource(R.string.open_app)
    val actionsLabel = stringResource(R.string.app_actions)
    val painter = remember(app.id) {
        runCatching {
            val icon = pm.getActivityIcon(ComponentName(app.packageName, app.activityName))
            BitmapPainter(icon.toBitmap(width = 96, height = 96).asImageBitmap())
        }.getOrNull()
    }
    Column(
        modifier = Modifier
            .combinedClickable(
                onClickLabel = openLabel,
                onLongClickLabel = actionsLabel,
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(3.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(Modifier.size(56.dp), contentAlignment = Alignment.Center) {
            if (painter != null) {
                Image(painter, contentDescription = app.label, modifier = Modifier.size(52.dp))
            } else {
                Text(app.label.take(1), fontSize = 25.sp)
            }
        }
        if (showLabel) {
            Spacer(Modifier.height(7.dp))
            Text(
                app.label, fontSize = 11.sp, maxLines = 1,
                overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center
            )
        }
        if (pinned) Text("•", color = Muted, fontSize = 12.sp)
    }
}

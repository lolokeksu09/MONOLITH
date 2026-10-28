package dev.monolith.launcher.ui

import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.ComponentName
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.zIndex
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import android.widget.TextView
import kotlin.math.roundToInt
import dev.monolith.launcher.R
import dev.monolith.launcher.data.HomeLayout
import dev.monolith.launcher.data.HomeEntry
import dev.monolith.launcher.data.LaunchableApp
import dev.monolith.launcher.data.LaunchStats
import dev.monolith.launcher.data.AppSort
import dev.monolith.launcher.data.sortApps
import dev.monolith.launcher.data.LauncherPreferences
import dev.monolith.launcher.data.filterApps
import dev.monolith.launcher.data.filterHiddenApps
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.delay

private val Background = Color(0xFF030303)
private val Panel = Color(0xFF171717)
private val Muted = Color(0xFFACACAC)
private val MainText = Color(0xFFF5F5F5)
private val Outline = Color(0xFF333333)
private enum class Screen { HOME, ALL_APPS, SETTINGS, WIDGETS, HIDDEN_APPS }

@Composable
fun MonolithScreen(
    apps: List<LaunchableApp>,
    layout: HomeLayout,
    preferences: LauncherPreferences,
    launchStats: Map<String, LaunchStats>,
    hiddenApps: Set<String>,
    loading: Boolean,
    loadError: Boolean,
    widgetIds: List<Int>,
    widgetHost: AppWidgetHost,
    widgetManager: AppWidgetManager,
    widgetProviders: List<AppWidgetProviderInfo>,
    widgetError: Boolean,
    isDefaultHome: Boolean,
    onMakeDefaultHome: () -> Unit,
    onRetry: () -> Unit,
    onLaunch: (LaunchableApp) -> Unit,
    onPin: (LaunchableApp) -> Unit,
    onUnpin: (LaunchableApp) -> Unit,
    onNewFolder: (LaunchableApp) -> Unit,
    onAddToFolder: (LaunchableApp, String) -> Unit,
    onRenameFolder: (String, String) -> Unit,
    onDeleteFolder: (String) -> Unit,
    onMoveOutOfFolder: (LaunchableApp, String) -> Unit,
    onReorder: (Int, Int) -> Unit,
    onAppInfo: (LaunchableApp) -> Unit,
    onPreferencesChange: (LauncherPreferences) -> Unit,
    onShowWidgets: () -> Unit,
    onChooseWidget: (AppWidgetProviderInfo) -> Unit,
    onRemoveWidget: (Int) -> Unit,
    onExportHome: () -> Unit,
    onImportHome: () -> Unit,
    onClearLaunchHistory: () -> Unit,
    onHideApp: (LaunchableApp) -> Unit,
    onUnhideApp: (LaunchableApp) -> Unit
) {
    var screen by rememberSaveable { mutableStateOf(Screen.HOME) }
    var query by rememberSaveable { mutableStateOf("") }
    var editing by rememberSaveable { mutableStateOf(false) }
    var selectedApp by remember { mutableStateOf<LaunchableApp?>(null) }
    var selectedFolderId by remember { mutableStateOf<String?>(null) }
    val pinnedIds = remember(layout) { layout.appIds }
    val visibleApps = remember(apps, query, preferences.appSort, launchStats, hiddenApps) {
        sortApps(filterApps(filterHiddenApps(apps, hiddenApps), query), preferences.appSort, launchStats)
    }
    val byId = remember(apps) { apps.associateBy { it.id } }
    val folders = remember(layout) { layout.entries.filterIsInstance<HomeEntry.Folder>() }
    BackHandler(enabled = screen != Screen.HOME || editing) {
        if (editing) editing = false else { screen = Screen.HOME; query = "" }
    }

    MaterialTheme(colorScheme = darkColorScheme(
        background = Background, surface = Background,
        primary = MainText, onPrimary = Background, onSurface = MainText
    )) {
        Surface(modifier = Modifier.fillMaxSize(), color = Background) {
            Column(
                modifier = Modifier.fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Header(screen, onSettings = { editing = false; screen = Screen.SETTINGS },
                    onHome = { screen = Screen.HOME; editing = false })
                Spacer(Modifier.height(14.dp))
                when (screen) {
                    Screen.HOME -> {
                        Column(
                            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                        ) {
                            // Swipe on the clock: leave home scrolling and widget touch targets intact.
                            Box(
                                modifier = Modifier.fillMaxWidth().pointerInput(editing) {
                                    if (!editing) {
                                        var total = 0f
                                        detectVerticalDragGestures(
                                            onDragStart = { total = 0f },
                                            onVerticalDrag = { change, drag ->
                                                total += drag
                                                change.consume()
                                            },
                                            onDragEnd = {
                                                if (total < -90f) {
                                                    screen = Screen.ALL_APPS
                                                    query = ""
                                                }
                                            }
                                        )
                                    }
                                }
                            ) {
                                Column {
                                    Clock()
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        stringResource(R.string.swipe_clock_hint),
                                        color = Muted, fontSize = 11.sp
                                    )
                                }
                            }
                            Spacer(Modifier.height(14.dp))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically) {
                                Text(stringResource(R.string.pinned_apps), color = Muted, letterSpacing = 2.sp, fontSize = 12.sp)
                                TextButton(onClick = { editing = !editing }) {
                                    Text(stringResource(if (editing) R.string.finish_edit else R.string.edit_home))
                                }
                            }
                            if (editing) {
                                Text(stringResource(R.string.drag_hint), color = Muted, fontSize = 12.sp)
                            }
                            Spacer(Modifier.height(10.dp))
                            when {
                                loadError -> ErrorState(onRetry)
                                loading && apps.isEmpty() -> Text(stringResource(R.string.loading), color = Muted)
                                layout.entries.isEmpty() -> {
                                    Box(Modifier.fillMaxWidth().background(Panel, RoundedCornerShape(18.dp)).padding(22.dp)) {
                                        Text(stringResource(R.string.empty_pinned), color = Muted, fontSize = 14.sp)
                                    }
                                }
                                else -> HomeGrid(
                                    entries = layout.entries, byId = byId, columns = preferences.columns,
                                    labels = preferences.showLabels, editing = editing,
                                    onLaunch = onLaunch, onAppActions = { selectedApp = it },
                                    onFolder = { selectedFolderId = it },
                                    onMove = onReorder
                                )
                            }
                            Spacer(Modifier.height(20.dp))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                OutlinedButton(onClick = { onShowWidgets(); screen = Screen.WIDGETS }, modifier = Modifier.weight(1f)) {
                                    Text(stringResource(R.string.add_widget), maxLines = 1)
                                }
                                OutlinedButton(onClick = { editing = false; screen = Screen.ALL_APPS }, modifier = Modifier.weight(1f)) {
                                    Text(stringResource(R.string.all_apps), maxLines = 1)
                                }
                            }
                            if (widgetIds.isNotEmpty()) {
                                Spacer(Modifier.height(24.dp))
                                Text(stringResource(R.string.widgets_section), color = Muted, letterSpacing = 2.sp, fontSize = 12.sp)
                                Spacer(Modifier.height(12.dp))
                                WidgetSection(widgetIds, widgetHost, widgetManager, onRemoveWidget)
                            }
                            if (!isDefaultHome) {
                                Spacer(Modifier.height(12.dp))
                                OutlinedButton(onClick = onMakeDefaultHome, modifier = Modifier.fillMaxWidth()) {
                                    Text(stringResource(R.string.make_default))
                                }
                            }
                            Spacer(Modifier.height(24.dp))
                        }
                    }
                    Screen.ALL_APPS -> {
                        Text(stringResource(R.string.all_apps), fontSize = 27.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(
                            value = query, onValueChange = { query = it },
                            label = { Text(stringResource(R.string.search_apps)) },
                            singleLine = true, modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(stringResource(R.string.apps_count, visibleApps.size), color = Muted, fontSize = 12.sp)
                            SortPicker(
                                selected = preferences.appSort,
                                onChange = { onPreferencesChange(preferences.copy(appSort = it)) }
                            )
                        }
                        if (preferences.appSort == AppSort.FREQUENT || preferences.appSort == AppSort.LAST_OPENED) {
                            Text(stringResource(R.string.tracked_launches_only), color = Muted, fontSize = 11.sp)
                        }
                        Spacer(Modifier.height(12.dp))
                        when {
                            loadError -> { ErrorState(onRetry); Spacer(Modifier.weight(1f)) }
                            loading && apps.isEmpty() -> {
                                Text(stringResource(R.string.loading), color = Muted)
                                Spacer(Modifier.weight(1f))
                            }
                            visibleApps.isEmpty() -> {
                                Text(stringResource(R.string.no_results), color = Muted)
                                Spacer(Modifier.weight(1f))
                            }
                            else -> AppGrid(visibleApps, pinnedIds, preferences, onLaunch,
                                { selectedApp = it }, Modifier.weight(1f))
                        }
                        Spacer(Modifier.height(10.dp))
                        MonolithButton(stringResource(R.string.back_home)) { screen = Screen.HOME; query = "" }
                    }
                    Screen.SETTINGS -> SettingsPage(
                        preferences, isDefaultHome, onPreferencesChange, onMakeDefaultHome,
                        onBack = { screen = Screen.HOME },
                        onExportHome = onExportHome, onImportHome = onImportHome,
                        onClearLaunchHistory = onClearLaunchHistory,
                        hiddenCount = hiddenApps.size,
                        onManageHidden = { screen = Screen.HIDDEN_APPS }
                    )
                    Screen.HIDDEN_APPS -> {
                        Text(stringResource(R.string.hidden_apps_title), fontSize = 27.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(8.dp))
                        Text(stringResource(R.string.hidden_apps_description), color = Muted, fontSize = 13.sp)
                        Spacer(Modifier.height(14.dp))
                        val hiddenInstalled = remember(apps, hiddenApps) {
                            apps.filter { it.id in hiddenApps }.sortedBy { it.label.lowercase() }
                        }
                        if (hiddenInstalled.isEmpty()) {
                            Text(stringResource(R.string.no_hidden_apps), color = Muted)
                            Spacer(Modifier.weight(1f))
                        } else {
                            LazyColumn(modifier = Modifier.weight(1f)) {
                                items(hiddenInstalled, key = { it.id }) { app ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(app.label, modifier = Modifier.weight(1f), maxLines = 2)
                                        TextButton(onClick = { onUnhideApp(app) }) {
                                            Text(stringResource(R.string.unhide_app))
                                        }
                                    }
                                }
                            }
                        }
                        MonolithButton(stringResource(R.string.settings)) { screen = Screen.SETTINGS }
                    }
                    Screen.WIDGETS -> {
                        Text(stringResource(R.string.choose_widget), fontSize = 27.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(12.dp))
                        if (widgetError) Text(stringResource(R.string.widget_failed), color = Muted)
                        if (widgetProviders.isEmpty()) {
                            Text(stringResource(R.string.no_widgets), color = Muted)
                            Spacer(Modifier.weight(1f))
                        } else {
                            val pm = LocalContext.current.packageManager
                            LazyColumn(modifier = Modifier.weight(1f)) {
                                items(widgetProviders, key = { it.provider.flattenToString() }) { provider ->
                                    TextButton(
                                        onClick = { onChooseWidget(provider); screen = Screen.HOME },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(provider.loadLabel(pm), modifier = Modifier.fillMaxWidth(),
                                            textAlign = TextAlign.Start, color = MainText)
                                    }
                                }
                            }
                        }
                        MonolithButton(stringResource(R.string.back_home)) { screen = Screen.HOME }
                    }
                }
            }
        }

        selectedApp?.let { app ->
            AppActionSheet(
                app = app, pinnedIndex = pinnedIds.indexOf(app.id),
                isTopLevel = layout.entries.any { it is HomeEntry.App && it.appId == app.id },
                folders = folders,
                onDismiss = { selectedApp = null },
                onTogglePin = { selectedApp = null; if (app.id in pinnedIds) onUnpin(app) else onPin(app) },
                onNewFolder = { selectedApp = null; onNewFolder(app) },
                onAddToFolder = { folderId -> selectedApp = null; onAddToFolder(app, folderId) },
                onAppInfo = { selectedApp = null; onAppInfo(app) },
                onHide = { selectedApp = null; onHideApp(app) }
            )
        }
        selectedFolderId?.let { id ->
            val folder = folders.firstOrNull { it.id == id }
            if (folder == null) selectedFolderId = null else FolderSheet(
                folder = folder, byId = byId,
                onDismiss = { selectedFolderId = null },
                onLaunch = { selectedFolderId = null; onLaunch(it) },
                onRename = { name -> onRenameFolder(id, name) },
                onDelete = { selectedFolderId = null; onDeleteFolder(id) },
                onRemoveApp = { selectedFolderId = null; onMoveOutOfFolder(it, id) }
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
    onBack: () -> Unit,
    onExportHome: () -> Unit,
    onImportHome: () -> Unit,
    onClearLaunchHistory: () -> Unit,
    hiddenCount: Int,
    onManageHidden: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
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
    Spacer(Modifier.height(18.dp))
    Text(stringResource(R.string.drawer_settings), fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
    Spacer(Modifier.height(12.dp))
    Text(stringResource(R.string.drawer_columns), color = Muted, fontSize = 13.sp)
    Spacer(Modifier.height(10.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        (3..6).forEach { count ->
            if (count == preferences.drawerColumns) {
                Button(onClick = { onPreferencesChange(preferences.copy(drawerColumns = count)) }) {
                    Text("$count")
                }
            } else {
                OutlinedButton(onClick = { onPreferencesChange(preferences.copy(drawerColumns = count)) }) {
                    Text("$count")
                }
            }
        }
    }
    Spacer(Modifier.height(15.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(stringResource(R.string.drawer_labels), modifier = Modifier.weight(1f))
        Switch(checked = preferences.showDrawerLabels,
            onCheckedChange = { onPreferencesChange(preferences.copy(showDrawerLabels = it)) })
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(stringResource(R.string.pin_dots), modifier = Modifier.weight(1f))
        Switch(checked = preferences.showPinDots,
            onCheckedChange = { onPreferencesChange(preferences.copy(showPinDots = it)) })
    }
    Spacer(Modifier.height(12.dp))
    Text(stringResource(R.string.drawer_icon_size), color = Muted, fontSize = 13.sp)
    Spacer(Modifier.height(9.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf(40, 48, 56).forEach { size ->
            if (size == preferences.drawerIconSize) {
                Button(onClick = { onPreferencesChange(preferences.copy(drawerIconSize = size)) }) {
                    Text("$size")
                }
            } else {
                OutlinedButton(onClick = { onPreferencesChange(preferences.copy(drawerIconSize = size)) }) {
                    Text("$size")
                }
            }
        }
    }
    Spacer(Modifier.height(14.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(stringResource(R.string.default_sort), color = Muted)
        SortPicker(selected = preferences.appSort,
            onChange = { onPreferencesChange(preferences.copy(appSort = it)) })
    }
    Spacer(Modifier.height(8.dp))
    Text(stringResource(R.string.tracked_launches_only), color = Muted, fontSize = 12.sp)
    Spacer(Modifier.height(8.dp))
    OutlinedButton(onClick = onManageHidden, modifier = Modifier.fillMaxWidth()) {
        Text(stringResource(R.string.manage_hidden_apps, hiddenCount))
    }
    Spacer(Modifier.height(10.dp))
    OutlinedButton(onClick = onClearLaunchHistory, modifier = Modifier.fillMaxWidth()) {
        Text(stringResource(R.string.clear_launches))
    }
    Spacer(Modifier.height(24.dp))
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
    Spacer(Modifier.height(20.dp))
    Text(stringResource(R.string.backup_title), fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
    Spacer(Modifier.height(10.dp))
    Text(stringResource(R.string.backup_description), color = Muted, fontSize = 13.sp)
    Spacer(Modifier.height(12.dp))
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedButton(onClick = onExportHome, modifier = Modifier.weight(1f)) {
            Text(stringResource(R.string.export_home))
        }
        OutlinedButton(onClick = onImportHome, modifier = Modifier.weight(1f)) {
            Text(stringResource(R.string.import_home))
        }
    }
    Spacer(Modifier.height(20.dp))
    MonolithButton(stringResource(R.string.back_home), onBack)
    Spacer(Modifier.height(18.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppActionSheet(
    app: LaunchableApp,
    pinnedIndex: Int,
    isTopLevel: Boolean,
    folders: List<HomeEntry.Folder>,
    onDismiss: () -> Unit,
    onTogglePin: () -> Unit,
    onNewFolder: () -> Unit,
    onAddToFolder: (String) -> Unit,
    onAppInfo: () -> Unit,
    onHide: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = Panel) {
        Column(
            modifier = Modifier.fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(start = 24.dp, end = 24.dp, bottom = 26.dp)
        ) {
            Text(app.label, fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
            Text(app.packageName, color = Muted, fontSize = 12.sp)
            Spacer(Modifier.height(12.dp))
            SheetAction(
                if (pinnedIndex >= 0) stringResource(R.string.unpin_app) else stringResource(R.string.pin_app),
                onTogglePin
            )
            if (isTopLevel) SheetAction(stringResource(R.string.create_folder), onNewFolder)
            folders.forEach { folder ->
                if (app.id !in folder.appIds) {
                    SheetAction(stringResource(R.string.move_to_folder, folder.name)) { onAddToFolder(folder.id) }
                }
            }
            SheetAction(stringResource(R.string.app_info), onAppInfo)
            SheetAction(stringResource(R.string.hide_app), onHide)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FolderSheet(
    folder: HomeEntry.Folder,
    byId: Map<String, LaunchableApp>,
    onDismiss: () -> Unit,
    onLaunch: (LaunchableApp) -> Unit,
    onRename: (String) -> Unit,
    onDelete: () -> Unit,
    onRemoveApp: (LaunchableApp) -> Unit
) {
    var folderName by remember(folder.id) { mutableStateOf(folder.name) }
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = Panel) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp)) {
            OutlinedTextField(
                value = folderName, onValueChange = { folderName = it.take(40) },
                label = { Text(stringResource(R.string.folder_name)) }, singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            TextButton(onClick = { onRename(folderName) }) { Text(stringResource(R.string.save_name)) }
            Column(modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp).verticalScroll(rememberScrollState())) {
                folder.appIds.mapNotNull(byId::get).forEach { app ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { onLaunch(app) }) {
                            Text(app.label, color = MainText, maxLines = 1)
                        }
                        TextButton(onClick = { onRemoveApp(app) }) {
                            Text(stringResource(R.string.move_out), color = Muted)
                        }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            TextButton(onClick = onDelete) {
                Text(stringResource(R.string.dissolve_folder), color = MainText)
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun WidgetSection(
    ids: List<Int>, host: AppWidgetHost, manager: AppWidgetManager,
    onRemove: (Int) -> Unit
) {
    ids.forEach { id ->
        val info = remember(id) { manager.getAppWidgetInfo(id) }
        Column(
            modifier = Modifier.fillMaxWidth()
                .background(Panel, RoundedCornerShape(18.dp))
                .padding(12.dp)
        ) {
            if (info == null) {
                Text(stringResource(R.string.widget_unavailable), color = Muted)
            } else {
                val context = LocalContext.current
                AndroidView(
                    factory = {
                        runCatching { host.createView(context, id, info) }
                            .getOrElse {
                                TextView(context).apply { text = context.getString(R.string.widget_unavailable) }
                            }
                    },
                    modifier = Modifier.fillMaxWidth().height(194.dp)
                )
            }
            TextButton(onClick = { onRemove(id) }) {
                Text(stringResource(R.string.remove_widget), color = Muted)
            }
        }
        Spacer(Modifier.height(12.dp))
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
        columns = GridCells.Fixed(preferences.drawerColumns),
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
        contentPadding = PaddingValues(vertical = 12.dp)
    ) {
        items(apps, key = { it.id }) { app ->
            AppTile(
                app, app.id in pinnedIds && preferences.showPinDots,
                preferences.showDrawerLabels, preferences.drawerIconSize,
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
    iconSize: Int,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val bitmap = rememberAppIcon(app)
    val openLabel = stringResource(R.string.open_app)
    val actionsLabel = stringResource(R.string.app_actions)
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
        Box(Modifier.size((iconSize + 4).dp), contentAlignment = Alignment.Center) {
            if (bitmap != null) {
                Image(bitmap.asImageBitmap(), contentDescription = app.label, modifier = Modifier.size(iconSize.dp))
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

@Composable
private fun HomeGrid(
    entries: List<HomeEntry>,
    byId: Map<String, LaunchableApp>,
    columns: Int,
    labels: Boolean,
    editing: Boolean,
    onLaunch: (LaunchableApp) -> Unit,
    onAppActions: (LaunchableApp) -> Unit,
    onFolder: (String) -> Unit,
    onMove: (Int, Int) -> Unit
) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val gap = 8.dp
        val cellWidth = (maxWidth - gap * (columns - 1)) / columns
        val density = LocalDensity.current
        val stepX = with(density) { (cellWidth + gap).toPx() }
        val stepY = with(density) { 112.dp.toPx() }
        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            entries.chunked(columns).forEachIndexed { row, elements ->
                Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
                    elements.forEachIndexed { column, entry ->
                        val index = row * columns + column
                        HomeTile(
                            entry = entry, app = (entry as? HomeEntry.App)?.let { byId[it.appId] },
                            cellWidth = cellWidth, labels = labels, editing = editing,
                            onClick = {
                                when (entry) {
                                    is HomeEntry.App -> byId[entry.appId]?.let(onLaunch)
                                    is HomeEntry.Folder -> onFolder(entry.id)
                                }
                            },
                            onLongClick = {
                                when (entry) {
                                    is HomeEntry.App -> byId[entry.appId]?.let(onAppActions)
                                    is HomeEntry.Folder -> onFolder(entry.id)
                                }
                            },
                            onDrop = { delta ->
                                val shiftX = (delta.x / stepX).roundToInt()
                                val shiftY = (delta.y / stepY).roundToInt()
                                val target = (index + shiftX + shiftY * columns).coerceIn(0, entries.lastIndex)
                                if (index != target) onMove(index, target)
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HomeTile(
    entry: HomeEntry,
    app: LaunchableApp?,
    cellWidth: Dp,
    labels: Boolean,
    editing: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onDrop: (Offset) -> Unit
) {
    var drag by remember(entry.key, editing) { mutableStateOf(Offset.Zero) }
    val density = LocalDensity.current
    val bitmap = rememberAppIcon(app)
    val gesture = if (editing) {
        Modifier.pointerInput(entry.key) {
            detectDragGesturesAfterLongPress(
                onDragStart = { drag = Offset.Zero },
                onDrag = { change, amount -> change.consume(); drag += amount },
                onDragEnd = { val end = drag; drag = Offset.Zero; onDrop(end) },
                onDragCancel = { drag = Offset.Zero }
            )
        }
    } else {
        Modifier.combinedClickable(onClick = onClick, onLongClick = onLongClick)
    }
    Column(
        modifier = Modifier.width(cellWidth).height(108.dp)
            .zIndex(if (drag != Offset.Zero) 3f else 0f)
            .offset {
                IntOffset(drag.x.roundToInt(), drag.y.roundToInt())
            }
            .then(gesture)
            .background(if (editing) Outline else Panel, RoundedCornerShape(16.dp))
            .padding(horizontal = 3.dp, vertical = 9.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(Modifier.size(49.dp), contentAlignment = Alignment.Center) {
            if (entry is HomeEntry.Folder) {
                Text("▦", fontSize = 34.sp, color = MainText)
            } else if (bitmap != null) {
                Image(bitmap.asImageBitmap(), contentDescription = app?.label, modifier = Modifier.size(45.dp))
            } else {
                Text(app?.label?.take(1).orEmpty(), fontSize = 25.sp)
            }
        }
        if (labels) {
            Spacer(Modifier.height(7.dp))
            Text(
                when (entry) {
                    is HomeEntry.App -> app?.label ?: stringResource(R.string.missing_app)
                    is HomeEntry.Folder -> entry.name
                },
                fontSize = 11.sp, color = MainText, maxLines = 1,
                overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun SortPicker(
    selected: AppSort,
    onChange: (AppSort) -> Unit
) {
    var menuOpen by remember { mutableStateOf(false) }
    Box {
        TextButton(onClick = { menuOpen = true }) {
            Text(stringResource(sortLabel(selected)), maxLines = 1, fontSize = 12.sp)
            Text("  ▾", color = Muted, fontSize = 12.sp)
        }
        DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
            AppSort.entries.forEach { choice ->
                DropdownMenuItem(
                    text = { Text(stringResource(sortLabel(choice))) },
                    onClick = {
                        menuOpen = false
                        if (choice != selected) onChange(choice)
                    }
                )
            }
        }
    }
}

private fun sortLabel(value: AppSort): Int = when (value) {
    AppSort.A_Z -> R.string.sort_az
    AppSort.Z_A -> R.string.sort_za
    AppSort.FREQUENT -> R.string.sort_frequent
    AppSort.LAST_OPENED -> R.string.sort_last_opened
    AppSort.INSTALLED_NEWEST -> R.string.sort_new_install
    AppSort.INSTALLED_OLDEST -> R.string.sort_old_install
}

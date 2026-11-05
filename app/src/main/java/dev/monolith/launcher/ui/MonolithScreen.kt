package dev.monolith.launcher.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.monolith.launcher.R
import dev.monolith.launcher.data.AppSort
import dev.monolith.launcher.data.LaunchableApp
import dev.monolith.launcher.data.LaunchStats
import dev.monolith.launcher.data.LauncherPreferences
import dev.monolith.launcher.data.chooseReferenceApps
import dev.monolith.launcher.data.filterApps
import dev.monolith.launcher.data.filterHiddenApps
import dev.monolith.launcher.data.effectiveDrawerQuery
import dev.monolith.launcher.data.sortApps

private val Background = Color(0xFF050505)
private val Foreground = Color(0xFFF5F5F5)
private val Muted = Color(0xFFADADAD)
private val Panel = Color(0xFF161616)

private enum class Screen { HOME, ALL_APPS, SETTINGS, HIDDEN_APPS }

/**
 * Reference HOME is the only desktop.
 * The old classic home, Dock, folders and widget host have been removed from the UI.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonolithScreen(
    apps: List<LaunchableApp>,
    preferences: LauncherPreferences,
    homeRequestGeneration: Int,
    launchStats: Map<String, LaunchStats>,
    hiddenApps: Set<String>,
    loading: Boolean,
    loadError: Boolean,
    isDefaultHome: Boolean,
    onRetry: () -> Unit,
    onMakeDefaultHome: () -> Unit,
    onLaunch: (LaunchableApp) -> Unit,
    onAppInfo: (LaunchableApp) -> Unit,
    onPreferencesChange: (LauncherPreferences) -> Unit,
    onHideApp: (LaunchableApp) -> Unit,
    onUnhideApp: (LaunchableApp) -> Unit,
    onClearLaunchHistory: () -> Unit
) {
    var screen by rememberSaveable { mutableStateOf(Screen.HOME) }
    var query by rememberSaveable { mutableStateOf("") }
    var selectedApp by remember { mutableStateOf<LaunchableApp?>(null) }
    val visibleApps = remember(apps, hiddenApps, query, preferences, launchStats) {
        sortApps(
            filterApps(filterHiddenApps(apps, hiddenApps),
                effectiveDrawerQuery(query, preferences.showDrawerSearch)),
            preferences.appSort, launchStats
        )
    }
    val referenceApps = remember(apps, hiddenApps, launchStats) {
        chooseReferenceApps(apps, hiddenApps, launchStats)
    }
    val launchAndReturnHome: (LaunchableApp) -> Unit = { app ->
        screen = Screen.HOME
        query = ""
        selectedApp = null
        onLaunch(app)
    }

    LaunchedEffect(homeRequestGeneration) {
        if (homeRequestGeneration > 0) {
            screen = Screen.HOME
            query = ""
            selectedApp = null
        }
    }
    LaunchedEffect(preferences.showDrawerSearch) {
        if (!preferences.showDrawerSearch) query = ""
    }
    BackHandler(enabled = screen != Screen.HOME) {
        screen = Screen.HOME
        query = ""
        selectedApp = null
    }

    MaterialTheme(colorScheme = darkColorScheme(
        background = Background, surface = Background, primary = Foreground,
        onPrimary = Background, onSurface = Foreground
    )) {
        Surface(modifier = Modifier.fillMaxSize(), color = Background) {
            when (screen) {
                Screen.HOME -> ReferenceHome(
                    apps = referenceApps,
                    homeRequestGeneration = homeRequestGeneration,
                    isDefaultHome = isDefaultHome,
                    loading = loading,
                    loadError = loadError,
                    onRetry = onRetry,
                    onLaunch = launchAndReturnHome,
                    onAppActions = { selectedApp = it },
                    onAllApps = { screen = Screen.ALL_APPS; query = "" },
                    onSettings = { screen = Screen.SETTINGS },
                    onMakeDefaultHome = onMakeDefaultHome
                )
                Screen.ALL_APPS -> {
                    val gridState = rememberLazyGridState()
                    LaunchedEffect(query, preferences.appSort, preferences.drawerColumns) {
                        gridState.scrollToItem(0)
                    }
                    Column(
                        modifier = Modifier.fillMaxSize()
                            .windowInsetsPadding(WindowInsets.safeDrawing)
                            .background(Color.Black)
                            .padding(horizontal = 20.dp, vertical = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().pointerInput(Unit) {
                                var dy = 0f
                                detectVerticalDragGestures(
                                    onDragStart = { dy = 0f },
                                    onVerticalDrag = { change, distance ->
                                        dy += distance
                                        change.consume()
                                    },
                                    onDragEnd = {
                                        if (dy > 80.dp.toPx()) { screen = Screen.HOME; query = "" }
                                        dy = 0f
                                    },
                                    onDragCancel = { dy = 0f }
                                )
                            },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            TextButton(onClick = { screen = Screen.HOME; query = "" }) {
                                Text("←", color = Foreground, fontSize = 23.sp)
                            }
                            Text(stringResource(R.string.drawer_title), fontSize = 21.sp, fontWeight = FontWeight.SemiBold)
                            TextButton(onClick = { screen = Screen.SETTINGS }) {
                                Text(stringResource(R.string.settings), color = Muted, fontSize = 12.sp)
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(stringResource(R.string.apps_count, visibleApps.size),
                                color = Muted, fontSize = 11.sp)
                            SortPicker(preferences.appSort) {
                                onPreferencesChange(preferences.copy(appSort = it))
                            }
                        }
                        if (loadError) {
                            TextButton(onClick = onRetry) {
                                Text(stringResource(R.string.catalog_stale_retry), color = Foreground)
                            }
                        }
                        when {
                            loading && apps.isEmpty() -> {
                                Text(stringResource(R.string.loading), color = Muted)
                                Spacer(Modifier.weight(1f))
                            }
                            loadError && apps.isEmpty() -> {
                                Text(stringResource(R.string.catalog_failed), color = Muted)
                                TextButton(onClick = onRetry) {
                                    Text(stringResource(R.string.retry))
                                }
                                Spacer(Modifier.weight(1f))
                            }
                            visibleApps.isEmpty() -> {
                                Text(stringResource(R.string.no_results), color = Muted)
                                Spacer(Modifier.weight(1f))
                            }
                            else -> LazyVerticalGrid(
                                state = gridState,
                                columns = GridCells.Fixed(preferences.drawerColumns),
                                modifier = Modifier.fillMaxWidth().weight(1f),
                                contentPadding = PaddingValues(vertical = 14.dp),
                                horizontalArrangement = Arrangement.spacedBy(7.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(visibleApps, key = { it.id }) { app ->
                                    DrawerAppTile(app, { launchAndReturnHome(app) }, { selectedApp = app })
                                }
                            }
                        }
                        if (preferences.showDrawerSearch) {
                            TextField(
                                value = query,
                                onValueChange = { query = it },
                                placeholder = { Text(stringResource(R.string.drawer_search_placeholder)) },
                                singleLine = true,
                                shape = RoundedCornerShape(26.dp),
                                modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp),
                                colors = TextFieldDefaults.colors(
                                    focusedTextColor = Foreground,
                                    unfocusedTextColor = Foreground,
                                    focusedContainerColor = Panel,
                                    unfocusedContainerColor = Panel,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
                                )
                            )
                        }
                    }
                }
                Screen.SETTINGS -> Column(
                    modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing)
                        .verticalScroll(rememberScrollState())
                        .padding(22.dp)
                ) {
                    Text(stringResource(R.string.settings), fontSize = 29.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(10.dp))
                    Text(stringResource(R.string.reference_only_description), color = Muted, fontSize = 13.sp)
                    Spacer(Modifier.height(25.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(stringResource(R.string.drawer_search_toggle), modifier = Modifier.weight(1f))
                        Switch(checked = preferences.showDrawerSearch,
                            onCheckedChange = { onPreferencesChange(preferences.copy(showDrawerSearch = it)) })
                    }
                    Spacer(Modifier.height(20.dp))
                    Text(stringResource(R.string.drawer_columns), color = Muted)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        (3..6).forEach { columns ->
                            if (preferences.drawerColumns == columns) {
                                Button(onClick = { onPreferencesChange(preferences.copy(drawerColumns = columns)) }) {
                                    Text("$columns")
                                }
                            } else {
                                OutlinedButton(onClick = { onPreferencesChange(preferences.copy(drawerColumns = columns)) }) {
                                    Text("$columns")
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(18.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(stringResource(R.string.default_sort), color = Muted)
                        SortPicker(preferences.appSort) {
                            onPreferencesChange(preferences.copy(appSort = it))
                        }
                    }
                    Text(stringResource(R.string.tracked_launches_only), fontSize = 12.sp, color = Muted)
                    Spacer(Modifier.height(22.dp))
                    OutlinedButton(onClick = { screen = Screen.HIDDEN_APPS }, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.manage_hidden_apps, hiddenApps.size))
                    }
                    Spacer(Modifier.height(10.dp))
                    OutlinedButton(onClick = onClearLaunchHistory, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.clear_launches))
                    }
                    Spacer(Modifier.height(14.dp))
                    if (!isDefaultHome) {
                        OutlinedButton(onClick = onMakeDefaultHome, modifier = Modifier.fillMaxWidth()) {
                            Text(stringResource(R.string.make_default))
                        }
                    }
                    Spacer(Modifier.height(17.dp))
                    Text(stringResource(R.string.reference_legacy_data_note),
                        fontSize = 11.sp, color = Muted)
                    Spacer(Modifier.height(20.dp))
                    OutlinedButton(onClick = { screen = Screen.HOME }, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.back_home))
                    }
                }
                Screen.HIDDEN_APPS -> Column(
                    modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing)
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    Text(stringResource(R.string.hidden_apps_title),
                        fontSize = 24.sp, fontWeight = FontWeight.SemiBold)
                    Text(stringResource(R.string.hidden_apps_description),
                        color = Muted, fontSize = 12.sp)
                    Spacer(Modifier.height(14.dp))
                    val byId = remember(apps) { apps.associateBy { it.id } }
                    val hiddenInstalled = remember(hiddenApps, byId) {
                        hiddenApps.mapNotNull(byId::get).sortedBy { it.label }
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
                                    Text(app.label, color = Foreground, modifier = Modifier.weight(1f),
                                        maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    TextButton(onClick = { onUnhideApp(app) }) {
                                        Text(stringResource(R.string.unhide_app))
                                    }
                                }
                            }
                        }
                    }
                    OutlinedButton(onClick = { screen = Screen.SETTINGS }, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.settings))
                    }
                }
            }
        }

        selectedApp?.let { app ->
            ModalBottomSheet(onDismissRequest = { selectedApp = null }, containerColor = Panel) {
                Column(Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, bottom = 28.dp)) {
                    Text(app.label, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                    Text(app.packageName, color = Muted, fontSize = 12.sp)
                    Spacer(Modifier.height(16.dp))
                    TextButton(onClick = { launchAndReturnHome(app) }) {
                        Text(stringResource(R.string.open_app))
                    }
                    TextButton(onClick = { selectedApp = null; onAppInfo(app) }) {
                        Text(stringResource(R.string.app_info))
                    }
                    TextButton(onClick = { selectedApp = null; onHideApp(app) }) {
                        Text(stringResource(R.string.hide_app))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DrawerAppTile(app: LaunchableApp, onLaunch: () -> Unit, onActions: () -> Unit) {
    val bitmap = rememberAppIcon(app)
    Column(
        modifier = Modifier.fillMaxWidth().combinedClickable(
            onClickLabel = stringResource(R.string.open_app),
            onLongClickLabel = stringResource(R.string.app_actions),
            onClick = onLaunch,
            onLongClick = onActions
        ).padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(Modifier.size(51.dp), contentAlignment = Alignment.Center) {
            if (bitmap != null) {
                Image(bitmap.asImageBitmap(), contentDescription = app.label, modifier = Modifier.size(46.dp))
            } else {
                Text(app.label.take(1), fontSize = 24.sp, color = Foreground)
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(app.label, color = Foreground, fontSize = 10.sp,
            maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center)
    }
}

@Composable
private fun SortPicker(selected: AppSort, onChange: (AppSort) -> Unit) {
    var open by remember { mutableStateOf(false) }
    Box {
        TextButton(onClick = { open = true }) {
            Text(stringResource(sortLabel(selected)), fontSize = 12.sp)
            Text("  ▾", fontSize = 12.sp, color = Muted)
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            AppSort.entries.forEach { choice ->
                DropdownMenuItem(
                    text = { Text(stringResource(sortLabel(choice))) },
                    onClick = { open = false; if (selected != choice) onChange(choice) }
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

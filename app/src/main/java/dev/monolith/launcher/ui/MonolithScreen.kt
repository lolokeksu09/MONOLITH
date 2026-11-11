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
import androidx.compose.foundation.layout.width
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
import androidx.compose.material3.Icon
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.monolith.launcher.R
import dev.monolith.launcher.data.AppIconStyle
import dev.monolith.launcher.data.AppSort
import dev.monolith.launcher.data.LaunchableApp
import dev.monolith.launcher.data.LaunchStats
import dev.monolith.launcher.data.LauncherPreferences
import dev.monolith.launcher.data.chooseReferenceApps
import dev.monolith.launcher.data.filterApps
import dev.monolith.launcher.data.filterHiddenApps
import dev.monolith.launcher.data.effectiveDrawerQuery
import dev.monolith.launcher.data.sortApps
import dev.monolith.launcher.data.ORBIT_CAPACITY
import dev.monolith.launcher.data.moveOrbitFavorite
import dev.monolith.launcher.data.toggleOrbitFavorite
import kotlinx.coroutines.delay

private val Background = Color(0xFF050505)
private val Foreground = Color(0xFFF4F3F0)
private val Muted = Color(0xFFA7A6A4)
private val Panel = Color(0xFF101010)

private enum class Screen { HOME, ALL_APPS, SETTINGS, ORBIT_APPS, HIDDEN_APPS }

/** Reference HOME remains the only desktop. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonolithScreen(
    apps: List<LaunchableApp>,
    preferences: LauncherPreferences,
    homeRequestGeneration: Int,
    launchStats: Map<String, LaunchStats>,
    hiddenApps: Set<String>,
    orbitFavorites: List<String>,
    loading: Boolean,
    loadError: Boolean,
    isDefaultHome: Boolean,
    onRetry: () -> Unit,
    onMakeDefaultHome: () -> Unit,
    onLaunch: (LaunchableApp) -> Unit,
    onAppInfo: (LaunchableApp) -> Unit,
    onOrbitFavoritesChange: (List<String>) -> Unit,
    onPreferencesChange: (LauncherPreferences) -> Unit,
    onHideApp: (LaunchableApp) -> Unit,
    onUnhideApp: (LaunchableApp) -> Unit,
    onClearLaunchHistory: () -> Unit
) {
    var screen by rememberSaveable { mutableStateOf(Screen.HOME) }
    var query by rememberSaveable { mutableStateOf("") }
    var debouncedQuery by rememberSaveable { mutableStateOf("") }
    var orbitQuery by rememberSaveable { mutableStateOf("") }
    var selectedApp by remember { mutableStateOf<LaunchableApp?>(null) }
    val visibleApps = remember(apps, hiddenApps, debouncedQuery, preferences, launchStats) {
        sortApps(
            filterApps(filterHiddenApps(apps, hiddenApps),
                effectiveDrawerQuery(debouncedQuery, preferences.showDrawerSearch)),
            preferences.appSort, launchStats
        )
    }
    val referenceApps = remember(
        apps, hiddenApps, launchStats, orbitFavorites, preferences.orbitDensity
    ) {
        chooseReferenceApps(
            apps = apps,
            hiddenIds = hiddenApps,
            stats = launchStats,
            limit = preferences.orbitDensity,
            favoriteIds = orbitFavorites
        )
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
    // Only nonblank input is debounced. Clearing search must restore the full
    // catalog immediately; sorting still runs on the UI thread after settling.
    LaunchedEffect(query) {
        if (query.isBlank()) {
            debouncedQuery = ""
        } else {
            delay(180)
            debouncedQuery = query
        }
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
                    iconStyle = preferences.appIconStyle,
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
                    LaunchedEffect(debouncedQuery, preferences.appSort, preferences.drawerColumns) {
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
                            TextButton(
                                onClick = { screen = Screen.HOME; query = "" },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    painterResource(R.drawable.ic_arrow_back),
                                    contentDescription = stringResource(R.string.back_home),
                                    tint = Foreground
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("MONOLITH / 03", color = Muted, fontSize = 9.sp,
                                    letterSpacing = 1.5.sp)
                                Text(stringResource(R.string.drawer_title), fontSize = 20.sp,
                                    fontWeight = FontWeight.Medium, letterSpacing = .2.sp)
                            }
                            TextButton(
                                onClick = { screen = Screen.SETTINGS },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    painterResource(R.drawable.ic_settings),
                                    contentDescription = stringResource(R.string.settings),
                                    tint = Muted
                                )
                            }
                        }
                        Spacer(Modifier.height(9.dp))
                        if (preferences.showDrawerSearch) {
                            TextField(
                                value = query,
                                onValueChange = { query = it },
                                label = { Text(stringResource(R.string.drawer_search_label)) },
                                placeholder = { Text(stringResource(R.string.drawer_search_placeholder)) },
                                leadingIcon = {
                                    Icon(painterResource(R.drawable.ic_search),
                                        contentDescription = null, modifier = Modifier.size(20.dp))
                                },
                                trailingIcon = if (query.isEmpty()) null else ({
                                    TextButton(onClick = { query = "" }, modifier = Modifier.size(48.dp)) {
                                        Icon(
                                            painterResource(R.drawable.ic_close),
                                            contentDescription = stringResource(R.string.drawer_clear_search),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
                                colors = TextFieldDefaults.colors(
                                    focusedTextColor = Foreground,
                                    unfocusedTextColor = Foreground,
                                    focusedLabelColor = Foreground,
                                    unfocusedLabelColor = Muted,
                                    focusedContainerColor = Panel,
                                    unfocusedContainerColor = Panel,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
                                )
                            )
                        }
                        Spacer(Modifier.height(9.dp))
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
                                    DrawerAppTile(
                                        app = app,
                                        iconStyle = preferences.appIconStyle,
                                        onLaunch = { launchAndReturnHome(app) },
                                        onActions = { selectedApp = app }
                                    )
                                }
                            }
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
                    Spacer(Modifier.height(22.dp))
                    Text(stringResource(R.string.appearance_title), color = Muted, fontSize = 12.sp)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        AppIconStyle.entries.forEach { style ->
                            ChoiceButton(
                                modifier = Modifier.weight(1f),
                                selected = preferences.appIconStyle == style,
                                label = stringResource(iconStyleLabel(style)),
                                onClick = {
                                    onPreferencesChange(preferences.copy(appIconStyle = style))
                                }
                            )
                        }
                    }
                    Text(stringResource(R.string.appearance_hint), fontSize = 11.sp,
                        lineHeight = 15.sp, color = Muted)
                    Spacer(Modifier.height(20.dp))
                    Text(stringResource(R.string.orbit_density), color = Muted, fontSize = 12.sp)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(8, 12, 15).forEach { density ->
                            ChoiceButton(
                                modifier = Modifier.weight(1f),
                                selected = preferences.orbitDensity == density,
                                label = "$density",
                                onClick = {
                                    onPreferencesChange(preferences.copy(orbitDensity = density))
                                }
                            )
                        }
                    }
                    Text(stringResource(R.string.orbit_density_hint), fontSize = 11.sp,
                        lineHeight = 15.sp, color = Muted)
                    Spacer(Modifier.height(20.dp))
                    OutlinedButton(onClick = { orbitQuery = ""; screen = Screen.ORBIT_APPS },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp)) {
                        Text(stringResource(R.string.reference_customize_orbit))
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(stringResource(R.string.reference_customize_hint),
                        fontSize = 12.sp, color = Muted)
                    Spacer(Modifier.height(20.dp))
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
                            ChoiceButton(
                                modifier = Modifier.weight(1f),
                                selected = preferences.drawerColumns == columns,
                                label = "$columns",
                                onClick = {
                                    onPreferencesChange(preferences.copy(drawerColumns = columns))
                                }
                            )
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
                Screen.ORBIT_APPS -> {
                    val available = remember(apps, hiddenApps) {
                        filterHiddenApps(apps, hiddenApps).sortedBy { it.label.lowercase() }
                    }
                    val byId = remember(available) { available.associateBy { it.id } }
                    val listed = remember(available, byId, orbitFavorites) {
                        orbitFavorites.mapNotNull(byId::get) + available.filterNot { it.id in orbitFavorites }
                    }
                    val filtered = remember(listed, orbitQuery) { filterApps(listed, orbitQuery) }
                    Column(
                        modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing)
                            .padding(horizontal = 20.dp, vertical = 14.dp)
                    ) {
                        Text(stringResource(R.string.reference_customize_orbit),
                            fontSize = 26.sp, fontWeight = FontWeight.SemiBold)
                        Text(stringResource(R.string.reference_selected_count, orbitFavorites.size, ORBIT_CAPACITY),
                            color = Muted, fontSize = 13.sp)
                        Spacer(Modifier.height(14.dp))
                        TextField(
                            value = orbitQuery,
                            onValueChange = { orbitQuery = it },
                            label = { Text(stringResource(R.string.drawer_search_label)) },
                            placeholder = { Text(stringResource(R.string.drawer_search_placeholder)) },
                            leadingIcon = {
                                Icon(painterResource(R.drawable.ic_search), contentDescription = null)
                            },
                            singleLine = true, shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Panel, unfocusedContainerColor = Panel,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            )
                        )
                        Spacer(Modifier.height(9.dp))
                        LazyColumn(Modifier.weight(1f)) {
                            items(filtered, key = { it.id }) { app ->
                                val selected = app.id in orbitFavorites
                                Row(
                                    Modifier.fillMaxWidth().heightIn(min = 56.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(app.label, modifier = Modifier.weight(1f),
                                        color = Foreground, fontSize = 13.sp,
                                        maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    if (selected && orbitQuery.isBlank()) {
                                        TextButton(
                                            onClick = { onOrbitFavoritesChange(moveOrbitFavorite(orbitFavorites, app.id, -1)) },
                                            enabled = orbitFavorites.indexOf(app.id) > 0
                                        ) { Text("↑", fontSize = 17.sp) }
                                        TextButton(
                                            onClick = { onOrbitFavoritesChange(moveOrbitFavorite(orbitFavorites, app.id, 1)) },
                                            enabled = orbitFavorites.indexOf(app.id) < orbitFavorites.lastIndex
                                        ) { Text("↓", fontSize = 17.sp) }
                                    }
                                    TextButton(
                                        enabled = selected || orbitFavorites.size < ORBIT_CAPACITY,
                                        onClick = { onOrbitFavoritesChange(toggleOrbitFavorite(orbitFavorites, app.id)) }
                                    ) {
                                        Text(stringResource(if(selected) R.string.reference_remove else R.string.reference_add))
                                    }
                                }
                            }
                        }
                        OutlinedButton(
                            modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp),
                            onClick = { screen = Screen.SETTINGS }
                        ) { Text(stringResource(R.string.reference_done)) }
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

@Composable
private fun ChoiceButton(
    modifier: Modifier = Modifier,
    selected: Boolean,
    label: String,
    onClick: () -> Unit
) {
    val buttonModifier = modifier.heightIn(min = 48.dp).semantics { this.selected = selected }
    val contentPadding = PaddingValues(horizontal = 4.dp)
    if (selected) {
        Button(onClick = onClick, modifier = buttonModifier, contentPadding = contentPadding) {
            Text(label, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    } else {
        OutlinedButton(onClick = onClick, modifier = buttonModifier, contentPadding = contentPadding) {
            Text(label, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DrawerAppTile(
    app: LaunchableApp,
    iconStyle: AppIconStyle,
    onLaunch: () -> Unit,
    onActions: () -> Unit
) {
    val bitmap = rememberAppIcon(app, iconStyle)
    Column(
        modifier = Modifier.fillMaxWidth().heightIn(min = 88.dp).combinedClickable(
            onClickLabel = stringResource(R.string.open_app),
            onLongClickLabel = stringResource(R.string.app_actions),
            onClick = onLaunch,
            onLongClick = onActions
        ).padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(Modifier.size(51.dp), contentAlignment = Alignment.Center) {
            if (bitmap != null) {
                Image(
                    bitmap.asImageBitmap(),
                    contentDescription = app.label,
                    modifier = Modifier.size(42.dp)
                )
            } else {
                Text(app.label.take(1), fontSize = 24.sp, color = Foreground)
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            app.label,
            color = Foreground,
            fontSize = 12.sp,
            lineHeight = 14.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.heightIn(min = 28.dp)
        )
    }
}

@Composable
private fun SortPicker(selected: AppSort, onChange: (AppSort) -> Unit) {
    var open by remember { mutableStateOf(false) }
    Box {
        TextButton(onClick = { open = true }) {
            Icon(
                painterResource(R.drawable.ic_sort),
                contentDescription = null,
                tint = Muted,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(7.dp))
            Text(stringResource(sortLabel(selected)), fontSize = 12.sp, maxLines = 1)
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

private fun iconStyleLabel(value: AppIconStyle): Int = when (value) {
    AppIconStyle.ORIGINAL -> R.string.icon_original
    AppIconStyle.MONO -> R.string.icon_mono
    AppIconStyle.INK -> R.string.icon_ink
}

private fun sortLabel(value: AppSort): Int = when (value) {
    AppSort.A_Z -> R.string.sort_az
    AppSort.Z_A -> R.string.sort_za
    AppSort.FREQUENT -> R.string.sort_frequent
    AppSort.LAST_OPENED -> R.string.sort_last_opened
    AppSort.INSTALLED_NEWEST -> R.string.sort_new_install
    AppSort.INSTALLED_OLDEST -> R.string.sort_old_install
}

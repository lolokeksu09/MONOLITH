package dev.monolith.launcher.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.foundation.layout.imePadding
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
import androidx.compose.runtime.produceState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.monolith.launcher.R
import dev.monolith.launcher.data.AppIconStyle
import dev.monolith.launcher.data.DrawerSearchPosition
import dev.monolith.launcher.data.DrawerProjectionRequest
import dev.monolith.launcher.data.DrawerProjectionResult
import dev.monolith.launcher.data.projectDrawerApps
import dev.monolith.launcher.data.matchingDrawerApps
import dev.monolith.launcher.data.AppSort
import dev.monolith.launcher.data.LaunchableApp
import dev.monolith.launcher.data.LaunchStats
import dev.monolith.launcher.data.LauncherPreferences
import dev.monolith.launcher.data.chooseReferenceApps
import dev.monolith.launcher.data.filterApps
import dev.monolith.launcher.data.filterHiddenApps
import dev.monolith.launcher.data.ORBIT_CAPACITY
import dev.monolith.launcher.data.moveOrbitFavorite
import dev.monolith.launcher.data.toggleOrbitFavorite
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

private val Background = Color.Black
private val Foreground = Color(0xFFF4F3F0)
private val Muted = Color(0xFFB8B6B1)
private val Panel = Color(0xFF161615)
private val Outline = Color(0xFF383835)

internal enum class Screen { HOME, ALL_APPS, SETTINGS, ORBIT_APPS, HIDDEN_APPS }

/** Return to the parent settings screen instead of unexpectedly jumping HOME. */
internal fun destinationOnBack(screen: Screen, settingsOrigin: Screen): Screen = when (screen) {
    Screen.ORBIT_APPS, Screen.HIDDEN_APPS -> Screen.SETTINGS
    Screen.SETTINGS -> if (settingsOrigin == Screen.ALL_APPS) Screen.ALL_APPS else Screen.HOME
    Screen.ALL_APPS -> Screen.HOME
    Screen.HOME -> Screen.HOME
}

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
    var settingsOrigin by rememberSaveable { mutableStateOf(Screen.HOME) }
    var query by rememberSaveable { mutableStateOf("") }
    var debouncedQuery by rememberSaveable { mutableStateOf("") }
    var orbitQuery by rememberSaveable { mutableStateOf("") }
    var orbitPinnedOnly by rememberSaveable { mutableStateOf(false) }
    var selectedApp by remember { mutableStateOf<LaunchableApp?>(null) }
    // Unicode collation and full-catalog sorting happen away from the UI thread.
    // The retained result from a previous effect is never displayed when the
    // request changes (especially after an app becomes hidden).
    val locale = Locale.getDefault()
    val drawerRequest = remember(
        apps, hiddenApps, debouncedQuery, preferences.showDrawerSearch,
        preferences.appSort, launchStats, locale
    ) {
        DrawerProjectionRequest(
            apps = apps,
            hiddenIds = hiddenApps,
            settledQuery = debouncedQuery,
            searchEnabled = preferences.showDrawerSearch,
            sort = preferences.appSort,
            stats = launchStats,
            locale = locale
        )
    }
    val drawerResult by produceState<DrawerProjectionResult?>(null, drawerRequest) {
        val matched = withContext(Dispatchers.Default) {
            projectDrawerApps(drawerRequest)
        }
        value = DrawerProjectionResult(drawerRequest, matched)
    }
    val visibleApps = matchingDrawerApps(drawerRequest, drawerResult)
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
    // A visible action sheet owns Back until it is dismissed.
    BackHandler(enabled = screen != Screen.HOME && selectedApp == null) {
        screen = destinationOnBack(screen, settingsOrigin)
        if (screen == Screen.HOME) query = ""
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
                    onSettings = { settingsOrigin = Screen.HOME; screen = Screen.SETTINGS },
                    onMakeDefaultHome = onMakeDefaultHome
                )
                Screen.ALL_APPS -> {
                    val gridState = rememberLazyGridState()
                    LaunchedEffect(drawerRequest, preferences.drawerColumns) {
                        gridState.scrollToItem(0)
                    }
                    Column(
                        modifier = Modifier.fillMaxSize()
                            .background(Color.Black)
                            .windowInsetsPadding(WindowInsets.safeDrawing)
                            .imePadding()
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
                            Column(
                                modifier = Modifier.weight(1f).padding(start = 10.dp),
                                horizontalAlignment = Alignment.Start
                            ) {
                                Text(
                                    "MONOLITH", color = Muted, fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium, letterSpacing = 2.sp
                                )
                                Text(
                                    stringResource(R.string.drawer_title),
                                    fontSize = 32.sp, fontWeight = FontWeight.SemiBold,
                                    letterSpacing = (-.8).sp, lineHeight = 38.sp
                                )
                            }
                            TextButton(
                                onClick = {
                                    settingsOrigin = Screen.ALL_APPS
                                    screen = Screen.SETTINGS
                                },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    painterResource(R.drawable.ic_settings),
                                    contentDescription = stringResource(R.string.settings),
                                    tint = Muted
                                )
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                        Box(Modifier.fillMaxWidth().height(1.dp).background(Outline))
                        Spacer(Modifier.height(17.dp))
                        val bottomSearch = preferences.drawerSearchPosition ==
                            DrawerSearchPosition.BOTTOM
                        if (preferences.showDrawerSearch && !bottomSearch) {
                            DrawerSearchField(query = query, onQueryChange = { query = it })
                        }
                        Spacer(Modifier.height(9.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                if (visibleApps == null) stringResource(R.string.drawer_updating)
                                else stringResource(R.string.apps_count, visibleApps.size),
                                color = Muted, fontSize = 12.sp
                            )
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
                            visibleApps == null -> {
                                // A result is pending. Never show an old filtered list
                                // or misreport a still-processing query as no results.
                                Spacer(Modifier.weight(1f))
                            }
                            visibleApps.isEmpty() -> {
                                Text(stringResource(R.string.no_results), color = Muted)
                                if (preferences.showDrawerSearch && query.isNotBlank()) {
                                    TextButton(
                                        onClick = { query = "" },
                                        modifier = Modifier.heightIn(min = 48.dp)
                                    ) {
                                        Text(stringResource(R.string.drawer_clear_search),
                                            color = Foreground)
                                    }
                                }
                                Spacer(Modifier.weight(1f))
                            }
                            else -> LazyVerticalGrid(
                                state = gridState,
                                columns = GridCells.Fixed(preferences.drawerColumns),
                                modifier = Modifier.fillMaxWidth().weight(1f),
                                contentPadding = PaddingValues(top = 19.dp, bottom = 20.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(19.dp)
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
                        if (preferences.showDrawerSearch && bottomSearch) {
                            Spacer(Modifier.height(9.dp))
                            DrawerSearchField(query = query, onQueryChange = { query = it })
                        }
                    }
                }
                Screen.SETTINGS -> Column(
                    modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing)
                        .verticalScroll(rememberScrollState())
                        .padding(22.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextButton(
                            onClick = { screen = destinationOnBack(Screen.SETTINGS, settingsOrigin) },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                painterResource(R.drawable.ic_arrow_back),
                                contentDescription = stringResource(R.string.settings_back),
                                tint = Foreground,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(Modifier.width(3.dp))
                        Text(
                            stringResource(R.string.settings),
                            fontSize = 34.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = (-1).sp
                        )
                    }
                    Spacer(Modifier.height(24.dp))
                    // The controls explain themselves; the previous tutorial
                    // paragraph created noise on every visit to Settings.
                    SettingsSectionHeading(stringResource(R.string.settings_section_appearance))
                    Text(stringResource(R.string.appearance_title), color = Muted, fontSize = 13.sp)
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
                    Spacer(Modifier.height(10.dp))
                    Text(stringResource(R.string.appearance_hint), fontSize = 12.sp,
                        lineHeight = 17.sp, color = Muted)
                    Spacer(Modifier.height(24.dp))
                    SettingsSectionHeading(stringResource(R.string.settings_section_orbit))
                    Text(stringResource(R.string.orbit_density), color = Muted, fontSize = 13.sp)
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
                    Spacer(Modifier.height(10.dp))
                    Text(stringResource(R.string.orbit_density_hint), fontSize = 12.sp,
                        lineHeight = 17.sp, color = Muted)
                    Spacer(Modifier.height(20.dp))
                    SettingsAction(
                        title = stringResource(R.string.reference_customize_orbit),
                        onClick = { orbitQuery = ""; screen = Screen.ORBIT_APPS }
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(stringResource(R.string.reference_customize_hint),
                        fontSize = 12.sp, lineHeight = 17.sp, color = Muted)
                    Spacer(Modifier.height(24.dp))
                    SettingsSectionHeading(stringResource(R.string.settings_section_catalog))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(stringResource(R.string.drawer_search_toggle), modifier = Modifier.weight(1f))
                        Switch(checked = preferences.showDrawerSearch,
                            onCheckedChange = { onPreferencesChange(preferences.copy(showDrawerSearch = it)) })
                    }
                    if (preferences.showDrawerSearch) {
                        Spacer(Modifier.height(12.dp))
                        Text(stringResource(R.string.drawer_search_position),
                            color = Muted, fontSize = 12.sp)
                        Spacer(Modifier.height(7.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            DrawerSearchPosition.entries.forEach { position ->
                                ChoiceButton(
                                    modifier = Modifier.weight(1f),
                                    selected = preferences.drawerSearchPosition == position,
                                    label = stringResource(
                                        if (position == DrawerSearchPosition.TOP)
                                            R.string.drawer_search_top
                                        else R.string.drawer_search_bottom
                                    ),
                                    onClick = {
                                        onPreferencesChange(
                                            preferences.copy(drawerSearchPosition = position)
                                        )
                                    }
                                )
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                        Text(stringResource(R.string.drawer_search_position_hint),
                            color = Muted, fontSize = 11.sp, lineHeight = 15.sp)
                    }
                    Spacer(Modifier.height(20.dp))
                    Text(stringResource(R.string.drawer_columns), color = Muted, fontSize = 13.sp)
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
                    Text(stringResource(R.string.tracked_launches_only),
                        fontSize = 12.sp, lineHeight = 17.sp, color = Muted)
                    Spacer(Modifier.height(24.dp))
                    SettingsSectionHeading(stringResource(R.string.settings_section_system))
                    SettingsAction(
                        title = stringResource(R.string.manage_hidden_apps, hiddenApps.size),
                        onClick = { screen = Screen.HIDDEN_APPS }
                    )
                    Spacer(Modifier.height(10.dp))
                    SettingsAction(
                        title = stringResource(R.string.clear_launches),
                        onClick = onClearLaunchHistory
                    )
                    Spacer(Modifier.height(14.dp))
                    if (!isDefaultHome) {
                        SettingsAction(
                            title = stringResource(R.string.make_default),
                            onClick = onMakeDefaultHome
                        )
                    }
                    Spacer(Modifier.height(17.dp))
                    Text(stringResource(R.string.reference_legacy_data_note),
                        fontSize = 11.sp, color = Muted)
                    Spacer(Modifier.height(20.dp))
                    SettingsAction(
                        title = stringResource(R.string.back_home),
                        onClick = { screen = Screen.HOME }
                    )
                }
                Screen.ORBIT_APPS -> {
                    val available = remember(apps, hiddenApps) {
                        filterHiddenApps(apps, hiddenApps).sortedBy { it.label.lowercase() }
                    }
                    val byId = remember(available) { available.associateBy { it.id } }
                    val listed = remember(available, byId, orbitFavorites) {
                        orbitFavorites.mapNotNull(byId::get) + available.filterNot { it.id in orbitFavorites }
                    }
                    val filtered = remember(listed, orbitQuery, orbitPinnedOnly, orbitFavorites) {
                        val candidates = if (orbitPinnedOnly) {
                            listed.filter { it.id in orbitFavorites }
                        } else {
                            listed
                        }
                        filterApps(candidates, orbitQuery)
                    }
                    Column(
                        modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing)
                            .padding(horizontal = 20.dp, vertical = 14.dp)
                    ) {
                        Text(stringResource(R.string.reference_customize_orbit),
                            fontSize = 26.sp, fontWeight = FontWeight.SemiBold)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                stringResource(
                                    R.string.reference_selected_count, orbitFavorites.size, ORBIT_CAPACITY
                                ),
                                color = Muted, fontSize = 13.sp
                            )
                            TextButton(
                                enabled = orbitFavorites.isNotEmpty() || orbitPinnedOnly,
                                onClick = { orbitPinnedOnly = !orbitPinnedOnly }
                            ) {
                                Text(
                                    stringResource(
                                        if (orbitPinnedOnly) R.string.reference_show_all_apps
                                        else R.string.reference_pinned_only
                                    ),
                                    fontSize = 12.sp, maxLines = 1
                                )
                            }
                        }
                        Spacer(Modifier.height(12.dp))
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
                            if (filtered.isEmpty()) {
                                item {
                                    Text(
                                        stringResource(
                                            if (orbitPinnedOnly && orbitFavorites.isEmpty())
                                                R.string.reference_no_pinned_apps
                                            else R.string.no_results
                                        ),
                                        color = Muted, fontSize = 13.sp,
                                        modifier = Modifier.padding(vertical = 16.dp)
                                    )
                                }
                            }
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
                    val pinnedToOrbit = app.id in orbitFavorites
                    TextButton(
                        enabled = pinnedToOrbit || orbitFavorites.size < ORBIT_CAPACITY,
                        onClick = {
                            onOrbitFavoritesChange(toggleOrbitFavorite(orbitFavorites, app.id))
                        }
                    ) {
                        Text(
                            stringResource(
                                if (pinnedToOrbit) R.string.reference_unpin_orbit
                                else R.string.reference_pin_orbit
                            )
                        )
                    }
                    Text(
                        stringResource(
                            if (!pinnedToOrbit && orbitFavorites.size == ORBIT_CAPACITY)
                                R.string.reference_orbit_full
                            else R.string.reference_selected_count,
                            orbitFavorites.size, ORBIT_CAPACITY
                        ),
                        modifier = Modifier.padding(start = 12.dp),
                        color = Muted, fontSize = 11.sp
                    )
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
private fun DrawerSearchField(
    query: String,
    onQueryChange: (String) -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    TextField(
        value = query,
        onValueChange = onQueryChange,
        label = { Text(stringResource(R.string.drawer_search_label)) },
        placeholder = { Text(stringResource(R.string.drawer_search_placeholder)) },
        leadingIcon = {
            Icon(painterResource(R.drawable.ic_search),
                contentDescription = null, modifier = Modifier.size(20.dp))
        },
        trailingIcon = if (query.isEmpty()) null else ({
            TextButton(onClick = { onQueryChange("") }, modifier = Modifier.size(48.dp)) {
                Icon(
                    painterResource(R.drawable.ic_close),
                    contentDescription = stringResource(R.string.drawer_clear_search),
                    modifier = Modifier.size(18.dp)
                )
            }
        }),
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { keyboardController?.hide() }),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().heightIn(min = 58.dp),
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

@Composable
private fun SettingsSectionHeading(title: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            title, color = Foreground, fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold, letterSpacing = (-.15).sp
        )
        Spacer(Modifier.width(16.dp))
        Box(Modifier.weight(1f).height(1.dp).background(Outline))
    }
}

@Composable
private fun SettingsAction(title: String, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, Outline),
        colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
            containerColor = Panel, contentColor = Foreground
        ),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(title, fontSize = 13.sp, fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center)
    }
}

@Composable
private fun ChoiceButton(
    modifier: Modifier = Modifier,
    selected: Boolean,
    label: String,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(14.dp)
    Box(
        modifier = modifier.heightIn(min = 50.dp)
            .clip(shape)
            .background(if (selected) Foreground else Panel)
            .border(1.dp, if (selected) Foreground else Outline, shape)
            .clickable(onClick = onClick)
            .semantics { this.selected = selected },
        contentAlignment = Alignment.Center
    ) {
        Text(
            label, color = if (selected) Background else Foreground,
            fontSize = 13.sp, fontWeight = FontWeight.Medium,
            maxLines = 1, overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
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
        modifier = Modifier.fillMaxWidth().heightIn(min = 96.dp).combinedClickable(
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
                    contentDescription = null,
                    modifier = Modifier.size(44.dp)
                )
            } else {
                Text(app.label.take(1), fontSize = 24.sp, color = Foreground)
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            app.label,
            color = Foreground,
            fontSize = 13.sp,
            lineHeight = 16.sp,
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
        TextButton(
            onClick = { open = true },
            modifier = Modifier.border(.7.dp, Outline, RoundedCornerShape(13.dp))
                .heightIn(min = 46.dp)
        ) {
            Icon(
                painterResource(R.drawable.ic_sort),
                contentDescription = null,
                tint = Muted,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(7.dp))
            Text(stringResource(sortLabel(selected)), fontSize = 12.sp, maxLines = 1)
        }
        DropdownMenu(
            expanded = open, onDismissRequest = { open = false },
            containerColor = Panel
        ) {
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

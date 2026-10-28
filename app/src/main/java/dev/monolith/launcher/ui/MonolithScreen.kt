package dev.monolith.launcher.ui

import android.content.ComponentName
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import dev.monolith.launcher.data.filterApps
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.delay

private val Background = Color(0xFF050505)
private val Panel = Color(0xFF171717)
private val Muted = Color(0xFFA7A7A7)
private val MainText = Color(0xFFF5F5F5)

@Composable
fun MonolithScreen(
    apps: List<LaunchableApp>,
    pinnedIds: List<String>,
    loading: Boolean,
    isDefaultHome: Boolean,
    onMakeDefaultHome: () -> Unit,
    onLaunch: (LaunchableApp) -> Unit,
    onTogglePin: (LaunchableApp) -> Unit
) {
    var drawer by rememberSaveable { mutableStateOf(false) }
    var query by rememberSaveable { mutableStateOf("") }
    BackHandler(enabled = drawer) { drawer = false; query = "" }
    val pinned = remember(apps, pinnedIds) {
        pinnedIds.mapNotNull { id -> apps.firstOrNull { it.id == id } }
    }
    val visibleApps = remember(apps, query) { filterApps(apps, query) }

    MaterialTheme(
        colorScheme = darkColorScheme(
            background = Background,
            surface = Background,
            primary = MainText,
            onPrimary = Background,
            onSurface = MainText
        )
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = Background) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Header()
                Spacer(Modifier.height(30.dp))
                if (drawer) {
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
                    Text(stringResource(R.string.pin_hint), fontSize = 12.sp, color = Muted)
                    Spacer(Modifier.height(12.dp))
                    if (loading && apps.isEmpty()) {
                        Text(stringResource(R.string.loading), color = Muted)
                        Spacer(Modifier.weight(1f))
                    } else if (visibleApps.isEmpty()) {
                        Text(stringResource(R.string.no_results), color = Muted)
                        Spacer(Modifier.weight(1f))
                    } else {
                        AppGrid(visibleApps, pinnedIds, onLaunch, onTogglePin, Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(10.dp))
                    MonolithButton(stringResource(R.string.back_home)) {
                        drawer = false
                        query = ""
                    }
                } else {
                    Clock()
                    Spacer(Modifier.height(34.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(stringResource(R.string.pinned_apps), color = Muted, fontSize = 12.sp, letterSpacing = 2.sp)
                        Text("${pinned.size}", color = Muted, fontSize = 12.sp)
                    }
                    Spacer(Modifier.height(12.dp))
                    if (loading && apps.isEmpty()) {
                        Text(stringResource(R.string.loading), color = Muted)
                    } else if (pinned.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Panel, RoundedCornerShape(20.dp))
                                .padding(24.dp)
                        ) {
                            Text(stringResource(R.string.empty_pinned), color = Muted, fontSize = 14.sp)
                        }
                    } else {
                        AppGrid(pinned, pinnedIds, onLaunch, onTogglePin, Modifier.weight(1f))
                    }
                    if (pinned.isEmpty()) Spacer(Modifier.weight(1f))
                    if (!isDefaultHome) {
                        OutlinedButton(onClick = onMakeDefaultHome, modifier = Modifier.fillMaxWidth()) {
                            Text(stringResource(R.string.make_default))
                        }
                        Spacer(Modifier.height(10.dp))
                    }
                    MonolithButton(stringResource(R.string.all_apps)) { drawer = true }
                }
            }
        }
    }
}

@Composable
private fun Header() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("MONOLITH", fontSize = 17.sp, letterSpacing = 3.sp, fontWeight = FontWeight.Bold)
        Text("0.1 / ALPHA", color = Muted, fontSize = 11.sp)
    }
}

@Composable
private fun Clock() {
    var now by remember { mutableStateOf(LocalDateTime.now()) }
    val locale = Locale.getDefault()
    LaunchedEffect(Unit) {
        while (true) {
            delay(30_000L)
            now = LocalDateTime.now()
        }
    }
    Text(now.format(DateTimeFormatter.ofPattern("HH:mm", locale)), fontSize = 68.sp, fontWeight = FontWeight.Light)
    Text(
        now.format(DateTimeFormatter.ofPattern("EEEE, d MMMM", locale)),
        color = Muted,
        fontSize = 14.sp
    )
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
    onLaunch: (LaunchableApp) -> Unit,
    onTogglePin: (LaunchableApp) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 76.dp),
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(9.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
        contentPadding = PaddingValues(vertical = 12.dp)
    ) {
        items(apps, key = { it.id }) { app ->
            AppTile(app, app.id in pinnedIds, { onLaunch(app) }, { onTogglePin(app) })
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AppTile(
    app: LaunchableApp,
    pinned: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val pm = LocalContext.current.packageManager
    val openLabel = stringResource(R.string.open_app)
    val toggleLabel = stringResource(if (pinned) R.string.unpin_app else R.string.pin_app)
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
                onLongClickLabel = toggleLabel,
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(3.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(Modifier.size(56.dp), contentAlignment = Alignment.Center) {
            if (painter != null) {
                Image(painter, contentDescription = null, modifier = Modifier.size(52.dp))
            } else {
                Text(app.label.take(1), fontSize = 25.sp)
            }
        }
        Spacer(Modifier.height(7.dp))
        Text(
            app.label,
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
        if (pinned) Text("•", color = Muted, fontSize = 12.sp)
    }
}

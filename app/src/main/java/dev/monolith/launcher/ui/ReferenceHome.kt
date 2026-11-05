package dev.monolith.launcher.ui

import android.content.Context
import android.media.AudioManager
import android.view.KeyEvent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.monolith.launcher.R
import dev.monolith.launcher.data.LaunchableApp
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs
import kotlinx.coroutines.delay

private val ReferenceWhite = Color(0xFFF2F1EF)
private val ReferenceGray = Color(0xFFAAAAAB)

/**
 * Two real launcher pages from the visual reference, not a video or a static screenshot.
 * The stock HOME role, Android app drawer, widgets and settings remain available.
 */
@Composable
fun ReferenceHome(
    apps: List<LaunchableApp>,
    homeRequestGeneration: Int,
    isDefaultHome: Boolean,
    loading: Boolean,
    loadError: Boolean,
    onRetry: () -> Unit,
    onLaunch: (LaunchableApp) -> Unit,
    onAppActions: (LaunchableApp) -> Unit,
    onAllApps: () -> Unit,
    onSettings: () -> Unit,
    onWidgets: () -> Unit,
    onMakeDefaultHome: () -> Unit
) {
    var page by rememberSaveable { mutableIntStateOf(0) }
    var orbitVisible by rememberSaveable { mutableStateOf(false) }
    var now by remember { mutableStateOf(LocalDateTime.now()) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        while (true) {
            now = LocalDateTime.now()
            delay(15_000L)
        }
    }
    LaunchedEffect(homeRequestGeneration) {
        if (homeRequestGeneration > 0) {
            page = 0
            orbitVisible = false
        }
    }
    BackHandler(enabled = page != 0 || orbitVisible) {
        if (orbitVisible) orbitVisible = false else page = 0
    }
    val openApp: (LaunchableApp) -> Unit = { app ->
        orbitVisible = false
        page = 0
        onLaunch(app)
    }
    val openDrawer: () -> Unit = {
        orbitVisible = false
        onAllApps()
    }

    Box(
        modifier = Modifier.fillMaxSize().background(Color(0xFF050505))
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .pointerInput(page) {
                var delta = 0f
                detectHorizontalDragGestures(
                    onDragStart = { delta = 0f },
                    onHorizontalDrag = { change, distance ->
                        delta += distance
                        change.consume()
                    },
                    onDragEnd = {
                        if (abs(delta) > 72.dp.toPx()) {
                            page = if (delta < 0f) 1 else 0
                            orbitVisible = false
                        }
                        delta = 0f
                    },
                    onDragCancel = { delta = 0f }
                )
            }
    ) {
        Crossfade(targetState = page, animationSpec = tween(320), label = "Reference home pages") { selected ->
            if (selected == 0) {
                ReferenceMinimalPage(
                    now = now,
                    apps = apps,
                    orbitVisible = orbitVisible,
                    onToggleOrbit = { orbitVisible = !orbitVisible },
                    onLaunch = openApp,
                    onAppActions = onAppActions,
                    onNextTrack = { sendMediaKey(context, KeyEvent.KEYCODE_MEDIA_NEXT) },
                    onPlayPause = { sendMediaKey(context, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) }
                )
            } else {
                ReferenceEditorialPage(
                    now = now,
                    apps = apps,
                    onLaunch = openApp,
                    onAppActions = onAppActions,
                    onWidgets = onWidgets
                )
            }
        }

        TextButton(
            onClick = onSettings,
            modifier = Modifier.align(Alignment.TopEnd).padding(top = 13.dp, end = 13.dp)
        ) {
            Text(stringResource(R.string.reference_settings), color = ReferenceGray,
                fontSize = 10.sp, letterSpacing = 1.4.sp)
        }

        Column(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, bottom = 11.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (loadError) {
                TextButton(onClick = onRetry) {
                    Text(stringResource(R.string.catalog_stale_retry), color = ReferenceWhite, fontSize = 11.sp)
                }
            } else if (loading && apps.isEmpty()) {
                Text(stringResource(R.string.loading), fontSize = 11.sp, color = ReferenceGray)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = { orbitVisible = false; page = 1 - page }) {
                    Text(stringResource(if (page == 0) R.string.reference_editorial else R.string.reference_minimal),
                        color = ReferenceGray, fontSize = 10.sp, letterSpacing = 1.3.sp)
                }
                TextButton(
                    onClick = openDrawer,
                    modifier = Modifier.pointerInput(Unit) {
                        var travel = 0f
                        detectVerticalDragGestures(
                            onDragStart = { travel = 0f },
                            onVerticalDrag = { change, amount -> travel += amount; change.consume() },
                            onDragEnd = { if (travel < -32.dp.toPx()) openDrawer() }
                        )
                    }
                ) {
                    Text(stringResource(R.string.reference_all_apps), color = ReferenceWhite,
                        fontSize = 11.sp, letterSpacing = 1.4.sp)
                }
            }
            if (!isDefaultHome) {
                TextButton(onClick = onMakeDefaultHome) {
                    Text(stringResource(R.string.make_default), color = ReferenceGray, fontSize = 11.sp)
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ReferenceMinimalPage(
    now: LocalDateTime,
    apps: List<LaunchableApp>,
    orbitVisible: Boolean,
    onToggleOrbit: () -> Unit,
    onLaunch: (LaunchableApp) -> Unit,
    onAppActions: (LaunchableApp) -> Unit,
    onNextTrack: () -> Unit,
    onPlayPause: () -> Unit
) {
    Box(Modifier.fillMaxSize().background(Color(0xFF050505))) {
        // The whole empty middle is an accessible trigger for the compact icon cluster.
        Box(
            modifier = Modifier.fillMaxSize().combinedClickable(
                onClick = onToggleOrbit, onLongClick = { if (!orbitVisible) onToggleOrbit() },
                onClickLabel = stringResource(R.string.reference_orbit_toggle),
                onLongClickLabel = stringResource(R.string.reference_orbit_toggle)
            )
        )
        Column(
            modifier = Modifier.align(Alignment.TopStart).padding(start = 28.dp, top = 35.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text("▰  " + now.format(DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())),
                color = ReferenceWhite, fontSize = 9.sp, letterSpacing = 1.sp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.width(132.dp)) {
                    Text(stringResource(R.string.reference_media), color = ReferenceWhite,
                        fontSize = 10.sp, maxLines = 1)
                    Text(stringResource(R.string.reference_media_hint), color = ReferenceGray, fontSize = 8.sp)
                }
                TextButton(onClick = onPlayPause) {
                    Text("Ⅱ / ▶", color = ReferenceWhite, fontSize = 12.sp)
                }
                TextButton(onClick = onNextTrack) {
                    Text("››", color = ReferenceWhite, fontSize = 17.sp)
                }
            }
        }
        Text(
            now.format(DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())),
            modifier = Modifier.align(Alignment.CenterStart)
                .offset(x = (-21).dp, y = 128.dp).rotate(-90f),
            color = ReferenceWhite, fontSize = 15.sp, fontWeight = FontWeight.Medium,
            letterSpacing = 1.sp
        )
        Text(
            stringResource(R.string.reference_hold_hint),
            modifier = Modifier.align(Alignment.CenterStart)
                .offset(x = (-43).dp, y = (-80).dp).rotate(-90f),
            color = ReferenceGray, fontSize = 9.sp, letterSpacing = 1.3.sp
        )
        Box(
            modifier = Modifier.align(Alignment.Center).size(250.dp),
            contentAlignment = Alignment.Center
        ) {
            ReferenceOrbit(apps, orbitVisible, onLaunch, onAppActions)
            if (!orbitVisible) {
                Text(
                    "○",
                    modifier = Modifier.clickable(onClick = onToggleOrbit).padding(17.dp),
                    fontSize = 28.sp, color = Color(0xFF4A4A4A)
                )
            }
        }
        Text(
            stringResource(R.string.reference_swipe_hint),
            modifier = Modifier.align(Alignment.BottomStart).padding(start = 28.dp, bottom = 106.dp),
            color = ReferenceGray, fontSize = 10.sp, letterSpacing = 1.sp
        )
    }
}

private val orbitOffsets = listOf(
    -22 to -94, 24 to -94,
    -68 to -51, -22 to -51, 24 to -51, 70 to -51,
    -68 to -8, -22 to -8, 24 to -8, 70 to -8,
    -22 to 35, 24 to 35, 70 to 35,
    -22 to 78, 24 to 78
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ReferenceOrbit(
    apps: List<LaunchableApp>,
    expanded: Boolean,
    onLaunch: (LaunchableApp) -> Unit,
    onActions: (LaunchableApp) -> Unit
) {
    val progress by animateFloatAsState(if (expanded) 1f else 0f, animationSpec = tween(280), label = "Orbit spread")
    apps.take(orbitOffsets.size).forEachIndexed { index, app ->
        val (dx, dy) = orbitOffsets[index]
        val bitmap = rememberAppIcon(app)
        val label = app.label
        Box(
            modifier = Modifier.offset(x = (dx * progress).dp, y = (dy * progress).dp)
                .size(41.dp)
                .graphicsLayer { alpha = progress; scaleX = 0.65f + 0.35f * progress; scaleY = scaleX }
                .background(Color(0xFF161616), CircleShape)
                .semantics { contentDescription = label }
                .combinedClickable(onClick = { if (expanded) onLaunch(app) },
                    onLongClick = { if (expanded) onActions(app) }),
            contentAlignment = Alignment.Center
        ) {
            if (bitmap != null) {
                Image(bitmap.asImageBitmap(), contentDescription = null, modifier = Modifier.size(34.dp))
            } else {
                Text(app.label.take(1), fontSize = 17.sp, color = ReferenceWhite)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ReferenceEditorialPage(
    now: LocalDateTime,
    apps: List<LaunchableApp>,
    onLaunch: (LaunchableApp) -> Unit,
    onAppActions: (LaunchableApp) -> Unit,
    onWidgets: () -> Unit
) {
    val dayName = now.format(DateTimeFormatter.ofPattern("EEE", Locale.ENGLISH)).uppercase(Locale.ENGLISH)
    val weekday = now.format(DateTimeFormatter.ofPattern("EEEE", Locale.ENGLISH)).uppercase(Locale.ENGLISH)
    val date = now.format(DateTimeFormatter.ofPattern("dd / MM / yyyy", Locale.getDefault()))
    Box(Modifier.fillMaxSize().background(Color(0xFF070707))) {
        ReferenceMountainArtwork()
        Column(
            modifier = Modifier.align(Alignment.TopStart).padding(start = 28.dp, top = 48.dp)
        ) {
            Text("—  " + weekday + "  /  " + date,
                color = ReferenceWhite, fontSize = 10.sp, letterSpacing = 1.sp)
            Spacer(Modifier.height(74.dp))
            Text(dayName, color = ReferenceWhite, fontSize = 66.sp,
                fontWeight = FontWeight.Black, letterSpacing = (-3).sp)
            Text("TIME   /   " + now.format(DateTimeFormatter.ofPattern("HH:mm")),
                color = ReferenceGray, fontSize = 10.sp, letterSpacing = 2.sp)
            Spacer(Modifier.height(37.dp))
            Text(stringResource(R.string.reference_quick_apps),
                color = ReferenceGray, fontSize = 10.sp, letterSpacing = 1.8.sp)
            Spacer(Modifier.height(9.dp))
            apps.take(8).chunked(4).forEach { rowApps ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    rowApps.forEach { app ->
                        ReferenceQuickApp(app, onLaunch, onAppActions)
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
            TextButton(onClick = onWidgets) {
                Text(stringResource(R.string.reference_widgets), fontSize = 10.sp,
                    color = ReferenceWhite, letterSpacing = 1.2.sp)
            }
        }
        Text(
            stringResource(
                when (now.hour) {
                    in 5..11 -> R.string.reference_good_morning
                    in 12..17 -> R.string.reference_good_afternoon
                    else -> R.string.reference_good_evening
                }
            ),
            modifier = Modifier.align(Alignment.BottomStart)
                .padding(start = 28.dp, bottom = 109.dp),
            color = ReferenceWhite, fontSize = 10.sp, letterSpacing = 1.5.sp
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ReferenceQuickApp(
    app: LaunchableApp,
    onLaunch: (LaunchableApp) -> Unit,
    onActions: (LaunchableApp) -> Unit
) {
    val bitmap = rememberAppIcon(app)
    Column(
        modifier = Modifier.width(46.dp).combinedClickable(
            onClick = { onLaunch(app) },
            onLongClick = { onActions(app) }
        ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(40.dp)
                .background(Color(0x66090909), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (bitmap != null) {
                Image(bitmap.asImageBitmap(), contentDescription = app.label,
                    modifier = Modifier.size(26.dp))
            } else {
                Text(app.label.take(1), fontSize = 19.sp, color = ReferenceWhite)
            }
        }
        Text(app.label, color = ReferenceWhite, fontSize = 8.sp, maxLines = 1,
            textAlign = TextAlign.Center, overflow = TextOverflow.Ellipsis)
    }
}

/** Original vector cliff artwork; no third-party photo or prebuilt theme files. */
@Composable
private fun ReferenceMountainArtwork() {
    Canvas(Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val ridge = Path().apply {
            moveTo(0.61f * w, 0.18f * h)
            lineTo(0.79f * w, 0.10f * h)
            lineTo(0.93f * w, 0.20f * h)
            lineTo(w, 0.16f * h)
            lineTo(w, h)
            lineTo(0.41f * w, h)
            lineTo(0.48f * w, 0.67f * h)
            lineTo(0.56f * w, 0.48f * h)
            close()
        }
        drawPath(
            ridge,
            brush = Brush.linearGradient(
                0f to Color(0xFF121314),
                0.55f to Color(0xFF545558),
                1f to Color(0xFFA1A1A4),
                start = Offset(w * 0.66f, h * 0.15f),
                end = Offset(w, h)
            )
        )
        for (i in 0 until 48) {
            val fraction = i / 48f
            val x = w * (0.57f + fraction * 0.45f)
            val y = h * (0.28f + fraction * 0.52f)
            val endX = (x + w * (0.035f + fraction * 0.065f)).coerceAtMost(w)
            drawLine(
                color = Color.White.copy(alpha = 0.08f + 0.22f * fraction),
                start = Offset(x, y),
                end = Offset(endX, (y + h * (0.06f + fraction * 0.09f)).coerceAtMost(h)),
                strokeWidth = if (i % 5 == 0) 3.2f else 1.1f
            )
        }
        val crack = Path().apply {
            moveTo(0.82f * w, 0.25f * h)
            lineTo(0.71f * w, 0.43f * h)
            lineTo(0.91f * w, 0.66f * h)
            lineTo(0.68f * w, h)
        }
        drawPath(crack, Color(0xFFDDDDDE).copy(alpha = 0.52f), style = Stroke(width = 2.4f))
    }
}

private fun sendMediaKey(context: Context, keyCode: Int) {
    val audio = context.getSystemService(AudioManager::class.java) ?: return
    // Dispatch controls to the active player; metadata access would require a separate opt-in.
    runCatching {
        audio.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, keyCode))
        audio.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_UP, keyCode))
    }
}

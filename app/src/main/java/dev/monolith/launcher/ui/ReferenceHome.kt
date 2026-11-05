package dev.monolith.launcher.ui

import android.animation.ValueAnimator
import android.content.Context
import android.media.AudioManager
import android.view.KeyEvent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import dev.monolith.launcher.R
import dev.monolith.launcher.data.LaunchableApp
import dev.monolith.launcher.data.orbitHitTargetsEnabled
import dev.monolith.launcher.data.orbitPositions
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs
import kotlinx.coroutines.delay

private val Ink = Color.Black
private val Paper = Color(0xFFF4F3F0)
private val Soft = Color(0xFFA7A6A4)
private val Dim = Color(0xFF555557)
private val Hint = Color(0xFF767678)
private val Accent = Color(0xFFC9C5BB)

/**
 * A two-scene, native Android launcher, inspired by the supplied iThemeDesign video.
 * The black landing screen and the editorial scene intentionally have different
 * visual density. All shortcuts refer to installed, visible applications.
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
    onMakeDefaultHome: () -> Unit
) {
    var page by rememberSaveable { mutableIntStateOf(0) }
    var orbitOpen by rememberSaveable { mutableStateOf(false) }
    var now by remember { mutableStateOf(LocalDateTime.now()) }
    val context = LocalContext.current
    // Re-read after resume/recomposition so system reduced-motion changes are respected.
    val motionDuration = if (ValueAnimator.areAnimatorsEnabled()) 420 else 0

    LaunchedEffect(Unit) {
        while (true) {
            val current = LocalDateTime.now()
            now = current
            val millisIntoMinute = current.second * 1000L + current.nano / 1_000_000L
            delay((60_000L - millisIntoMinute).coerceIn(250L, 60_000L))
        }
    }
    LaunchedEffect(homeRequestGeneration) {
        if (homeRequestGeneration > 0) {
            page = 0
            orbitOpen = false
        }
    }
    BackHandler(enabled = page != 0 || orbitOpen) {
        if (orbitOpen) orbitOpen = false else page = 0
    }

    val openApp: (LaunchableApp) -> Unit = { app ->
        orbitOpen = false
        page = 0
        onLaunch(app)
    }
    val openDrawer: () -> Unit = {
        orbitOpen = false
        onAllApps()
    }

    Box(
        Modifier.fillMaxSize().background(Ink)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .pointerInput(page, orbitOpen) {
                var dx = 0f
                var dy = 0f
                detectDragGestures(
                    onDragStart = { dx = 0f; dy = 0f },
                    onDrag = { change, drag ->
                        dx += drag.x
                        dy += drag.y
                        change.consume()
                    },
                    onDragEnd = {
                        val horizontal = abs(dx) > abs(dy) * 1.25f && abs(dx) > 70.dp.toPx()
                        if (horizontal) {
                            page = if (dx < 0f) 1 else 0
                            orbitOpen = false
                        } else if (dy < -74.dp.toPx() && abs(dy) > abs(dx)) {
                            openDrawer()
                        }
                    },
                    onDragCancel = { dx = 0f; dy = 0f }
                )
            }
    ) {
        AnimatedContent(
            targetState = page,
            modifier = Modifier.fillMaxSize(),
            transitionSpec = {
                val direction = if (targetState > initialState) 1 else -1
                (slideInHorizontally(
                    animationSpec = if (motionDuration == 0) tween(0) else spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = 180f
                    ),
                    initialOffsetX = { direction * it / 10 }
                ) + fadeIn(tween(motionDuration * 3 / 4, easing = FastOutSlowInEasing))) togetherWith
                    (slideOutHorizontally(
                        animationSpec = if (motionDuration == 0) tween(0) else spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = 180f
                        ),
                        targetOffsetX = { -direction * it / 10 }
                    ) + fadeOut(tween(motionDuration * 3 / 4, easing = FastOutSlowInEasing)))
            },
            label = "Reference scenes"
        ) { selected ->
            if (selected == 0) {
                MinimalScene(
                    now = now,
                    apps = apps,
                    orbitOpen = orbitOpen,
                    motionDuration = motionDuration,
                    onToggleOrbit = { orbitOpen = !orbitOpen },
                    onLaunch = openApp,
                    onActions = onAppActions,
                    onPlayPause = { sendMediaKey(context, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) },
                    onNext = { sendMediaKey(context, KeyEvent.KEYCODE_MEDIA_NEXT) }
                )
            } else {
                EditorialScene(now, apps, openApp, onAppActions)
            }
        }

        TextButton(
            onClick = onSettings,
            modifier = Modifier.align(Alignment.TopEnd).padding(top = 20.dp, end = 17.dp)
                .size(width = 92.dp, height = 48.dp)
        ) {
            Text(stringResource(R.string.reference_settings), color = Soft,
                fontSize = 10.sp, fontWeight = FontWeight.Normal)
        }

        Column(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, bottom = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (loadError) {
                TextButton(onClick = onRetry) {
                    Text(stringResource(R.string.catalog_stale_retry),
                        color = Paper, fontSize = 12.sp)
                }
            } else if (loading && apps.isEmpty()) {
                Text(stringResource(R.string.loading), color = Soft, fontSize = 12.sp)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = { orbitOpen = false; page = 1-page }) {
                    Crossfade(
                        targetState = page,
                        animationSpec = tween(
                            motionDuration * 3 / 4,
                            easing = FastOutSlowInEasing
                        ),
                        label = "Reference page indicator"
                    ) { currentPage ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(if (currentPage == 0) "●  ○" else "○  ●",
                                color = Soft, fontSize = 12.sp)
                            Spacer(Modifier.width(9.dp))
                            Text(stringResource(if (currentPage == 0) R.string.reference_editorial
                                                else R.string.reference_minimal),
                                color = Soft, fontSize = 10.sp)
                        }
                    }
                }
                TextButton(onClick = openDrawer) {
                    Text(stringResource(R.string.reference_all_apps), color = Soft,
                        fontSize = 11.sp, fontWeight = FontWeight.Medium)
                }
            }
            if (!isDefaultHome) {
                TextButton(onClick = onMakeDefaultHome) {
                    Text(stringResource(R.string.make_default), color = Accent, fontSize = 12.sp)
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MinimalScene(
    now: LocalDateTime,
    apps: List<LaunchableApp>,
    orbitOpen: Boolean,
    motionDuration: Int,
    onToggleOrbit: () -> Unit,
    onLaunch: (LaunchableApp) -> Unit,
    onActions: (LaunchableApp) -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit
) {
    // One shared, velocity-preserving spring avoids a spec switch between open and close.
    val orbitProgress by animateFloatAsState(
        targetValue = if (orbitOpen) 1f else 0f,
        animationSpec = if (motionDuration == 0) tween(0) else spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = 180f
        ),
        label = "Orbit expansion"
    )
    val orbitStateDescription = stringResource(
        if (orbitOpen) R.string.reference_orbit_expanded
        else R.string.reference_orbit_collapsed
    )

    Box(Modifier.fillMaxSize().background(Ink)) {
        Column(
            modifier = Modifier.align(Alignment.TopStart).fillMaxWidth()
                .padding(start = 29.dp, top = 40.dp, end = 24.dp)
        ) {
            Text("●   MONOLITH    /    01",
                color = Soft, fontSize = 10.sp, letterSpacing = 1.3.sp)
            Spacer(Modifier.height(23.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f).padding(end = 8.dp)) {
                    Text(stringResource(R.string.reference_media), color = Paper,
                        fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(
                        stringResource(R.string.reference_media_hint),
                        color = Soft,
                        fontSize = 10.sp,
                        lineHeight = 12.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                MediaControl(0, stringResource(R.string.reference_play_pause), onPlayPause)
                MediaControl(1, stringResource(R.string.reference_next_track), onNext)
            }
        }

        VerticalRailText(
            text = now.format(DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())) +
                "   /   " + now.format(DateTimeFormatter.ofPattern("EEE", Locale.ENGLISH)).uppercase(Locale.ENGLISH),
            modifier = Modifier.align(Alignment.CenterStart)
                .offset(y = 128.dp).padding(start = 16.dp),
            fontSize = 15.sp,
            color = Paper,
            letterSpacing = 1.sp
        )
        VerticalRailText(
            text = stringResource(R.string.reference_hold_hint),
            modifier = Modifier.align(Alignment.CenterStart)
                .offset(y = (-105).dp).padding(start = 18.dp),
            color = Hint,
            fontSize = 10.sp,
            letterSpacing = 1.1.sp
        )

        BoxWithConstraints(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Keep 48dp hit targets spaced >=52dp even on compact phones.
            val orbitScale = minOf(1f, (maxWidth.value - 28f) / 295f).coerceAtLeast(.92f)
            Box(
                modifier = Modifier.size(304.dp)
                    .semantics { stateDescription = orbitStateDescription }
                    .combinedClickable(
                        onClick = onToggleOrbit,
                        onLongClick = { if (!orbitOpen) onToggleOrbit() },
                        onClickLabel = stringResource(R.string.reference_orbit_toggle),
                        onLongClickLabel = stringResource(R.string.reference_orbit_toggle)
                    ),
                contentAlignment = Alignment.Center
            ) {
                OrbitCluster(apps, orbitOpen, orbitScale, orbitProgress, onLaunch, onActions)
                Canvas(
                    Modifier.size(36.dp).graphicsLayer {
                        alpha = (1f - orbitProgress * 1.6f).coerceIn(0f, 1f)
                        scaleX = 1f + orbitProgress * .18f
                        scaleY = scaleX
                    }
                ) {
                    drawCircle(Dim.copy(alpha = .72f),
                        radius = size.minDimension * .27f, style = Stroke(width = 1.dp.toPx()))
                    drawCircle(Soft.copy(alpha = .85f), radius = 2.dp.toPx())
                }
            }
            AnimatedVisibility(
                visible = orbitOpen,
                modifier = Modifier.align(Alignment.Center).offset(y = 176.dp),
                enter = fadeIn(tween(
                    if (motionDuration == 0) 0 else 220,
                    easing = FastOutSlowInEasing
                )),
                exit = fadeOut(tween(
                    if (motionDuration == 0) 0 else 180,
                    easing = FastOutSlowInEasing
                ))
            ) {
                Text(
                    stringResource(R.string.reference_close_orbit),
                    modifier = Modifier.clickable(onClick = onToggleOrbit).padding(14.dp),
                    color = Soft, fontSize = 11.sp, letterSpacing = 1.sp
                )
            }
        }

        Text(
            stringResource(R.string.reference_swipe_hint),
            modifier = Modifier.align(Alignment.BottomStart)
                .padding(start = 30.dp, bottom = 102.dp),
            fontSize = 11.sp, color = Hint, letterSpacing = .7.sp
        )
    }
}

/**
 * Measures the rotated bounds instead of pushing unrotated text beyond the screen edge.
 * This keeps every glyph inside the safe area at different font scales and locales.
 */
@Composable
private fun VerticalRailText(
    text: String,
    modifier: Modifier,
    color: Color,
    fontSize: TextUnit,
    letterSpacing: TextUnit
) {
    Layout(
        content = {
            Text(
                text = text,
                color = color,
                fontSize = fontSize,
                letterSpacing = letterSpacing,
                maxLines = 1,
                softWrap = false
            )
        },
        modifier = modifier
    ) { measurables, _ ->
        val placeable = measurables.single().measure(Constraints())
        val rotatedWidth = placeable.height
        val rotatedHeight = placeable.width
        layout(rotatedWidth, rotatedHeight) {
            placeable.placeWithLayer(
                x = (rotatedWidth - placeable.width) / 2,
                y = (rotatedHeight - placeable.height) / 2
            ) {
                rotationZ = -90f
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun OrbitCluster(
    apps: List<LaunchableApp>,
    expanded: Boolean,
    orbitScale: Float,
    progress: Float,
    onLaunch: (LaunchableApp) -> Unit,
    onActions: (LaunchableApp) -> Unit
) {
    val positions = remember(apps.size) { orbitPositions(apps.size) }
    // Warm all visible orbit icons before the first tap, without doing package I/O on the UI thread.
    val icons = positions.mapIndexed { index, _ -> rememberAppIcon(apps[index]) }
    val local = progress.coerceIn(0f, 1f)
    if (expanded || local > .001f) positions.forEachIndexed { index, point ->
        val app = apps[index]
        val bitmap = icons[index]
        Box(
            modifier = Modifier
                .offset(x = (point.x * orbitScale * local).dp,
                        y = (point.y * orbitScale * local).dp)
                .zIndex(index.toFloat())
                .size(48.dp)
                .graphicsLayer {
                    alpha = (local * 1.35f).coerceIn(0f, 1f)
                    scaleX = .80f + .20f * local
                    scaleY = scaleX
                }
                .semantics { contentDescription = app.label }
                .combinedClickable(
                    enabled = orbitHitTargetsEnabled(expanded, local),
                    onClick = { onLaunch(app) },
                    onLongClick = { onActions(app) },
                    onClickLabel = stringResource(R.string.open_app),
                    onLongClickLabel = stringResource(R.string.app_actions)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (bitmap != null) {
                Image(bitmap.asImageBitmap(), contentDescription = null,
                    modifier = Modifier.size(34.dp))
            } else {
                Text(app.label.take(1), fontSize = 18.sp, color = Paper)
            }
        }
    }
}

@Composable
private fun MediaControl(kind: Int, description: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier.size(48.dp).semantics { contentDescription = description }
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.size(19.dp)) {
            val w = size.width
            val h = size.height
            if (kind == 0) {
                drawLine(Paper, androidx.compose.ui.geometry.Offset(w*.3f,h*.20f),
                    androidx.compose.ui.geometry.Offset(w*.3f,h*.80f),
                    strokeWidth = 2.dp.toPx(), cap = StrokeCap.Round)
                drawLine(Paper, androidx.compose.ui.geometry.Offset(w*.7f,h*.20f),
                    androidx.compose.ui.geometry.Offset(w*.7f,h*.80f),
                    strokeWidth = 2.dp.toPx(), cap = StrokeCap.Round)
            } else {
                val triangle = Path().apply {
                    moveTo(w*.18f,h*.18f); lineTo(w*.72f,h*.5f)
                    lineTo(w*.18f,h*.82f); close()
                }
                drawPath(triangle, Paper)
                drawLine(Paper, androidx.compose.ui.geometry.Offset(w*.82f,h*.18f),
                    androidx.compose.ui.geometry.Offset(w*.82f,h*.82f),
                    strokeWidth = 2.dp.toPx(), cap = StrokeCap.Round)
            }
        }
    }
}

@Composable
private fun EditorialScene(
    now: LocalDateTime,
    apps: List<LaunchableApp>,
    onLaunch: (LaunchableApp) -> Unit,
    onActions: (LaunchableApp) -> Unit
) {
    val dayShort = now.format(DateTimeFormatter.ofPattern("EEE", Locale.ENGLISH))
        .uppercase(Locale.ENGLISH)
    val weekday = now.format(DateTimeFormatter.ofPattern("EEEE", Locale.ENGLISH))
        .uppercase(Locale.ENGLISH)
    BoxWithConstraints(Modifier.fillMaxSize().background(Ink)) {
        val editorialWidth = maxWidth.value
        Image(
            painter = painterResource(R.drawable.editorial_rock),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(Modifier.fillMaxSize().background(
            Brush.horizontalGradient(
                0f to Ink.copy(alpha = .99f),
                .48f to Ink.copy(alpha = .68f),
                1f to Color.Transparent
            )
        ))
        Box(Modifier.fillMaxSize().background(
            Brush.verticalGradient(
                0f to Ink.copy(alpha = .14f),
                .7f to Color.Transparent,
                1f to Ink.copy(alpha = .28f)
            )
        ))

        Column(
            modifier = Modifier.align(Alignment.TopStart).padding(start = 28.dp, top = 44.dp)
        ) {
            Text("MONOLITH   /   02", color = Soft,
                fontSize = 10.sp, letterSpacing = 1.4.sp)
            Spacer(Modifier.height(26.dp))
            Text(weekday, color = Paper, fontSize = 11.sp, letterSpacing = 2.2.sp)
            Text(
                now.format(DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.getDefault())),
                color = Soft, fontSize = 11.sp
            )
            Spacer(Modifier.height(14.dp))
            Text("────────────────", color = Dim, fontSize = 9.sp)
        }

        Column(
            modifier = Modifier.align(Alignment.CenterStart)
                .offset(y = (-94).dp)
                .padding(start = 25.dp)
        ) {
            Text("Today /", color = Soft, fontSize = 14.sp,
                fontWeight = FontWeight.Light)
            Text(
                dayShort,
                color = Paper,
                fontSize = minOf(96f, editorialWidth * .27f).sp,
                fontWeight = FontWeight.Black,
                letterSpacing = (-4).sp,
                maxLines = 1
            )
            Text("—  " + now.format(DateTimeFormatter.ofPattern("HH:mm")),
                fontSize = 11.sp, color = Soft, letterSpacing = 1.9.sp)
        }

        Column(
            modifier = Modifier.align(Alignment.BottomStart)
                .padding(start = 28.dp, bottom = 114.dp)
        ) {
            Text(stringResource(R.string.reference_quick_apps), fontSize = 10.sp,
                color = Soft, letterSpacing = 1.8.sp)
            Spacer(Modifier.height(13.dp))
            apps.take(8).chunked(4).forEach { rowApps ->
                Row(horizontalArrangement = Arrangement.spacedBy(11.dp)) {
                    rowApps.forEach { app -> EditorialShortcut(app, onLaunch, onActions) }
                }
                Spacer(Modifier.height(9.dp))
            }
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(when(now.hour) {
                    in 5..11 -> R.string.reference_good_morning
                    in 12..17 -> R.string.reference_good_afternoon
                    else -> R.string.reference_good_evening
                }),
                color = Paper, fontSize = 11.sp, letterSpacing = 1.5.sp
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun EditorialShortcut(
    app: LaunchableApp,
    onLaunch: (LaunchableApp) -> Unit,
    onActions: (LaunchableApp) -> Unit
) {
    val bitmap = rememberAppIcon(app)
    val mono = remember { ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) }) }
    Box(
        modifier = Modifier.size(48.dp)
            .semantics { contentDescription = app.label }
            .combinedClickable(
                onClick = { onLaunch(app) },
                onLongClick = { onActions(app) },
                onClickLabel = stringResource(R.string.open_app),
                onLongClickLabel = stringResource(R.string.app_actions)
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier.size(40.dp)
                .border(.6.dp, Paper.copy(alpha = .20f), CircleShape)
                .background(Ink.copy(alpha = .22f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (bitmap != null) {
                Image(bitmap.asImageBitmap(), contentDescription = null,
                    modifier = Modifier.size(23.dp), colorFilter = mono)
            } else {
                Text(app.label.take(1), color = Paper, fontSize = 14.sp,
                    fontWeight = FontWeight.Medium)
            }
        }
    }
}

private fun sendMediaKey(context: Context, keyCode: Int) {
    val audio = context.getSystemService(AudioManager::class.java) ?: return
    runCatching {
        audio.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, keyCode))
        audio.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_UP, keyCode))
    }
}

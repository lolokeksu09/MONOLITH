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
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material3.Icon
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
import androidx.compose.ui.semantics.clearAndSetSemantics
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
import dev.monolith.launcher.data.AppIconStyle
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
private val Soft = Color(0xFFB8B6B1)
private val Dim = Color(0xFF686662)
private val Hint = Color(0xFF999792)
private val Accent = Color(0xFFE1DDD5)

@Composable
fun ReferenceHome(
    apps: List<LaunchableApp>,
    iconStyle: AppIconStyle,
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
    val pageState = stringResource(R.string.reference_page_state, page + 1, 2)

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
                    iconStyle = iconStyle,
                    orbitOpen = orbitOpen,
                    motionDuration = motionDuration,
                    onToggleOrbit = { orbitOpen = !orbitOpen },
                    onLaunch = openApp,
                    onActions = { app ->
                        orbitOpen = false
                        onAppActions(app)
                    },
                    onPlayPause = { sendMediaKey(context, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) },
                    onNext = { sendMediaKey(context, KeyEvent.KEYCODE_MEDIA_NEXT) }
                )
            } else {
                EditorialScene(now, apps, iconStyle, openApp, onAppActions, openDrawer)
            }
        }

        TextButton(
            onClick = onSettings,
            modifier = Modifier.align(Alignment.TopEnd).padding(top = 16.dp, end = 12.dp)
                .size(48.dp)
        ) {
            Icon(
                painterResource(R.drawable.ic_settings),
                contentDescription = stringResource(R.string.settings),
                tint = Soft.copy(alpha = .72f),
                modifier = Modifier.size(18.dp)
            )
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
                TextButton(
                    onClick = { orbitOpen = false; page = 1-page },
                    modifier = Modifier.semantics {
                        stateDescription = pageState
                    }
                ) {
                    Crossfade(
                        targetState = page,
                        animationSpec = tween(
                            motionDuration * 3 / 4,
                            easing = FastOutSlowInEasing
                        ),
                        label = "Reference page indicator"
                    ) { currentPage ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Purely visual dots; the announced state lives on the button.
                            Text(if (currentPage == 0) "●  ○" else "○  ●",
                                color = Soft, fontSize = 12.sp,
                                modifier = Modifier.clearAndSetSemantics { })
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
    iconStyle: AppIconStyle,
    orbitOpen: Boolean,
    motionDuration: Int,
    onToggleOrbit: () -> Unit,
    onLaunch: (LaunchableApp) -> Unit,
    onActions: (LaunchableApp) -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit
) {
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
        if (orbitOpen) {
            val dismissLabel = stringResource(R.string.reference_dismiss_orbit)
            // Full-screen dismiss layer sits BEHIND media controls and shortcuts,
            // preserving their own click actions and swipe gestures. It only
            // exists while expanded; the closed HOME stays non-interactive
            // outside the explicit 48dp center trigger.
            Box(
                modifier = Modifier.fillMaxSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClickLabel = dismissLabel,
                        onClick = onToggleOrbit
                    )
                    .semantics { contentDescription = dismissLabel }
            )
        }
        Column(
            modifier = Modifier.align(Alignment.TopStart).fillMaxWidth()
                .padding(start = 28.dp, top = 43.dp, end = 20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f).padding(end = 8.dp)) {
                    Text(stringResource(R.string.reference_media), color = Paper,
                        fontSize = 12.sp, fontWeight = FontWeight.Medium,
                        maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(
                        stringResource(R.string.reference_media_hint),
                        color = Soft,
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                MediaControl(
                    R.drawable.ic_play_arrow,
                    stringResource(R.string.reference_play_pause),
                    onPlayPause
                )
                MediaControl(
                    R.drawable.ic_skip_next,
                    stringResource(R.string.reference_next_track),
                    onNext
                )
            }
        }

        VerticalRailText(
            text = now.format(DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())) +
                "   /   " + now.format(DateTimeFormatter.ofPattern("EEE", Locale.getDefault()))
                    .uppercase(Locale.getDefault()),
            modifier = Modifier.align(Alignment.CenterStart)
                .offset(y = 128.dp).padding(start = 16.dp),
            fontSize = 16.sp,
            color = Paper,
            letterSpacing = .7.sp
        )
        VerticalRailText(
            text = stringResource(R.string.reference_hold_hint),
            modifier = Modifier.align(Alignment.CenterStart)
                .offset(y = (-105).dp).padding(start = 18.dp),
            color = Hint,
            fontSize = 11.sp,
            letterSpacing = .7.sp
        )

        BoxWithConstraints(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            val orbitScale = minOf(1f, (maxWidth.value - 28f) / 295f).coerceAtLeast(.92f)
            Box(
                modifier = Modifier.size(304.dp),
                contentAlignment = Alignment.Center
            ) {
                // The 304dp host positions icons; it is not a giant invisible
                // click target. The central button is exactly 48dp instead.
                OrbitCluster(
                    apps, iconStyle, orbitOpen, orbitScale, orbitProgress, onLaunch, onActions
                )
                // Do not block a single app at (0, 0) once the Orbit opens.
                // During collapse, wait until the center indicator is visible.
                if (!orbitOpen && orbitProgress <= .62f) {
                    Box(
                        modifier = Modifier.size(48.dp)
                            .semantics { stateDescription = orbitStateDescription }
                            .combinedClickable(
                                onClick = onToggleOrbit,
                                onLongClick = onToggleOrbit,
                                onClickLabel = stringResource(R.string.reference_orbit_toggle),
                                onLongClickLabel = stringResource(R.string.reference_orbit_toggle)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(
                            Modifier.size(42.dp).graphicsLayer {
                                alpha = (1f - orbitProgress * 1.6f).coerceIn(0f, 1f)
                                scaleX = 1f + orbitProgress * .18f
                                scaleY = scaleX
                            }
                        ) {
                            drawCircle(Dim.copy(alpha = .72f),
                                radius = size.minDimension * .28f,
                                style = Stroke(width = 1.2.dp.toPx()))
                            drawCircle(Paper.copy(alpha = .93f), radius = 2.2.dp.toPx())
                        }
                    }
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
                Row(
                    modifier = Modifier.semantics {
                        stateDescription = orbitStateDescription
                    }.clickable(onClick = onToggleOrbit)
                        .height(48.dp).padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painterResource(R.drawable.ic_close),
                        contentDescription = null,
                        tint = Soft,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        stringResource(R.string.reference_close_orbit),
                        color = Soft, fontSize = 11.sp, letterSpacing = 1.sp
                    )
                }
            }
        }

        Text(
            stringResource(R.string.reference_swipe_hint),
            modifier = Modifier.align(Alignment.BottomStart)
                .padding(start = 30.dp, bottom = 102.dp),
            fontSize = 12.sp, color = Hint, letterSpacing = .4.sp
        )
    }
}

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
    iconStyle: AppIconStyle,
    expanded: Boolean,
    orbitScale: Float,
    progress: Float,
    onLaunch: (LaunchableApp) -> Unit,
    onActions: (LaunchableApp) -> Unit
) {
    val positions = remember(apps.size) { orbitPositions(apps.size) }
    val icons = positions.mapIndexed { index, _ ->
        rememberAppIcon(apps[index], iconStyle)
    }
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
                    modifier = Modifier.size(38.dp))
            } else {
                Text(app.label.take(1), fontSize = 18.sp, color = Paper)
            }
        }
    }
}

@Composable
private fun MediaControl(iconRes: Int, description: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier.size(48.dp).semantics { contentDescription = description }
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painterResource(iconRes),
            contentDescription = null,
            tint = Paper,
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
private fun EditorialScene(
    now: LocalDateTime,
    apps: List<LaunchableApp>,
    iconStyle: AppIconStyle,
    onLaunch: (LaunchableApp) -> Unit,
    onActions: (LaunchableApp) -> Unit,
    onAllApps: () -> Unit
) {
    val locale = Locale.getDefault()
    val dayShort = now.format(DateTimeFormatter.ofPattern("EEE", locale)).uppercase(locale)
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
                .43f to Ink.copy(alpha = .76f),
                1f to Ink.copy(alpha = .05f)
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
            Text(
                now.format(DateTimeFormatter.ofPattern("dd MMMM yyyy", locale)),
                color = Soft, fontSize = 11.sp, maxLines = 2
            )
            Spacer(Modifier.height(14.dp))
            Box(Modifier.width(48.dp).height(1.dp).background(Dim))
        }

        Column(
            modifier = Modifier.align(Alignment.CenterStart)
                .offset(y = (-94).dp)
                .padding(start = 25.dp)
        ) {
            Text(
                dayShort,
                color = Paper,
                fontSize = minOf(96f, editorialWidth * .27f).sp,
                fontWeight = FontWeight.Black,
                letterSpacing = (-4).sp,
                maxLines = 1
            )
            Text("—  " + now.format(DateTimeFormatter.ofPattern("HH:mm", locale)),
                fontSize = 11.sp, color = Soft, letterSpacing = 1.9.sp)
        }

        Column(
            modifier = Modifier.align(Alignment.BottomStart)
                .padding(start = 28.dp, bottom = 114.dp)
        ) {
            Text(stringResource(R.string.reference_quick_apps), fontSize = 10.sp,
                color = Soft, letterSpacing = 1.8.sp)
            Spacer(Modifier.height(13.dp))
            if (apps.isEmpty()) {
                Text(stringResource(R.string.reference_no_quick_apps),
                    color = Soft, fontSize = 12.sp)
                TextButton(
                    onClick = onAllApps,
                    modifier = Modifier.height(48.dp)
                ) {
                    Text(stringResource(R.string.reference_browse_all_apps),
                        color = Paper, fontSize = 12.sp)
                }
            } else {
                apps.take(8).chunked(4).forEach { rowApps ->
                    Row(horizontalArrangement = Arrangement.spacedBy(11.dp)) {
                        rowApps.forEach { app ->
                            EditorialShortcut(app, iconStyle, onLaunch, onActions)
                        }
                    }
                    Spacer(Modifier.height(9.dp))
                }
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
    iconStyle: AppIconStyle,
    onLaunch: (LaunchableApp) -> Unit,
    onActions: (LaunchableApp) -> Unit
) {
    val bitmap = rememberAppIcon(app, iconStyle)
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
            modifier = Modifier.size(42.dp)
                .border(.7.dp, Paper.copy(alpha = .30f), CircleShape)
                .background(Ink.copy(alpha = .22f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (bitmap != null) {
                Image(bitmap.asImageBitmap(), contentDescription = null,
                    modifier = Modifier.size(29.dp))
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

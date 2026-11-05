package dev.monolith.launcher.ui

import android.content.ComponentName
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.LruCache
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.core.graphics.drawable.toBitmap
import dev.monolith.launcher.data.AppIconStyle
import dev.monolith.launcher.data.LaunchableApp
import kotlin.math.roundToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private data class IconCacheKey(
    val appId: String,
    val style: AppIconStyle,
    val sizePx: Int
) {
    val storageKey = "$appId|${style.name}|$sizePx"
}

/** Package icon loading and bitmap rendering stay off the Compose main thread. */
object AppIconCache {
    private val cache = object : LruCache<String, Bitmap>(8 * 1024) {
        override fun sizeOf(key: String, value: Bitmap): Int =
            (value.byteCount / 1024).coerceAtLeast(1)
    }

    var revision by mutableIntStateOf(0)
        private set

    fun load(
        context: Context,
        app: LaunchableApp,
        style: AppIconStyle = AppIconStyle.ORIGINAL,
        sizePx: Int = 96
    ): Bitmap? {
        val boundedSize = sizePx.coerceIn(48, 256)
        val key = IconCacheKey(app.id, style, boundedSize)
        synchronized(cache) { cache.get(key.storageKey) }?.let { return it }
        val bitmap = runCatching {
            val drawable = context.packageManager.getActivityIcon(
                ComponentName(app.packageName, app.activityName)
            )
            when (style) {
                AppIconStyle.ORIGINAL -> drawable.toBitmap(
                    width = boundedSize,
                    height = boundedSize,
                    config = Bitmap.Config.ARGB_8888
                )
                AppIconStyle.MONO -> renderMonochrome(drawable, boundedSize)
                AppIconStyle.INK -> nativeInkDrawable(drawable)?.let {
                    renderInk(it, boundedSize)
                } ?: drawable.toBitmap(
                    width = boundedSize,
                    height = boundedSize,
                    config = Bitmap.Config.ARGB_8888
                )
            }
        }.getOrNull() ?: return null
        synchronized(cache) { cache.put(key.storageKey, bitmap) }
        return bitmap
    }

    /** Invoked on main thread when apps are installed, upgraded or removed. */
    fun invalidate(packageName: String?) {
        synchronized(cache) {
            if (packageName == null) {
                cache.evictAll()
            } else {
                cache.snapshot().keys.filter { it.startsWith("$packageName/") }
                    .forEach(cache::remove)
            }
        }
        revision++
    }
}

private fun nativeInkDrawable(drawable: Drawable): Drawable? =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        drawable is AdaptiveIconDrawable
    ) drawable.monochrome else null

private fun renderMonochrome(drawable: Drawable, sizePx: Int): Bitmap =
    renderDrawable(drawable, sizePx, Paint(Paint.ANTI_ALIAS_FLAG).apply {
        colorFilter = ColorMatrixColorFilter(ColorMatrix().apply { setToSaturation(0f) })
    })

private fun renderInk(drawable: Drawable, sizePx: Int): Bitmap =
    renderDrawable(drawable, sizePx, Paint(Paint.ANTI_ALIAS_FLAG).apply {
        colorFilter = PorterDuffColorFilter(0xFFF4F3F0.toInt(), PorterDuff.Mode.SRC_IN)
    })

private fun renderDrawable(drawable: Drawable, sizePx: Int, paint: Paint): Bitmap {
    val result = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(result)
    drawable.setBounds(0, 0, sizePx, sizePx)
    val layer = canvas.saveLayer(0f, 0f, sizePx.toFloat(), sizePx.toFloat(), paint)
    drawable.draw(canvas)
    canvas.restoreToCount(layer)
    return result
}

@Composable
fun rememberAppIcon(
    app: LaunchableApp?,
    style: AppIconStyle = AppIconStyle.ORIGINAL,
    sizeDp: Int = 48
): Bitmap? {
    val context = LocalContext.current.applicationContext
    val density = LocalDensity.current.density
    val sizePx = (sizeDp.coerceIn(24, 96) * density).roundToInt().coerceIn(48, 256)
    val generation = AppIconCache.revision
    val icon by produceState<Bitmap?>(null, app?.id, style, sizePx, generation) {
        value = if (app == null) null else withContext(Dispatchers.IO) {
            AppIconCache.load(context, app, style, sizePx)
        }
    }
    return icon
}

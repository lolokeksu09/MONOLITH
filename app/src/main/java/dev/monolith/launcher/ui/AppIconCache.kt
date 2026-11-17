package dev.monolith.launcher.ui

import android.content.ComponentName
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
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
                AppIconStyle.INK -> renderInkWithOriginalFallback(
                    drawable, nativeInkDrawable(drawable), boundedSize
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

/**
 * Some adaptive-icon monochrome layers render nearly transparent. The icon
 * renderer must not cache an empty INK bitmap as a successful application
 * icon. Fallback uses the untouched activity icon, matching the existing
 * fallback for apps that have no monochrome layer.
 */
internal fun renderInkWithOriginalFallback(
    original: Drawable,
    monochrome: Drawable?,
    sizePx: Int
): Bitmap {
    val candidate = monochrome?.let { mono ->
        runCatching { renderInk(mono, sizePx) }.getOrNull()
    }
    if (candidate != null && containsVisibleInkArtwork(candidate)) return candidate
    return original.toBitmap(
        width = sizePx,
        height = sizePx,
        config = Bitmap.Config.ARGB_8888
    )
}

/**
 * Check cached monochrome output once, off the UI thread. At least 1/512 of
 * the pixels must have useful opacity; isolated transparent/noise pixels do
 * not count as recognizable icon artwork.
 */
internal fun containsVisibleInkArtwork(bitmap: Bitmap): Boolean {
    if (bitmap.width <= 0 || bitmap.height <= 0) return false
    val pixels = IntArray(bitmap.width * bitmap.height)
    bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
    val required = (pixels.size / 512).coerceAtLeast(4)
    var visible = 0
    for (pixel in pixels) {
        if ((pixel ushr 24) >= 48 && ++visible >= required) return true
    }
    return false
}

private fun renderMonochrome(drawable: Drawable, sizePx: Int): Bitmap =
    renderDrawable(drawable, sizePx, Paint(Paint.ANTI_ALIAS_FLAG).apply {
        colorFilter = ColorMatrixColorFilter(ColorMatrix().apply { setSaturation(0f) })
    })

/**
 * Applying a PorterDuff color filter to a saveLayer paint is not consistent
 * across renderers (notably in bitmap unit tests). Tint the native alpha mask
 * explicitly, retaining its exact alpha channel and changing INK mode only.
 */
private fun renderInk(drawable: Drawable, sizePx: Int): Bitmap {
    val bitmap = renderDrawable(drawable, sizePx, Paint(Paint.ANTI_ALIAS_FLAG))
    val pixels = IntArray(sizePx * sizePx)
    bitmap.getPixels(pixels, 0, sizePx, 0, 0, sizePx, sizePx)
    val inkRgb = 0x00F4F3F0
    for (i in pixels.indices) {
        pixels[i] = (pixels[i] and 0xFF000000.toInt()) or inkRgb
    }
    bitmap.setPixels(pixels, 0, sizePx, 0, 0, sizePx, sizePx)
    return bitmap
}

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

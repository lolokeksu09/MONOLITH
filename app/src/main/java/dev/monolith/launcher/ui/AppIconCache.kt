package dev.monolith.launcher.ui

import android.content.ComponentName
import android.content.Context
import android.graphics.Bitmap
import android.util.LruCache
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.graphics.drawable.toBitmap
import dev.monolith.launcher.data.LaunchableApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Loading package icons can involve disk I/O and bitmap rendering.
 * Never do either inside Compose rendering on the main thread.
 */
object AppIconCache {
    private val cache = object : LruCache<String, Bitmap>(8 * 1024) {
        override fun sizeOf(key: String, value: Bitmap): Int =
            (value.byteCount / 1024).coerceAtLeast(1)
    }

    var revision by mutableIntStateOf(0)
        private set

    fun load(context: Context, app: LaunchableApp): Bitmap? {
        synchronized(cache) { cache.get(app.id) }?.let { return it }
        val bitmap = runCatching {
            context.packageManager
                .getActivityIcon(ComponentName(app.packageName, app.activityName))
                .toBitmap(width = 96, height = 96, config = Bitmap.Config.ARGB_8888)
        }.getOrNull() ?: return null
        synchronized(cache) { cache.put(app.id, bitmap) }
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

@Composable
fun rememberAppIcon(app: LaunchableApp?): Bitmap? {
    val context = LocalContext.current.applicationContext
    val generation = AppIconCache.revision
    val icon by produceState<Bitmap?>(initialValue = null, app?.id, generation) {
        value = if (app == null) null
            else withContext(Dispatchers.IO) { AppIconCache.load(context, app) }
    }
    return icon
}

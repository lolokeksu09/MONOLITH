package dev.monolith.launcher.data

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import java.util.Locale

data class LaunchableApp(
    val id: String,
    val label: String,
    val packageName: String,
    val activityName: String
)

fun filterApps(apps: List<LaunchableApp>, query: String): List<LaunchableApp> {
    val needle = query.trim().lowercase(Locale.ROOT)
    return if (needle.isEmpty()) apps else apps.filter {
        it.label.lowercase(Locale.ROOT).contains(needle) ||
            it.packageName.lowercase(Locale.ROOT).contains(needle)
    }
}

class AppCatalog(private val context: Context) {
    private val pm: PackageManager get() = context.packageManager

    fun load(): List<LaunchableApp> {
        val launcherIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        @Suppress("DEPRECATION")
        val activities = pm.queryIntentActivities(launcherIntent, 0)
        return activities.mapNotNull { info ->
            val activity = info.activityInfo ?: return@mapNotNull null
            LaunchableApp(
                id = "${activity.packageName}/${activity.name}",
                label = info.loadLabel(pm).toString(),
                packageName = activity.packageName,
                activityName = activity.name
            )
        }.distinctBy { it.id }.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.label })
    }

    fun launch(app: LaunchableApp): Boolean {
        val intent = Intent(Intent.ACTION_MAIN)
            .addCategory(Intent.CATEGORY_LAUNCHER)
            .setClassName(app.packageName, app.activityName)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
        return try {
            context.startActivity(intent)
            true
        } catch (_: ActivityNotFoundException) {
            false
        } catch (_: SecurityException) {
            false
        }
    }
}

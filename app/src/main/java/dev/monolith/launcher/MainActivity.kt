package dev.monolith.launcher

import android.app.role.RoleManager
import android.content.ActivityNotFoundException
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.lifecycleScope
import dev.monolith.launcher.data.AppCatalog
import dev.monolith.launcher.data.HiddenAppsStore
import dev.monolith.launcher.data.LaunchStats
import dev.monolith.launcher.data.LaunchStatsStore
import dev.monolith.launcher.data.LaunchableApp
import dev.monolith.launcher.data.LauncherPreferences
import dev.monolith.launcher.data.LauncherSettingsStore
import dev.monolith.launcher.data.OrbitFavoritesStore
import dev.monolith.launcher.data.normalizeOrbitFavorites
import dev.monolith.launcher.data.hideApp
import dev.monolith.launcher.data.unhideApp
import dev.monolith.launcher.ui.AppIconCache
import dev.monolith.launcher.ui.MonolithScreen
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * A background catalog refresh may have captured stats before a successful app
 * launch or History Clear. Do not let that stale snapshot overwrite newer local
 * state. No persistence format or HOME behavior is changed.
 */
internal fun statsAfterCatalogRefresh(
    current: Map<String, LaunchStats>,
    refreshed: Map<String, LaunchStats>,
    revisionAtStart: Long,
    currentRevision: Long
): Map<String, LaunchStats> =
    if (revisionAtStart == currentRevision) refreshed else current

/** Android HOME entry point. Legacy desktop, Dock and widget state is not modified or cleared. */
class MainActivity : ComponentActivity() {
    private lateinit var catalog: AppCatalog
    private lateinit var settingsStore: LauncherSettingsStore
    private lateinit var statsStore: LaunchStatsStore
    private lateinit var hiddenStore: HiddenAppsStore
    private lateinit var orbitStore: OrbitFavoritesStore

    private var apps by mutableStateOf<List<LaunchableApp>>(emptyList())
    private var preferences by mutableStateOf(LauncherPreferences())
    private var launchStats by mutableStateOf<Map<String, LaunchStats>>(emptyMap())
    private var hiddenApps by mutableStateOf<Set<String>>(emptySet())
    private var orbitFavorites by mutableStateOf<List<String>>(emptyList())
    private var loading by mutableStateOf(true)
    private var loadError by mutableStateOf(false)
    private var isDefaultHome by mutableStateOf(false)
    private var homeRequestGeneration by mutableIntStateOf(0)
    private var refreshJob: Job? = null
    // Revision advances on successful MONOLITH launches and history clearing.
    private var statsRevision: Long = 0L
    private var receiverRegistered = false

    private val packageReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            AppIconCache.invalidate(intent.data?.schemeSpecificPart)
            refreshApps()
        }
    }

    private val homeRoleLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        isDefaultHome = checkDefaultHome()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        catalog = AppCatalog(applicationContext)
        settingsStore = LauncherSettingsStore(applicationContext)
        statsStore = LaunchStatsStore(applicationContext)
        hiddenStore = HiddenAppsStore(applicationContext)
        orbitStore = OrbitFavoritesStore(applicationContext)
        preferences = settingsStore.read()
        hiddenApps = hiddenStore.read()
        orbitFavorites = orbitStore.read()

        setContent {
            MonolithScreen(
                apps = apps,
                preferences = preferences,
                homeRequestGeneration = homeRequestGeneration,
                launchStats = launchStats,
                hiddenApps = hiddenApps,
                orbitFavorites = orbitFavorites,
                loading = loading,
                loadError = loadError,
                isDefaultHome = isDefaultHome,
                onRetry = ::refreshApps,
                onMakeDefaultHome = ::requestDefaultHome,
                onLaunch = { app ->
                    if (catalog.launch(app)) {
                        val next = statsStore.record(app.id)
                        statsRevision++
                        launchStats = launchStats + (app.id to next)
                    } else {
                        Toast.makeText(this, R.string.launch_failed, Toast.LENGTH_SHORT).show()
                        refreshApps()
                    }
                },
                onAppInfo = ::openAppInfo,
                onOrbitFavoritesChange = { next ->
                    val normalized = normalizeOrbitFavorites(next)
                    orbitFavorites = normalized
                    orbitStore.write(normalized)
                },
                onPreferencesChange = { next ->
                    preferences = next
                    settingsStore.write(next)
                },
                onHideApp = { app ->
                    val next = hideApp(hiddenApps, app.id)
                    if (next != hiddenApps) {
                        hiddenApps = next
                        hiddenStore.write(next)
                    }
                },
                onUnhideApp = { app ->
                    val next = unhideApp(hiddenApps, app.id)
                    if (next != hiddenApps) {
                        hiddenApps = next
                        hiddenStore.write(next)
                    }
                },
                onClearLaunchHistory = {
                    statsStore.clear()
                    statsRevision++
                    launchStats = emptyMap()
                    Toast.makeText(this, R.string.usage_cleared, Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.action == Intent.ACTION_MAIN && intent.hasCategory(Intent.CATEGORY_HOME)) {
            homeRequestGeneration++
        }
    }

    private fun openAppInfo(app: LaunchableApp) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            .setData(Uri.fromParts("package", app.packageName, null))
        try {
            startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(this, R.string.app_info_unavailable, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onStart() {
        super.onStart()
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_CHANGED)
            addAction(Intent.ACTION_PACKAGE_REPLACED)
            addDataScheme("package")
        }
        registerReceiver(packageReceiver, filter)
        receiverRegistered = true
    }

    override fun onStop() {
        if (receiverRegistered) {
            unregisterReceiver(packageReceiver)
            receiverRegistered = false
        }
        super.onStop()
    }

    override fun onResume() {
        super.onResume()
        isDefaultHome = checkDefaultHome()
        refreshApps()
    }

    private fun checkDefaultHome(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roles = getSystemService(RoleManager::class.java)
            if (roles?.isRoleAvailable(RoleManager.ROLE_HOME) == true) {
                return roles.isRoleHeld(RoleManager.ROLE_HOME)
            }
        }
        val homeIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        @Suppress("DEPRECATION")
        val resolved = packageManager.resolveActivity(homeIntent, 0)
        return resolved?.activityInfo?.packageName == packageName
    }

    private fun requestDefaultHome() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roles = getSystemService(RoleManager::class.java)
            if (roles?.isRoleAvailable(RoleManager.ROLE_HOME) == true &&
                !roles.isRoleHeld(RoleManager.ROLE_HOME)
            ) {
                homeRoleLauncher.launch(roles.createRequestRoleIntent(RoleManager.ROLE_HOME))
                return
            }
        }
        try {
            startActivity(Intent(Settings.ACTION_HOME_SETTINGS))
        } catch (_: ActivityNotFoundException) {
            startActivity(Intent(Settings.ACTION_SETTINGS))
        }
    }

    private fun refreshApps() {
        refreshJob?.cancel()
        loading = true
        loadError = false
        // Capture this BEFORE launching the IO operation, on the main thread.
        val revisionAtStart = statsRevision
        refreshJob = lifecycleScope.launch {
            // PackageManager enumeration and SharedPreferences history loading
            // are potentially expensive. Neither should block Compose / HOME.
            val result: Result<Pair<List<LaunchableApp>, Map<String, LaunchStats>>> =
                withContext(Dispatchers.IO) {
                    try {
                        val refreshedApps = catalog.load()
                        Result.success(refreshedApps to statsStore.read(refreshedApps))
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        Result.failure(e)
                    }
                }
            result.fold(
                onSuccess = { (refreshedApps, refreshedStats) ->
                    apps = refreshedApps
                    launchStats = statsAfterCatalogRefresh(
                        launchStats, refreshedStats, revisionAtStart, statsRevision
                    )
                    loadError = false
                },
                onFailure = {
                    // Retain last known installed apps and stats for recovery.
                    loadError = true
                }
            )
            loading = false
        }
    }

    override fun onDestroy() {
        refreshJob?.cancel()
        super.onDestroy()
    }
}

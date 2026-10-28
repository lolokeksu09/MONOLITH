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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.lifecycleScope
import dev.monolith.launcher.data.AppCatalog
import dev.monolith.launcher.data.LaunchableApp
import dev.monolith.launcher.data.LauncherPreferences
import dev.monolith.launcher.data.LauncherSettingsStore
import dev.monolith.launcher.data.PinnedAppsStore
import dev.monolith.launcher.data.movePinned
import dev.monolith.launcher.data.togglePinned
import dev.monolith.launcher.ui.MonolithScreen
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    private lateinit var catalog: AppCatalog
    private lateinit var pinnedStore: PinnedAppsStore
    private lateinit var settingsStore: LauncherSettingsStore
    private var apps by mutableStateOf<List<LaunchableApp>>(emptyList())
    private var pinnedIds by mutableStateOf<List<String>>(emptyList())
    private var preferences by mutableStateOf(LauncherPreferences())
    private var loading by mutableStateOf(true)
    private var loadError by mutableStateOf(false)
    private var isDefaultHome by mutableStateOf(false)
    private var refreshJob: Job? = null

    private val packageReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) = refreshApps()
    }
    private var receiverRegistered = false

    private val homeRoleLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { isDefaultHome = checkDefaultHome() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        catalog = AppCatalog(applicationContext)
        pinnedStore = PinnedAppsStore(applicationContext)
        settingsStore = LauncherSettingsStore(applicationContext)
        pinnedIds = pinnedStore.read()
        preferences = settingsStore.read()
        setContent {
            MonolithScreen(
                apps = apps,
                pinnedIds = pinnedIds,
                preferences = preferences,
                loading = loading,
                loadError = loadError,
                isDefaultHome = isDefaultHome,
                onMakeDefaultHome = ::requestDefaultHome,
                onRetry = ::refreshApps,
                onLaunch = { app ->
                    if (!catalog.launch(app)) {
                        Toast.makeText(this, R.string.launch_failed, Toast.LENGTH_SHORT).show()
                        refreshApps()
                    }
                },
                onTogglePin = { app ->
                    updatePins(togglePinned(pinnedIds, app.id))
                },
                onMovePinned = { app, delta ->
                    updatePins(movePinned(pinnedIds, app.id, delta))
                },
                onAppInfo = ::openAppInfo,
                onPreferencesChange = { next ->
                    preferences = next
                    settingsStore.write(next)
                }
            )
        }
    }

    private fun updatePins(next: List<String>) {
        if (next != pinnedIds) {
            pinnedIds = next
            pinnedStore.write(next)
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
            val roleManager = getSystemService(RoleManager::class.java)
            if (roleManager?.isRoleAvailable(RoleManager.ROLE_HOME) == true) {
                return roleManager.isRoleHeld(RoleManager.ROLE_HOME)
            }
        }
        val homeIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        @Suppress("DEPRECATION")
        val resolved = packageManager.resolveActivity(homeIntent, 0)
        return resolved?.activityInfo?.packageName == packageName
    }

    private fun requestDefaultHome() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = getSystemService(RoleManager::class.java)
            if (roleManager?.isRoleAvailable(RoleManager.ROLE_HOME) == true &&
                !roleManager.isRoleHeld(RoleManager.ROLE_HOME)
            ) {
                homeRoleLauncher.launch(roleManager.createRequestRoleIntent(RoleManager.ROLE_HOME))
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
        refreshJob = lifecycleScope.launch {
            val result = try {
                withContext(Dispatchers.IO) { runCatching { catalog.load() } }
            } catch (e: CancellationException) {
                throw e
            }
            apps = result.getOrDefault(emptyList())
            loadError = result.isFailure
            loading = false
        }
    }

    override fun onDestroy() {
        refreshJob?.cancel()
        super.onDestroy()
    }
}

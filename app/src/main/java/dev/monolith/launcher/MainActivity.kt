package dev.monolith.launcher

import android.app.role.RoleManager
import android.content.ActivityNotFoundException
import android.content.Intent
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
import dev.monolith.launcher.data.PinnedAppsStore
import dev.monolith.launcher.data.togglePinned
import dev.monolith.launcher.ui.MonolithScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    private lateinit var catalog: AppCatalog
    private lateinit var pinnedStore: PinnedAppsStore
    private var apps by mutableStateOf<List<LaunchableApp>>(emptyList())
    private var pinnedIds by mutableStateOf<List<String>>(emptyList())
    private var loading by mutableStateOf(true)
    private var isDefaultHome by mutableStateOf(false)
    private var refreshJob: Job? = null
    private val homeRoleLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        isDefaultHome = checkDefaultHome()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        catalog = AppCatalog(applicationContext)
        pinnedStore = PinnedAppsStore(applicationContext)
        pinnedIds = pinnedStore.read()
        setContent {
            MonolithScreen(
                apps = apps,
                pinnedIds = pinnedIds,
                loading = loading,
                isDefaultHome = isDefaultHome,
                onMakeDefaultHome = ::requestDefaultHome,
                onLaunch = { app ->
                    if (!catalog.launch(app)) {
                        Toast.makeText(this, R.string.launch_failed, Toast.LENGTH_SHORT).show()
                        refreshApps()
                    }
                },
                onTogglePin = { app ->
                    pinnedIds = togglePinned(pinnedIds, app.id)
                    pinnedStore.write(pinnedIds)
                }
            )
        }
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
                homeRoleLauncher.launch(
                    roleManager.createRequestRoleIntent(RoleManager.ROLE_HOME)
                )
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
        refreshJob = lifecycleScope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching { catalog.load() }
            }
            apps = result.getOrDefault(emptyList())
            loading = false
            if (result.isFailure) {
                Toast.makeText(this@MainActivity, R.string.catalog_failed, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        refreshJob?.cancel()
        super.onDestroy()
    }
}

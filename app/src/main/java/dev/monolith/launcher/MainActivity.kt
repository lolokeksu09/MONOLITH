package dev.monolith.launcher

import android.app.role.RoleManager
import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
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
import dev.monolith.launcher.data.HomeLayout
import dev.monolith.launcher.data.HomeLayoutStore
import dev.monolith.launcher.data.LaunchableApp
import dev.monolith.launcher.data.LauncherPreferences
import dev.monolith.launcher.data.LauncherSettingsStore
import dev.monolith.launcher.data.WidgetIdsStore
import dev.monolith.launcher.data.addAppToFolder
import dev.monolith.launcher.data.createHomeFolder
import dev.monolith.launcher.data.moveAppOutOfFolder
import dev.monolith.launcher.data.pinToHome
import dev.monolith.launcher.data.removeHomeFolder
import dev.monolith.launcher.data.renameHomeFolder
import dev.monolith.launcher.data.reorderHome
import dev.monolith.launcher.data.unpinFromHome
import dev.monolith.launcher.ui.MonolithScreen
import java.util.UUID
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    companion object {
        private const val WIDGET_HOST_ID = 14903
        private const val WIDGET_CONFIG_REQUEST = 7301
    }

    private lateinit var catalog: AppCatalog
    private lateinit var layoutStore: HomeLayoutStore
    private lateinit var settingsStore: LauncherSettingsStore
    private lateinit var widgetIdsStore: WidgetIdsStore
    private val widgetHost by lazy { AppWidgetHost(this, WIDGET_HOST_ID) }
    private val widgetManager by lazy { AppWidgetManager.getInstance(this) }
    private var apps by mutableStateOf<List<LaunchableApp>>(emptyList())
    private var layout by mutableStateOf(HomeLayout())
    private var preferences by mutableStateOf(LauncherPreferences())
    private var widgetIds by mutableStateOf<List<Int>>(emptyList())
    private var widgetProviders by mutableStateOf<List<AppWidgetProviderInfo>>(emptyList())
    private var loading by mutableStateOf(true)
    private var loadError by mutableStateOf(false)
    private var widgetError by mutableStateOf(false)
    private var isDefaultHome by mutableStateOf(false)
    private var pendingWidgetId: Int? = null
    private var refreshJob: Job? = null
    private var receiverRegistered = false

    private val packageReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) = refreshApps()
    }
    private val homeRoleLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { isDefaultHome = checkDefaultHome() }

    private val bindWidgetLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val id = pendingWidgetId ?: return@registerForActivityResult
        if (result.resultCode == RESULT_OK && widgetManager.getAppWidgetInfo(id) != null) {
            configureOrAddWidget(id)
        } else {
            cancelWidget(id)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        catalog = AppCatalog(applicationContext)
        layoutStore = HomeLayoutStore(applicationContext)
        settingsStore = LauncherSettingsStore(applicationContext)
        widgetIdsStore = WidgetIdsStore(applicationContext)
        layout = layoutStore.read()
        preferences = settingsStore.read()
        widgetIds = widgetIdsStore.read()
        pendingWidgetId = savedInstanceState?.getInt("pendingWidgetId", -1)?.takeIf { it > 0 }
        setContent {
            MonolithScreen(
                apps = apps, layout = layout, preferences = preferences,
                loading = loading, loadError = loadError,
                widgetIds = widgetIds, widgetHost = widgetHost, widgetManager = widgetManager,
                widgetProviders = widgetProviders, widgetError = widgetError,
                isDefaultHome = isDefaultHome,
                onMakeDefaultHome = ::requestDefaultHome,
                onRetry = ::refreshApps,
                onLaunch = { app ->
                    if (!catalog.launch(app)) {
                        Toast.makeText(this, R.string.launch_failed, Toast.LENGTH_SHORT).show()
                        refreshApps()
                    }
                },
                onPin = { app -> updateLayout(pinToHome(layout, app.id)) },
                onUnpin = { app -> updateLayout(unpinFromHome(layout, app.id)) },
                onNewFolder = { app ->
                    updateLayout(createHomeFolder(layout, app.id, UUID.randomUUID().toString(), getString(R.string.new_folder)))
                },
                onAddToFolder = { app, folderId -> updateLayout(addAppToFolder(layout, app.id, folderId)) },
                onRenameFolder = { id, name -> updateLayout(renameHomeFolder(layout, id, name)) },
                onDeleteFolder = { id -> updateLayout(removeHomeFolder(layout, id)) },
                onMoveOutOfFolder = { app, id -> updateLayout(moveAppOutOfFolder(layout, app.id, id)) },
                onReorder = { from, to -> updateLayout(reorderHome(layout, from, to)) },
                onAppInfo = ::openAppInfo,
                onPreferencesChange = { next ->
                    preferences = next
                    settingsStore.write(next)
                },
                onShowWidgets = ::loadWidgetProviders,
                onChooseWidget = ::selectWidget,
                onRemoveWidget = ::removeWidget
            )
        }
    }

    private fun updateLayout(next: HomeLayout) {
        if (next != layout) {
            layout = next
            layoutStore.write(next)
        }
    }

    private fun loadWidgetProviders() {
        widgetError = false
        widgetProviders = runCatching {
            widgetManager.installedProviders.sortedBy { it.loadLabel(packageManager).lowercase() }
        }.getOrElse {
            widgetError = true
            emptyList()
        }
    }

    private fun selectWidget(provider: AppWidgetProviderInfo) {
        if (pendingWidgetId != null) return
        widgetError = false
        val id = try {
            widgetHost.allocateAppWidgetId()
        } catch (_: RuntimeException) {
            widgetError = true
            return
        }
        pendingWidgetId = id
        try {
            if (widgetManager.bindAppWidgetIdIfAllowed(id, provider.profile, provider.provider, null)) {
                configureOrAddWidget(id)
            } else {
                val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_BIND)
                    .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id)
                    .putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, provider.provider)
                    .putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER_PROFILE, provider.profile)
                bindWidgetLauncher.launch(intent)
            }
        } catch (_: RuntimeException) {
            cancelWidget(id)
        }
    }

    private fun configureOrAddWidget(id: Int) {
        val info = widgetManager.getAppWidgetInfo(id)
        if (info == null) {
            cancelWidget(id)
            return
        }
        if (info.configure != null) {
            try {
                widgetHost.startAppWidgetConfigureActivityForResult(
                    this, id, 0, WIDGET_CONFIG_REQUEST, null
                )
            } catch (_: Exception) {
                cancelWidget(id)
            }
        } else {
            completeWidget(id)
        }
    }

    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == WIDGET_CONFIG_REQUEST) {
            val id = pendingWidgetId ?: return
            if (resultCode == RESULT_OK && widgetManager.getAppWidgetInfo(id) != null) {
                completeWidget(id)
            } else {
                cancelWidget(id)
            }
        }
    }

    private fun completeWidget(id: Int) {
        if (pendingWidgetId != id) return
        widgetIds = (widgetIds + id).distinct()
        widgetIdsStore.write(widgetIds)
        pendingWidgetId = null
    }

    private fun cancelWidget(id: Int) {
        runCatching { widgetHost.deleteAppWidgetId(id) }
        if (pendingWidgetId == id) pendingWidgetId = null
        widgetError = true
    }

    private fun removeWidget(id: Int) {
        widgetIds = widgetIds.filterNot { it == id }
        widgetIdsStore.write(widgetIds)
        runCatching { widgetHost.deleteAppWidgetId(id) }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        pendingWidgetId?.let { outState.putInt("pendingWidgetId", it) }
        super.onSaveInstanceState(outState)
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
        runCatching { widgetHost.startListening() }
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
        runCatching { widgetHost.stopListening() }
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

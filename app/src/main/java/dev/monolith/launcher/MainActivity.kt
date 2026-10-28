package dev.monolith.launcher

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.lifecycleScope
import dev.monolith.launcher.data.AppCatalog
import dev.monolith.launcher.data.LaunchableApp
import dev.monolith.launcher.data.PinnedAppsStore
import dev.monolith.launcher.ui.MonolithScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    private lateinit var catalog: AppCatalog
    private lateinit var pinnedStore: PinnedAppsStore
    private var apps by mutableStateOf<List<LaunchableApp>>(emptyList())
    private var pinnedIds by mutableStateOf<List<String>>(emptyList())
    private var loading by mutableStateOf(true)

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
                onLaunch = { app ->
                    if (!catalog.launch(app)) {
                        Toast.makeText(this, R.string.launch_failed, Toast.LENGTH_SHORT).show()
                        refreshApps()
                    }
                },
                onTogglePin = { app ->
                    pinnedIds = if (app.id in pinnedIds) {
                        pinnedIds - app.id
                    } else {
                        pinnedIds + app.id
                    }
                    pinnedStore.write(pinnedIds)
                }
            )
        }
    }

    override fun onResume() {
        super.onResume()
        refreshApps()
    }

    private fun refreshApps() {
        loading = true
        lifecycleScope.launch {
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
}

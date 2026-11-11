package dev.monolith.launcher.data

import java.util.Locale

/**
 * Immutable inputs for the expensive drawer catalog computation.
 *
 * Creation and change detection happen on the UI thread; filtering, Unicode
 * collation and sorting execute on Dispatchers.Default in the Compose caller.
 */
data class DrawerProjectionRequest(
    val apps: List<LaunchableApp>,
    val hiddenIds: Set<String>,
    val settledQuery: String,
    val searchEnabled: Boolean,
    val sort: AppSort,
    val stats: Map<String, LaunchStats>,
    val locale: Locale
)

data class DrawerProjectionResult(
    val request: DrawerProjectionRequest,
    val apps: List<LaunchableApp>
)

fun projectDrawerApps(request: DrawerProjectionRequest): List<LaunchableApp> =
    sortApps(
        filterApps(
            filterHiddenApps(request.apps, request.hiddenIds),
            effectiveDrawerQuery(request.settledQuery, request.searchEnabled)
        ),
        request.sort,
        request.stats,
        request.locale
    )

/**
 * A previous result may still be retained by produceState while a new request
 * is processing. Reject it, particularly when an app has just been hidden.
 *
 * Identity is deliberate: even a structurally equal re-request is a new
 * generation and must not receive an older completion.
 * null = processing; emptyList() = completed with no matches.
 */
fun matchingDrawerApps(
    request: DrawerProjectionRequest,
    completed: DrawerProjectionResult?
): List<LaunchableApp>? = completed?.takeIf { it.request === request }?.apps

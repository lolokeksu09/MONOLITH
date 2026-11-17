package dev.monolith.launcher.ui

import androidx.compose.runtime.mutableStateOf
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.performScrollTo
import dev.monolith.launcher.data.AppIconStyle
import dev.monolith.launcher.data.LaunchableApp
import dev.monolith.launcher.data.LaunchStats
import dev.monolith.launcher.data.LauncherPreferences
import dev.monolith.launcher.data.DrawerSearchPosition
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Compose-level checks for the HOME / drawer / settings paths. These run as local
 * unit tests under Robolectric with native graphics, so they execute in CI without
 * an emulator. They are not a substitute for physical TalkBack or motion review.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35])
class MonolithScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val appAlpha = LaunchableApp("pkg/alpha", "Alpha", "pkg", "alpha")
    private val appBeta = LaunchableApp("pkg/beta", "Beta", "pkg", "beta")
    private val appGamma = LaunchableApp("pkg/gamma", "Gamma", "pkg", "gamma")
    private val currentPreferences = mutableStateOf(LauncherPreferences())
    private val currentFavorites = mutableStateOf<List<String>>(emptyList())

    private fun launchMonolith(
        apps: List<LaunchableApp> = listOf(appAlpha, appBeta),
        loading: Boolean = false,
        loadError: Boolean = false,
        hiddenApps: Set<String> = emptySet(),
        launchStats: Map<String, LaunchStats> = emptyMap(),
        onRetry: () -> Unit = {},
        onPreferencesChange: (LauncherPreferences) -> Unit =
            { currentPreferences.value = it },
        onClearLaunchHistory: () -> Unit = {},
        onLaunch: (LaunchableApp) -> Unit = {}
    ) {
        composeRule.setContent {
            MonolithScreen(
                apps = apps,
                preferences = currentPreferences.value,
                homeRequestGeneration = 0,
                launchStats = launchStats,
                hiddenApps = hiddenApps,
                orbitFavorites = currentFavorites.value,
                loading = loading,
                loadError = loadError,
                isDefaultHome = true,
                onRetry = onRetry,
                onMakeDefaultHome = {},
                onLaunch = onLaunch,
                onAppInfo = {},
                onOrbitFavoritesChange = { currentFavorites.value = it },
                onPreferencesChange = onPreferencesChange,
                onHideApp = {},
                onUnhideApp = {},
                onClearLaunchHistory = onClearLaunchHistory
            )
        }
    }

    @Test
    fun regularPhoneLayoutPreservesApprovedHomeHierarchy() {
        val layout = referencePhoneLayout(412f, 860f, 1f)
        assertFalse(layout.compact)
        assertEquals(32, layout.mediaTopDp)
        assertEquals(142, layout.clockTopDp)
        assertEquals(78f, layout.clockFontSp)
        assertEquals(215, layout.quickAppsBottomDp)
        assertTrue(layout.showQuickApps)
        assertEquals(8, layout.editorialAppLimit)
        assertEquals(144, layout.editorialBottomDp)
    }

    @Test
    fun compactAndLargeFontLayoutsKeepTheClockAndReduceClutter() {
        val compact = referencePhoneLayout(320f, 640f, 1.5f)
        assertTrue(compact.compact)
        assertTrue(compact.showQuickApps)
        assertTrue(compact.clockFontSp < 78f)
        assertEquals(4, compact.editorialAppLimit)
        assertTrue(compact.editorialDayFontSp < 96f)

        val short = referencePhoneLayout(320f, 520f, 2f)
        assertTrue(short.compact)
        assertFalse(short.showQuickApps)
        assertFalse(short.showClockDate)
        assertFalse(short.showEditorialGreeting)
        assertTrue(short.clockFontSp > 0f)
        assertTrue(short.clockTopDp + short.clockLineSp * 2f < 260f)

        // An invalid font scale cannot create NaN offsets or font sizes.
        assertTrue(referencePhoneLayout(320f, 520f, Float.NaN).clockFontSp.isFinite())
    }

    @Test
    fun veryShortScreenRetainsOrbitAndAppsNavigationWithoutQuickDockOverlap() {
        composeRule.setContent {
            Box(Modifier.size(320.dp, 520.dp)) {
                ReferenceHome(
                    apps = listOf(appAlpha, appBeta),
                    iconStyle = AppIconStyle.ORIGINAL,
                    homeRequestGeneration = 0,
                    isDefaultHome = true,
                    loading = false,
                    loadError = false,
                    onRetry = {},
                    onLaunch = {},
                    onAppActions = {},
                    onAllApps = {},
                    onSettings = {},
                    onMakeDefaultHome = {}
                )
            }
        }
        composeRule.onNodeWithText("OPEN ORBIT").assertExists()
        composeRule.onNodeWithText("QUICK ACCESS").assertDoesNotExist()
        composeRule.onNodeWithContentDescription("Settings").assertExists()
    }

    @Test
    fun precisionHomeShowsTimepieceAndSmallOrbitHitTarget() {
        launchMonolith(apps = listOf(appAlpha, appBeta))
        composeRule.onNodeWithText("MONOLITH").assertExists()
        // Quick access is omitted on genuinely short HOME layouts.
        // The central Orbit trigger must remain accessible.
        composeRule.onNodeWithText("OPEN ORBIT").assertExists()
        composeRule.onNodeWithText("System playback controls").assertDoesNotExist()
        // On short layouts the collapsed quick-access panel is intentionally hidden.
        // The real app shortcut must still be accessible once Orbit expands.
        val center = composeRule.onNode(
            SemanticsMatcher.expectValue(
                SemanticsProperties.StateDescription, "Orbit applications collapsed"
            )
        )
        val width = center.fetchSemanticsNode().boundsInRoot.width
        val settingsWidth = composeRule.onNodeWithContentDescription("Settings")
            .fetchSemanticsNode().boundsInRoot.width
        assertTrue("The optical dial must not expand the 48dp tap target",
            width <= settingsWidth * 1.5f)
        center.performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription("Alpha").assertExists()
        composeRule.onNodeWithText("MONOLITH").assertDoesNotExist()
        composeRule.onNodeWithText("QUICK ACCESS").assertDoesNotExist()
    }

    @Test
    fun orbitTriggerOnlyOccupiesCentralTouchTarget() {
        launchMonolith(apps = listOf(appAlpha))
        val collapsed = composeRule.onNode(
            SemanticsMatcher.expectValue(
                SemanticsProperties.StateDescription, "Orbit applications collapsed"
            )
        )
        val triggerWidth = collapsed.fetchSemanticsNode().boundsInRoot.width
        val settingsWidth = composeRule.onNodeWithContentDescription("Settings")
            .fetchSemanticsNode().boundsInRoot.width
        assertTrue("Orbit must not turn its whole 304dp area into a button",
            triggerWidth <= settingsWidth * 1.5f)
        collapsed.performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription("Alpha").assertExists()
        composeRule.onNode(
            SemanticsMatcher.expectValue(
                SemanticsProperties.StateDescription, "Orbit applications expanded"
            )
        ).assertExists()
    }

    @Test
    fun collapseControlStillClosesOrbitOnOrdinaryTap() {
        launchMonolith(apps = listOf(appAlpha, appBeta))
        composeRule.onNode(
            SemanticsMatcher.expectValue(
                SemanticsProperties.StateDescription, "Orbit applications collapsed"
            )
        ).performClick()
        composeRule.onNodeWithText("Collapse orbit").performClick()
        composeRule.waitUntil(5_000) {
            runCatching {
                composeRule.onNode(
                    SemanticsMatcher.expectValue(
                        SemanticsProperties.StateDescription, "Orbit applications collapsed"
                    )
                ).assertExists()
            }.isSuccess
        }
    }

    @Test
    fun longPressDragFromCollapseLaunchesSingleOrbitApp() {
        val launched = mutableListOf<LaunchableApp>()
        launchMonolith(apps = listOf(appAlpha), onLaunch = { launched += it })
        composeRule.onNode(
            SemanticsMatcher.expectValue(
                SemanticsProperties.StateDescription, "Orbit applications collapsed"
            )
        ).performClick()
        composeRule.waitForIdle()
        // The existing Collapse control is 176dp below the Orbit center.
        composeRule.onNode(
            SemanticsMatcher.expectValue(
                SemanticsProperties.StateDescription, "Orbit applications expanded"
            )
        ).performTouchInput {
            val start = center
            val pxPerDp = height / 48f
            down(start)
            advanceEventTime(800L)
            moveTo(Offset(start.x, start.y - 48f * pxPerDp))
            moveTo(Offset(start.x, start.y - 176f * pxPerDp))
            up()
        }
        composeRule.waitForIdle()
        assertEquals(listOf(appAlpha), launched)
        // Quick access is omitted on genuinely short HOME layouts.
        // Navigation and the central Orbit trigger must remain usable.
        composeRule.onNodeWithText("OPEN ORBIT").assertExists()
    }

    @Test
    fun dragReleaseBetweenCollapseAndOrbitIconDoesNotLaunch() {
        val launched = mutableListOf<LaunchableApp>()
        launchMonolith(apps = listOf(appAlpha), onLaunch = { launched += it })
        composeRule.onNode(
            SemanticsMatcher.expectValue(
                SemanticsProperties.StateDescription, "Orbit applications collapsed"
            )
        ).performClick()
        composeRule.waitForIdle()
        composeRule.onNode(
            SemanticsMatcher.expectValue(
                SemanticsProperties.StateDescription, "Orbit applications expanded"
            )
        ).performTouchInput {
            val start = center
            val pxPerDp = height / 48f
            down(start)
            advanceEventTime(800L)
            moveTo(Offset(start.x, start.y - 48f * pxPerDp))
            moveTo(Offset(start.x, start.y - 88f * pxPerDp))
            up()
        }
        composeRule.waitForIdle()
        assertTrue(launched.isEmpty())
        composeRule.onNode(
            SemanticsMatcher.expectValue(
                SemanticsProperties.StateDescription, "Orbit applications expanded"
            )
        ).assertExists()
    }

    @Test
    fun oneHandedBottomSearchIsOptInAndStillFilters() {
        launchMonolith()
        composeRule.onNodeWithContentDescription("Settings").performClick()
        composeRule.onNodeWithText("Search bar position").assertExists()
        composeRule.onNodeWithText("Bottom").performScrollTo().performClick()
        composeRule.waitForIdle()
        assertEquals(DrawerSearchPosition.BOTTOM,
            currentPreferences.value.drawerSearchPosition)
        composeRule.onNodeWithText("Back home").performScrollTo().performClick()
        composeRule.onNodeWithText("ALL APPS ↑").performClick()
        // Partial query avoids matching both TextField value and result label.
        composeRule.onNode(hasSetTextAction()).performTextInput("Bet")
        composeRule.waitUntil(5_000) {
            runCatching {
                composeRule.onNodeWithText("Alpha").assertDoesNotExist()
                composeRule.onNodeWithText("Beta").assertExists()
            }.isSuccess
        }
        composeRule.onNodeWithText("Beta").assertIsDisplayed()
    }

    @Test
    fun backReturnsToParentSettingsAndThenDrawerOrigin() {
        assertEquals(Screen.SETTINGS, destinationOnBack(Screen.ORBIT_APPS, Screen.HOME))
        assertEquals(Screen.SETTINGS, destinationOnBack(Screen.HIDDEN_APPS, Screen.ALL_APPS))
        assertEquals(Screen.ALL_APPS, destinationOnBack(Screen.SETTINGS, Screen.ALL_APPS))
        assertEquals(Screen.HOME, destinationOnBack(Screen.SETTINGS, Screen.HOME))
        assertEquals(Screen.HOME, destinationOnBack(Screen.ALL_APPS, Screen.HOME))
    }

    @Test
    fun editorialRecentShortcutsOpenActualAppsWithoutChangingOrbitFavorites() {
        val launched = mutableListOf<LaunchableApp>()
        currentFavorites.value = listOf(appAlpha.id)
        launchMonolith(
            apps = listOf(appAlpha, appBeta),
            launchStats = mapOf(appBeta.id to LaunchStats(1, 100L)),
            onLaunch = { launched += it }
        )
        composeRule.onNodeWithText("EDITORIAL →").performClick()
        composeRule.onNodeWithText("RECENT IN MONOLITH").assertExists()
        composeRule.onNodeWithContentDescription("Beta").performClick()
        composeRule.waitForIdle()
        assertEquals(listOf(appBeta), launched)
        assertEquals(listOf(appAlpha.id), currentFavorites.value)
        composeRule.onNodeWithText("OPEN ORBIT").assertExists()
    }

    @Test
    fun editorialFreshOrHiddenHistoryUsesRealQuickAccessFallback() {
        launchMonolith(
            apps = listOf(appAlpha, appBeta),
            hiddenApps = setOf(appBeta.id),
            launchStats = mapOf(appBeta.id to LaunchStats(1, 100L))
        )
        composeRule.onNodeWithText("EDITORIAL →").performClick()
        composeRule.onNodeWithText("QUICK ACCESS").assertExists()
        composeRule.onNodeWithText("RECENT IN MONOLITH").assertDoesNotExist()
        composeRule.onNodeWithContentDescription("Alpha").assertExists()
        composeRule.onNodeWithContentDescription("Beta").assertDoesNotExist()
    }

    @Test
    fun emptyEditorialOffersRealPathToAllApps() {
        launchMonolith(apps = emptyList())
        composeRule.onNodeWithText("EDITORIAL →").performClick()
        composeRule.onNodeWithText("Browse all apps").performClick()
        composeRule.onNodeWithText("Apps").assertIsDisplayed()
    }

    @Test
    fun emptyCatalogShowsLoading() {
        launchMonolith(apps = emptyList(), loading = true)
        composeRule.onNodeWithText("Loading applications…").assertIsDisplayed()
    }

    @Test
    fun catalogErrorOffersRetry() {
        var retried = false
        launchMonolith(apps = emptyList(), loadError = true, onRetry = { retried = true })
        composeRule.onNodeWithText("Unable to load apps. Try again").performClick()
        composeRule.waitForIdle()
        assertTrue(retried)
        composeRule.onNodeWithText(
            "App list could not refresh. Using the last loaded list. Tap to retry."
        ).assertDoesNotExist()
    }

    @Test
    fun firstLoadFailureInDrawerOffersExactlyOneRetry() {
        var retries = 0
        launchMonolith(apps = emptyList(), loadError = true, onRetry = { retries++ })
        composeRule.onNodeWithText("ALL APPS ↑").performClick()
        composeRule.onNodeWithText("Unable to load installed applications").assertExists()
        composeRule.onAllNodesWithText("Try again").assertCountEquals(1)
        composeRule.onNodeWithText(
            "App list could not refresh. Using the last loaded list. Tap to retry."
        ).assertDoesNotExist()
        composeRule.onNodeWithText("Try again").performClick()
        assertEquals(1, retries)
    }

    @Test
    fun failedRefreshWithExistingCatalogKeepsAppsVisible() {
        launchMonolith(apps = listOf(appAlpha), loadError = true)
        composeRule.onNodeWithText("ALL APPS ↑").performClick()
        composeRule.onNodeWithText("Alpha").assertExists()
        composeRule.onNodeWithText(
            "App list could not refresh. Using the last loaded list. Tap to retry."
        ).assertExists()
        composeRule.onNodeWithText("Unable to load installed applications")
            .assertDoesNotExist()
    }

    @Test
    fun sortMenuAnnouncesTheActiveChoice() {
        launchMonolith()
        composeRule.onNodeWithText("ALL APPS ↑").performClick()
        composeRule.onNodeWithText("A–Z").performClick()
        composeRule.onNode(
            SemanticsMatcher.expectValue(SemanticsProperties.Selected, true)
                and hasText("A–Z")
        ).assertExists()
    }

    @Test
    fun tappingEmptyBackgroundClosesExpandedOrbit() {
        launchMonolith()
        composeRule.onNode(
            SemanticsMatcher.expectValue(
                SemanticsProperties.StateDescription, "Orbit applications collapsed"
            )
        ).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription("Dismiss expanded orbit").performClick()
        composeRule.waitUntil(5_000) {
            runCatching {
                composeRule.onNode(
                    SemanticsMatcher.expectValue(
                        SemanticsProperties.StateDescription, "Orbit applications collapsed"
                    )
                ).assertExists()
            }.isSuccess
        }
    }

    @Test
    fun noSearchResultsHaveAccessibleClearAction() {
        launchMonolith()
        composeRule.onNodeWithText("ALL APPS ↑").performClick()
        composeRule.onNode(hasSetTextAction()).performTextInput("unlikely-zz9")
        composeRule.waitUntil(5_000) {
            runCatching {
                composeRule.onNodeWithText("No applications found").assertExists()
            }.isSuccess
        }
        composeRule.onNodeWithText("Clear search").performClick()
        composeRule.waitUntil(5_000) {
            runCatching {
                composeRule.onNodeWithText("Alpha").assertExists()
                composeRule.onNodeWithText("Beta").assertExists()
            }.isSuccess
        }
    }

    @Test
    fun compactDrawerHeaderKeepsSearchSortAndBackNavigation() {
        currentPreferences.value = LauncherPreferences(drawerColumns = 4)
        launchMonolith()
        composeRule.onNodeWithText("ALL APPS ↑").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText("Apps").assertIsDisplayed()
        // The brand remains on HOME; the drawer no longer repeats it above
        // the page title, leaving more room for the actual catalog.
        composeRule.onNodeWithText("MONOLITH").assertDoesNotExist()
        composeRule.onNode(hasSetTextAction()).assertIsDisplayed()

        composeRule.onNodeWithText("A–Z").performClick()
        composeRule.onNodeWithText("Most frequently launched").performClick()
        composeRule.waitForIdle()
        assertEquals(dev.monolith.launcher.data.AppSort.FREQUENT,
            currentPreferences.value.appSort)

        composeRule.onNodeWithContentDescription("Back to home").performClick()
        composeRule.onNodeWithText("OPEN ORBIT").assertExists()
    }

    @Test
    fun drawerListsInstalledApps() {
        launchMonolith()
        composeRule.onNodeWithText("ALL APPS ↑").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Alpha").assertIsDisplayed()
        composeRule.onNodeWithText("Beta").assertIsDisplayed()
    }

    @Test
    fun longPressAppCanPinAndUnpinWithoutOpeningSettings() {
        launchMonolith()
        composeRule.onNodeWithText("ALL APPS ↑").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Alpha").performTouchInput { longClick() }
        composeRule.onNodeWithText("Pin to Orbit").performClick()
        composeRule.waitForIdle()

        assertEquals(listOf(appAlpha.id), currentFavorites.value)
        composeRule.onNodeWithText("Remove from Orbit").performClick()
        composeRule.waitForIdle()
        assertTrue(currentFavorites.value.isEmpty())
        composeRule.onNodeWithText("Pin to Orbit").assertExists()
    }

    @Test
    fun orbitCustomizerFiltersPinnedAppsWithoutChangingSavedOrder() {
        currentFavorites.value = listOf(appBeta.id)
        launchMonolith()
        composeRule.onNodeWithContentDescription("Settings").performClick()
        composeRule.onNodeWithText("Customize orbit apps").performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Alpha").assertExists()
        composeRule.onNodeWithText("Beta").assertExists()

        composeRule.onNodeWithText("Pinned only").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Beta").assertExists()
        composeRule.onNodeWithText("Alpha").assertDoesNotExist()
        composeRule.onNodeWithText("All applications").performClick()
        composeRule.onNodeWithText("Alpha").assertExists()
        assertEquals(listOf(appBeta.id), currentFavorites.value)
    }

    @Test
    fun fullOrbitDoesNotAllowSilentExtraPin() {
        currentFavorites.value = (1..15).map { "pkg/app-$it" }
        launchMonolith()
        composeRule.onNodeWithText("ALL APPS ↑").performClick()
        composeRule.onNodeWithText("Alpha").performTouchInput { longClick() }

        composeRule.onNodeWithText("Pin to Orbit").assertIsNotEnabled()
        composeRule.onNodeWithText("Orbit is full (15/15). Remove a pin first.").assertExists()
        assertEquals(15, currentFavorites.value.size)
    }

    @Test
    fun hiddenAppsNeverAppearInDrawer() {
        launchMonolith(hiddenApps = setOf(appAlpha.id))
        composeRule.onNodeWithText("ALL APPS ↑").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Alpha").assertDoesNotExist()
        composeRule.onNodeWithText("Beta").assertIsDisplayed()
    }

    @Test
    fun changingQueryHidesEarlierCatalogWhileDebouncing() {
        assertTrue(drawerQueryIsSettling("Bet", "Al", searchEnabled = true))
        assertTrue(drawerQueryIsSettling("", "Al", searchEnabled = true))
        assertFalse(drawerQueryIsSettling("Bet", "Bet", searchEnabled = true))
        assertFalse(drawerQueryIsSettling("Bet", "Al", searchEnabled = false))

        launchMonolith()
        composeRule.onNodeWithText("ALL APPS ↑").performClick()
        composeRule.onNode(hasSetTextAction()).performTextInput("Bet")
        composeRule.onNodeWithText("Alpha").assertDoesNotExist()
        composeRule.waitUntil(5_000) {
            runCatching {
                composeRule.onNodeWithText("Beta").assertExists()
            }.isSuccess
        }
    }

    @Test
    fun searchImeLaunchesSingleSettledResultAndReturnsHome() {
        val launched = mutableListOf<LaunchableApp>()
        assertEquals(appBeta, singleDrawerImeMatch("Bet", "Bet", true, listOf(appBeta)))
        launchMonolith(onLaunch = { launched += it })
        composeRule.onNodeWithText("ALL APPS ↑").performClick()
        composeRule.onNode(hasSetTextAction()).performTextInput("Bet")
        composeRule.waitUntil(5_000) {
            runCatching { composeRule.onNodeWithText("Beta").assertIsDisplayed() }.isSuccess
        }
        composeRule.onNode(hasSetTextAction()).performImeAction()
        composeRule.waitForIdle()
        assertEquals(listOf(appBeta), launched)
        // Quick access is omitted on genuinely short HOME layouts.
        // Navigation and the central Orbit trigger must remain usable.
        composeRule.onNodeWithText("OPEN ORBIT").assertExists()
    }

    @Test
    fun searchImeNeverLaunchesStaleEmptyOrAmbiguousResults() {
        assertEquals(null, singleDrawerImeMatch("Be", "Al", true, listOf(appBeta)))
        assertEquals(null, singleDrawerImeMatch("", "", true, listOf(appBeta)))
        assertEquals(null, singleDrawerImeMatch("Be", "Be", false, listOf(appBeta)))
        assertEquals(null, singleDrawerImeMatch("a", "a", true, listOf(appAlpha, appBeta)))
        assertEquals(null, singleDrawerImeMatch("Be", "Be", true, null))

        val launched = mutableListOf<LaunchableApp>()
        launchMonolith(onLaunch = { launched += it })
        composeRule.onNodeWithText("ALL APPS ↑").performClick()
        composeRule.onNode(hasSetTextAction()).performTextInput("a")
        composeRule.waitUntil(5_000) {
            runCatching {
                composeRule.onNodeWithText("Alpha").assertIsDisplayed()
                composeRule.onNodeWithText("Beta").assertIsDisplayed()
            }.isSuccess
        }
        composeRule.onNode(hasSetTextAction()).performImeAction()
        composeRule.waitForIdle()
        assertTrue(launched.isEmpty())
        composeRule.onNodeWithText("Apps").assertIsDisplayed()
    }

    @Test
    fun drawerSearchFiltersAfterDebounce() {
        launchMonolith()
        composeRule.onNodeWithText("ALL APPS ↑").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Alpha").assertIsDisplayed()
        composeRule.onNode(hasSetTextAction()).performTextInput("zzz")
        composeRule.waitUntil(5_000) {
            runCatching {
                composeRule.onNodeWithText("No applications found").assertExists()
            }.isSuccess
        }
        composeRule.onNodeWithText("No applications found").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Clear search").performClick()
        // Synchronize the Compose effect with Robolectric; the production
        // blank-query path does not include the 180ms debounce delay.
        composeRule.waitUntil(5_000) {
            runCatching {
                composeRule.onNodeWithText("Alpha").assertExists()
            }.isSuccess
        }
        composeRule.onNodeWithText("Alpha").assertIsDisplayed()
        composeRule.onNodeWithText("Beta").assertIsDisplayed()
    }

    @Test
    fun clearingLaunchHistoryRequiresExplicitConfirmation() {
        var clearCount = 0
        launchMonolith(onClearLaunchHistory = { clearCount++ })
        composeRule.onNodeWithContentDescription("Settings").performClick()
        composeRule.onNodeWithText("Clear launch history").performScrollTo().performClick()
        composeRule.onNodeWithText("Clear launch history?").assertIsDisplayed()

        composeRule.onNodeWithText("Cancel").performClick()
        assertEquals(0, clearCount)
        composeRule.onNodeWithText("Clear launch history").performClick()
        composeRule.onNodeWithText("Clear history").performClick()
        composeRule.waitForIdle()
        assertEquals(1, clearCount)
        composeRule.onNodeWithText("Clear launch history?").assertDoesNotExist()
    }

    @Test
    fun orbitManagerSeparatesPinnedAndOtherAppsAndAllowsAccessibleReorder() {
        currentFavorites.value = listOf(appAlpha.id, appBeta.id)
        launchMonolith(apps = listOf(appAlpha, appBeta, appGamma))
        composeRule.onNodeWithContentDescription("Settings").performClick()
        composeRule.onNodeWithText("Customize orbit apps").performScrollTo().performClick()

        composeRule.onNodeWithText("Pinned (2)").assertExists()
        composeRule.onNodeWithText("Available (1)").assertExists()
        composeRule.onNodeWithContentDescription("Move Beta up").performClick()
        composeRule.waitForIdle()
        assertEquals(listOf(appBeta.id, appAlpha.id), currentFavorites.value)
        composeRule.onNodeWithContentDescription("Move Beta up").assertIsNotEnabled()
    }

    @Test
    fun orbitManagerCanClearSearchAndRestorePinnedAndAvailableLists() {
        currentFavorites.value = listOf(appBeta.id)
        launchMonolith(apps = listOf(appAlpha, appBeta))
        composeRule.onNodeWithContentDescription("Settings").performClick()
        composeRule.onNodeWithText("Customize orbit apps").performScrollTo().performClick()
        composeRule.onNode(hasSetTextAction()).performTextInput("not-an-installed-app")

        composeRule.onNodeWithText("No applications found").assertExists()
        composeRule.onNodeWithContentDescription("Clear search").performClick()
        composeRule.onNodeWithText("Pinned (1)").assertExists()
        composeRule.onNodeWithText("Available (1)").assertExists()
        assertEquals(listOf(appBeta.id), currentFavorites.value)
    }

    @Test
    fun precisionSettingsHaveSectionsAndPreserveDrawerBackOrigin() {
        launchMonolith()
        composeRule.onNodeWithText("ALL APPS ↑").performClick()
        composeRule.onNodeWithContentDescription("Settings").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText("Appearance").assertExists()
        composeRule.onNodeWithText("Orbit").assertExists()
        composeRule.onNodeWithText("App drawer").assertExists()
        composeRule.onNodeWithText("Management").assertExists()
        composeRule.onNodeWithContentDescription("Back").performClick()
        composeRule.onNodeWithText("Apps").assertIsDisplayed()
    }

    @Test
    fun settingsIconModeSelectionIsAnnouncedAndReported() {
        val received = mutableListOf<LauncherPreferences>()
        launchMonolith(onPreferencesChange = {
            received.add(it)
            currentPreferences.value = it
        })
        composeRule.onNodeWithContentDescription("Settings").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Application icon appearance").assertIsDisplayed()

        val ink = composeRule.onNodeWithText("INK")
        ink.assertExists()
        composeRule.onNode(
            SemanticsMatcher.expectValue(SemanticsProperties.Selected, false)
                and hasText("INK")
        ).assertExists()
        ink.performClick()
        composeRule.waitForIdle()
        composeRule.onNode(
            SemanticsMatcher.expectValue(SemanticsProperties.Selected, true)
                and hasText("INK")
        ).assertExists()
        assertEquals(AppIconStyle.INK, received.last().appIconStyle)
    }

    @Test
    fun denseDrawerKeepsFullLongAppNameForAccessibility() {
        val longName = "Interactive live wallpapers"
        currentPreferences.value = LauncherPreferences(drawerColumns = 5)
        launchMonolith(
            apps = listOf(LaunchableApp("pkg/long", longName, "pkg", "long"))
        )
        composeRule.onNodeWithText("ALL APPS ↑").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription(longName).assertExists()
    }

    @Test
    fun settingsChoicesExposeRadioSelectionSemantics() {
        launchMonolith()
        composeRule.onNodeWithContentDescription("Settings").performClick()
        composeRule.onNode(
            SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.RadioButton)
                and SemanticsMatcher.expectValue(SemanticsProperties.Selected, false)
                and hasText("INK")
        ).assertExists()
    }

    @Test
    fun pageIndicatorExposesLocalizedPositionState() {
        launchMonolith()
        composeRule.onNode(
            SemanticsMatcher.expectValue(
                SemanticsProperties.StateDescription, "Screen 1 of 2"
            )
        ).assertIsDisplayed()
    }
}

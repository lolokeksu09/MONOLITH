package dev.monolith.launcher.ui

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.performScrollTo
import dev.monolith.launcher.data.AppIconStyle
import dev.monolith.launcher.data.LaunchableApp
import dev.monolith.launcher.data.LauncherPreferences
import dev.monolith.launcher.data.DrawerSearchPosition
import org.junit.Assert.assertEquals
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
        onRetry: () -> Unit = {},
        onPreferencesChange: (LauncherPreferences) -> Unit =
            { currentPreferences.value = it },
        onClearLaunchHistory: () -> Unit = {}
    ) {
        composeRule.setContent {
            MonolithScreen(
                apps = apps,
                preferences = currentPreferences.value,
                homeRequestGeneration = 0,
                launchStats = emptyMap(),
                hiddenApps = hiddenApps,
                orbitFavorites = currentFavorites.value,
                loading = loading,
                loadError = loadError,
                isDefaultHome = true,
                onRetry = onRetry,
                onMakeDefaultHome = {},
                onLaunch = {},
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
    fun precisionHomeShowsTimepieceDockAndSmallOrbitHitTarget() {
        launchMonolith(apps = listOf(appAlpha, appBeta))
        composeRule.onNodeWithText("MONOLITH").assertExists()
        composeRule.onNodeWithText("QUICK ACCESS").assertExists()
        composeRule.onNodeWithText("OPEN ORBIT").assertExists()
        composeRule.onNodeWithContentDescription("Alpha").assertExists()
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
        composeRule.onNodeWithText(
            "App list could not refresh. Using the last loaded list. Tap to retry."
        ).performClick()
        composeRule.waitForIdle()
        assertTrue(retried)
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
    fun pageIndicatorExposesLocalizedPositionState() {
        launchMonolith()
        composeRule.onNode(
            SemanticsMatcher.expectValue(
                SemanticsProperties.StateDescription, "Screen 1 of 2"
            )
        ).assertIsDisplayed()
    }
}

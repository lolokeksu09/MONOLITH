package dev.monolith.launcher.data

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeBackupTest {
    private val layout = HomeLayout(
        listOf(
            HomeEntry.App("com.example.one/.Main"),
            HomeEntry.Folder(
                "folder-1", "Utilities",
                listOf("com.example.two/.Main", "com.example.three/.Main")
            ),
            HomeEntry.App("com.example.four/.Main")
        )
    )

    @Test fun roundTripRetainsFolderOrderAndGridPreferences() {
        val saved = encodeHomeBackup(layout, LauncherPreferences(5, false))
        val restored = decodeHomeBackup(saved)
        assertEquals(layout, restored.layout)
        assertEquals(LauncherPreferences(5, false), restored.preferences)
        assertFalse(saved.contains("appWidgetId"))
    }

    @Test fun rejectsUnexpectedFormatOrVersion() {
        val original = encodeHomeBackup(layout, LauncherPreferences())
        assertThrows(IllegalArgumentException::class.java) {
            decodeHomeBackup(original.replace("monolith-home", "another-app"))
        }
        val unsupported = JSONObject(original).put("version", 999).toString()
        assertThrows(IllegalArgumentException::class.java) {
            decodeHomeBackup(unsupported)
        }
    }

    @Test fun rejectsDuplicateMembershipInsteadOfSilentlyLosingApps() {
        val original = encodeHomeBackup(layout, LauncherPreferences())
        val changed = original.replace("com.example.four/.Main", "com.example.two/.Main")
        assertThrows(IllegalArgumentException::class.java) {
            decodeHomeBackup(changed)
        }
    }

    @Test fun rejectsOversizeAndInvalidJson() {
        assertThrows(IllegalArgumentException::class.java) { decodeHomeBackup("X".repeat(256 * 1024 + 1)) }
        assertThrows(Exception::class.java) { decodeHomeBackup("not json") }
    }

    @Test fun roundTripPersistsNewAppDrawerPreferences() {
        val options = LauncherPreferences(
            columns = 3, showLabels = false, drawerColumns = 5,
            showDrawerLabels = false, appSort = AppSort.FREQUENT,
            drawerIconSize = 56, showPinDots = false
        )
        val result = decodeHomeBackup(encodeHomeBackup(layout, options))
        assertEquals(options, result.preferences)
    }

    @Test fun legacyBackupsStillRestoreWithSafeDefaults() {
        val original = encodeHomeBackup(layout, LauncherPreferences())
        val json = org.json.JSONObject(original)
        listOf("drawerColumns", "drawerLabels", "appSort", "drawerIconSize", "pinDots")
            .forEach(json::remove)
        assertEquals(LauncherPreferences(), decodeHomeBackup(json.toString()).preferences)
    }

    @Test fun hiddenAppsRoundTripWithoutChangingFolderLayout() {
        val original = setOf("one/.Main", "two/.Main")
        val restored = decodeHomeBackup(encodeHomeBackup(layout, LauncherPreferences(), original))
        assertEquals(original, restored.hiddenApps)
        assertEquals(layout, restored.layout)
    }

    @Test fun legacyHomeBackupWithoutHiddenFieldIsStillCompatible() {
        val old = org.json.JSONObject(encodeHomeBackup(layout, LauncherPreferences()))
            .apply { remove("hiddenApps") }.toString()
        assertEquals(emptySet<String>(), decodeHomeBackup(old).hiddenApps)
    }

    @Test fun duplicateHiddenIdsAreRejectedBeforeImport() {
        val raw = org.json.JSONObject(encodeHomeBackup(layout, LauncherPreferences()))
            .put("hiddenApps", org.json.JSONArray().put("a/.Main").put("a/.Main"))
            .toString()
        assertThrows(IllegalArgumentException::class.java) { decodeHomeBackup(raw) }
    }

    @Test fun wallpaperAndDockSurviveExportImport() {
        val preferences = LauncherPreferences(showWallpaper = true)
        val dock = listOf("one/.Main", "two/.Main", "three/.Main")
        val backup = decodeHomeBackup(
            encodeHomeBackup(layout, preferences, hiddenApps = emptySet(), dockApps = dock)
        )
        assertEquals(dock, backup.dockApps)
        assertEquals(preferences, backup.preferences)
    }

    @Test fun oldBackupWithoutDockAndWallpaperRestoresDefaults() {
        val json = org.json.JSONObject(encodeHomeBackup(layout, LauncherPreferences()))
            .apply {
                remove("dockApps")
                remove("showWallpaper")
            }
        val backup = decodeHomeBackup(json.toString())
        assertEquals(emptyList<String>(), backup.dockApps)
        assertEquals(false, backup.preferences.showWallpaper)
    }

    @Test fun duplicateOrOverCapacityDockInBackupIsRejected() {
        val original = org.json.JSONObject(encodeHomeBackup(layout, LauncherPreferences()))
        val duplicates = org.json.JSONArray().put("a/.Main").put("a/.Main")
        val tooMany = org.json.JSONArray().apply {
            (1..6).forEach { put("app$it/.Main") }
        }
        assertThrows(IllegalArgumentException::class.java) {
            decodeHomeBackup(original.put("dockApps", duplicates).toString())
        }
        assertThrows(IllegalArgumentException::class.java) {
            decodeHomeBackup(original.put("dockApps", tooMany).toString())
        }
    }

    @Test fun drawerAppearanceSurvivesBackupRestore() {
        val settings = LauncherPreferences(
            showWallpaper = true, showDrawerSearch = false, drawerOpacity = 37
        )
        val restored = decodeHomeBackup(encodeHomeBackup(layout, settings))
        assertEquals(settings, restored.preferences)
    }

    @Test fun olderBackupWithoutDrawerAppearanceUsesSafeDefaults() {
        val document = JSONObject(encodeHomeBackup(layout, LauncherPreferences()))
            .apply {
                remove("showDrawerSearch")
                remove("drawerOpacity")
            }.toString()
        val restored = decodeHomeBackup(document)
        assertEquals(true, restored.preferences.showDrawerSearch)
        assertEquals(65, restored.preferences.drawerOpacity)
    }

    @Test fun outOfRangeDrawerOpacityIsBounded() {
        val document = JSONObject(encodeHomeBackup(layout, LauncherPreferences()))
            .put("drawerOpacity", 100000).toString()
        val restored = decodeHomeBackup(document)
        assertEquals(100, restored.preferences.drawerOpacity)
    }

    @Test fun acceptsEmptyDesktopAsValidBackup() {
        val decoded = decodeHomeBackup(encodeHomeBackup(HomeLayout(), LauncherPreferences()))
        assertTrue(decoded.layout.entries.isEmpty())
    }
}

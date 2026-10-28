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

    @Test fun acceptsEmptyDesktopAsValidBackup() {
        val decoded = decodeHomeBackup(encodeHomeBackup(HomeLayout(), LauncherPreferences()))
        assertTrue(decoded.layout.entries.isEmpty())
    }
}

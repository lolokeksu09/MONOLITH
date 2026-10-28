# MONOLITH

A native, AMOLED-first Android HOME launcher, Kotlin + Jetpack Compose. Latest candidate **0.7.0-beta**.

## Features

- Home screen with clock, pinned apps, folders, edit-mode drag reorder
- A persistent five-app Dock; add apps from their long-press actions, long-press Dock items to remove/move
- Optional system wallpaper background (supports native static and live Android wallpapers); AMOLED-black default preserved
- Swipe up on clock area or use All Apps button to open app drawer
- App search and contextual actions, adjustable home and drawer grids (3–6 columns each)
- Hide applications from All Apps using an app's long-press menu, and restore them in Settings (desktop shortcuts still work)
- App icons render on a background dispatcher using a bounded memory cache; package changes invalidate cached icons
- Temporary PackageManager failures keep the last loaded app list and offer a retry
- All Apps sorting: A–Z, Z–A, most frequently launched, recently opened, newest/oldest installation
- Independent drawer icon size (40, 48, 56 dp), label and pinned-indicator settings; saved locally
- Launch frequency counts only successful launches via MONOLITH; no Android usage permission required
- Android AppWidgetHost integration with provider selection/binding/removal
- **New:** Save your app shortcuts, folders and layout settings to a portable JSON file; import it using Android's system document picker
- Opt-in Android HOME role selection with a route back to the stock launcher
- Offline and local-first; no root, internet or broad storage permission needed

## Portable backups

From **Settings > Home screen backup**, tap Export or Import. Export includes folder memberships, their order, grid preferences, hidden applications and Dock order. Existing 0.4–0.6 backups import with an empty Dock. **Android widget IDs are not portable and are not backed up.** Import replaces the current home layout and preferences; invalid or oversized files are rejected before changes are applied.

Make a backup before any reinstall or signing-certificate migration. An Android uninstall deletes app-local data, but a previously exported JSON file can be imported on the same or another phone (apps not installed on the destination remain unavailable).

## Build and release

CI runs JVM tests, Android lint, debug assembly and a disposable-key signing smoke test on pull requests. A successful main-branch run uses a persistent private key from GitHub Actions Secrets to sign the release APK, verifies the signature **and its pinned public certificate SHA-256**, then publishes it to GitHub Releases.

Read [Signing](docs/SIGNING.md) for how to back up the private key and why old debug-signed APKs are incompatible.

## Safety

MONOLITH does **not** silently set itself as the default launcher, remove the stock launcher or require root. The original launcher can be restored from Android **Settings > Apps > Default apps > Home app**. Device smoke tests on the Lenovo Legion Y70 / Motorola ported firmware remain necessary for gestures, widgets and OEM interactions.

Release gate: the 0.7 beta must be smoke-tested on the target Lenovo Legion Y70 for system wallpaper/live wallpaper behavior, persistent Dock, widgets, wallpaper toggle, HOME role, OEM gestures, rotation and Settings backup/restore. Multi-page home, adjustable widget sizes and pinned app shortcuts beyond launcher activities are still unsupported; this is not yet a stable 1.0.

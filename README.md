# MONOLITH

A native, AMOLED-first Android HOME launcher, Kotlin + Jetpack Compose. Latest candidate **0.4.0-alpha**.

## Features

- Home screen with clock, pinned apps, folders, edit-mode drag reorder
- Swipe up on clock area or use All Apps button to open app drawer
- App search, context menus, app information, adjustable 3–6-column grid
- Android AppWidgetHost integration with provider selection/binding/removal
- **New:** Save your app shortcuts, folders and layout settings to a portable JSON file; import it using Android's system document picker
- Opt-in Android HOME role selection with a route back to the stock launcher
- Offline and local-first; no root, internet or broad storage permission needed

## Portable backups

From **Settings > Home screen backup**, tap Export or Import. Export includes folder memberships, their order, the grid column count and app-label preference. **Android widget IDs are not portable and are not backed up.** Import replaces the current home layout and preferences; invalid or oversized files are rejected before changes are applied.

Make a backup before any reinstall or signing-certificate migration. An Android uninstall deletes app-local data, but a previously exported JSON file can be imported on the same or another phone (apps not installed on the destination remain unavailable).

## Build and release

CI runs JVM tests, Android lint, debug assembly and a disposable-key signing smoke test on pull requests. A successful main-branch run uses a persistent private key from GitHub Actions Secrets to sign the release APK, verifies the signature **and its pinned public certificate SHA-256**, then publishes it to GitHub Releases.

Read [Signing](docs/SIGNING.md) for how to back up the private key and why old debug-signed APKs are incompatible.

## Safety

MONOLITH does **not** silently set itself as the default launcher, remove the stock launcher or require root. The original launcher can be restored from Android **Settings > Apps > Default apps > Home app**. Device smoke tests on the Lenovo Legion Y70 / Motorola ported firmware remain necessary for gestures, widgets and OEM interactions.

Android launcher widget configuration, auto-resizing, cross-page dragging and launcher shortcuts beyond main activities are still alpha limitations.

# MONOLITH 0.3.0-alpha

Native, local-first Android HOME launcher for Android 8–16, implemented with Kotlin and Jetpack Compose.

## 0.3 Highlights
- Persisted home folders, rename, add/move apps between home and folders, dissolve a folder without losing its apps
- An explicit edit mode: long-press and drag home tiles to reorder within the home grid (currently no cross-page auto-scroll)
- Android AppWidgetHost integration: provider picker, Android system binding consent, provider configuration, display and removal
- 0.2 pinned app migration into the versioned layout model, preserving order and identifiers
- New tests for reorder, folders, migration-related model operations and widget ID sanitization

## Building
CI runs `gradle --no-daemon :app:testDebugUnitTest :app:lintDebug :app:assembleDebug` with Gradle 8.13 / JDK 17 / SDK 36. Successful main builds automatically publish a debug prerelease to GitHub Releases.

## How to use
1. Install the debug APK; open it as a normal app before choosing it as a HOME app.
2. Pin an app from All apps using long-press > Add to home.
3. Long-press a pinned app > Create folder; then long-press another app > Move to folder.
4. Tap a folder to rename, open apps or dissolve it. Dissolving returns all apps to the home grid.
5. Tap Edit layout, then long-press and drag a home tile to rearrange it. Tap Done.
6. Tap Add widget, select a compatible widget, authorize binding when Android asks and finish any provider configuration.
7. Remove a widget with its Remove widget control.

## Safety and compatibility
- Does not require root, internet, accessibility, all-packages query or device admin privileges.
- Does not automatically change the default HOME role.
- Keeps the original launcher installed: Settings > Apps > Default apps > Home app can restore it.
- DEBUG signing may vary across GitHub Actions runners. If Android refuses an in-place update due to certificate mismatch, installing may require uninstalling MONOLITH and losing its app-private settings; do not uninstall without that consideration.
- Physical testing on Lenovo Legion Y70 / Motorola Android 16 is pending. Widget provider support and manufacturer behavior vary.
- Real desktop editing is limited to visible tiles; no multi-page workspace, wallpaper engine, folders drag-to-merge or widget resize yet.
- No production-ready claims until device smoke tests and user acceptance.

## Components
- `data/HomeLayoutStore.kt`: versioned home model and migration from 0.2 pinned IDs
- `data/WidgetIdsStore.kt`: persisted widget host ID list
- `MainActivity.kt`: widget binding/configuration and lifecycle, state ownership
- `ui/MonolithScreen.kt`: home edit gestures, folders, app drawer and widgets
- `docs/ROADMAP.md`: upcoming work

# MONOLITH

An experimental, native, local-first Android home launcher.

## Status

**0.1.0-alpha / foundation.** This repository contains a HOME-capable Android application, app drawer, local pinned-app list, basic monochrome UI and a CI build workflow. It is not yet a production-ready daily driver.

## Requirements

- Android Studio and Android SDK 36
- JDK 17
- Gradle 8.13 (or import the project through Android Studio)
- Android 8.0+ (API 26); primary validation target: Android 16

## Build

```sh
gradle :app:assembleDebug :app:testDebugUnitTest
```

The GitHub Actions workflow builds a debug APK on pushes and pull requests. Get the APK from the workflow run's **MONOLITH-debug-apk** artifact after a successful run. There is no published release APK yet.

## Activate and revert

1. Install the debug APK.
2. Open MONOLITH normally and check the app drawer.
3. Open Android **Settings > Apps > Default apps > Home app** and select MONOLITH (names vary by device).
4. To revert, return to **Home app** and choose your original launcher. Do not uninstall the original launcher.

MONOLITH doesn't request root, device admin, internet, usage access, accessibility access or the broad QUERY_ALL_PACKAGES permission.

## Architecture

- `data/AppCatalog.kt`: enumerates LAUNCHER activities via PackageManager and starts explicitly selected activities.
- `data/PinnedAppsStore.kt`: minimal local persistence for pinned activity keys.
- `ui/MonolithScreen.kt`: Compose HOME and ALL APPS views.
- `MainActivity.kt`: lifecycle boundary, state owner, launcher action handling.

The initial iteration intentionally avoids a complicated database, remote services and privilege-sensitive device integrations. Planned widgets, layouts, DataStore migration and spaces are tracked in [ROADMAP](docs/ROADMAP.md).

## Privacy

No analytics, networking, hidden background processes, or remote account dependency. Pinned activity identifiers are saved in app-private SharedPreferences.

## Repository rename

The project is called MONOLITH. The GitHub repository may still use the legacy name `NOX-LEGION` until the owner renames it in **Settings > General > Repository name**.

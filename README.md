# MONOLITH

Native, offline-first Android HOME launcher built with Kotlin and Jetpack Compose.

**Status:** `0.11.0-beta` (not a stable 1.0 release). **Android:** API 26–36. **Application ID:** `dev.monolith.launcher`.

## What works today

- **Reference HOME mode (0.11 beta):** minimal black AMOLED page, touch/hold app orbit, left-swipe editorial page with original cliff artwork, quick app links, music transport keys and full app drawer. Classic home remains available in Settings.
- HOME role selection with an explicit return path to the stock launcher.
- Desktop clock, pinned apps, reorderable folders and a persistent five-app Dock.
- All Apps drawer with search, hiding/restoring apps, adjustable grid and icon sizes, and sorting by name, install date or **launches made through MONOLITH**.
- Optional native Android wallpaper (static or live where supported); AMOLED-black desktop by default.
- Android AppWidgetHost picker/binding, persistent widget order, 128–512 dp widget-height steps and confirmation before removal.
- Return to the desktop when opening an app from the drawer, Dock, folder or home grid.
- Portable JSON backup/import through Android's document picker; English and Russian resources.
- No root, internet permission, broad storage permission or mandatory external account.

## Installation and compatibility

Use a **permanently signed beta APK** from [Releases](https://github.com/lolokeksu09/MONOLITH/releases) for in-place updates. CI debug APKs and disposable-key signing builds are **not** update-compatible with signed releases.

Set MONOLITH as the HOME app using its prompt or Android **Settings → Apps → Default apps → Home app**. Keep the stock launcher installed.

Before reinstalling, export the layout from **Settings → Home screen backup**. Import replaces current shortcuts, folders and supported preferences. Backups do **not** carry Android widget IDs, widget dimensions, launch history or app binaries. Existing 0.4–0.9 JSON backups remain compatible; old backups without Dock entries restore an empty Dock.

## Development

Requirements: JDK 17, Android SDK platform 36, Gradle 8.13. The repository currently uses the Gradle version provisioned by CI rather than a checked-in Gradle Wrapper.

```sh
gradle --no-daemon :app:testDebugUnitTest :app:lintDebug :app:assembleDebug
```

Public prerelease version: `monolithVersionName` in `gradle.properties`. CI runs tests, lint, a debug build and a disposable-key release-signing check for pull requests. Only `main` may publish permanently signed prereleases; the signing certificate fingerprint is pinned. See [signing instructions](docs/SIGNING.md).

## Current limitations

- Reference mode has two fixed visual pages; a freely editable multi-page home desktop and theme editor are not implemented yet.
- No system lock-screen replacement, recents/QuickSwitch replacement or guarantee of OEM gesture parity.
- Widgets cannot be portably recreated from JSON backups.
- Android 16 Motorola/Lenovo gesture, keyboard, live-wallpaper and widget interactions still require a physical-device acceptance pass. A green CI build alone is **not** device certification.

See [Roadmap](docs/ROADMAP.md), [Repository audit](docs/AUDIT_2026-11-05.md) and [Device acceptance checklist](docs/DEVICE_ACCEPTANCE.md).

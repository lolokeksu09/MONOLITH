# MONOLITH

A native, local-first alternative Android home launcher.

**Current development version: 0.2.0-alpha.** Kotlin, Jetpack Compose, Android 8.0+ (API 26), target Android 16 (API 36). No root, internet or account required.

## Features

- Home screen with clock, date and pinned applications
- All-apps drawer, text search, and swipe-up home gesture
- Long-press app menu: pin/unpin, reorder home icons, system app info
- Settings: 3–6 grid columns, show or hide app labels
- Local persistence of pinned applications and preferences
- Opt-in Android system HOME selection with fallback to default-app settings
- App-list refresh on returning home or Android package changes
- AMOLED dark UI, Russian/English strings, retryable errors

## Build and validation

CI executes `gradle --no-daemon :app:testDebugUnitTest :app:lintDebug :app:assembleDebug` with JDK 17, Android SDK 36 and Gradle 8.13. A successful push to main publishes a **prerelease** under GitHub Releases containing the **debug-signed APK**. This is an alpha testing APK, not production signed.

## Installation and safety

1. Install an APK from a successful Actions workflow or the alpha prerelease.
2. Launch MONOLITH as an ordinary app and check the drawer/search and pinned apps.
3. Only then optionally tap **Set as default home**, which opens Android's official system selection dialog.
4. To return to the original launcher, open **Settings > Apps > Default apps > Home app** and select the stock launcher. Keep the stock launcher installed.

MONOLITH does not replace SystemUI, recent-apps handling or vendor navigation animations. Widgets, drag/drop and advanced home layouts are future work. Do not use this alpha as your only available launcher.

## Source layout

- `MainActivity.kt`: HOME role, app refresh lifecycle, local state
- `data/AppCatalog.kt`: discover and launch visible application activities
- `data/PinnedAppsStore.kt`: ordered pinned activity IDs
- `data/LauncherSettingsStore.kt`: durable local view preferences
- `ui/MonolithScreen.kt`: Compose home, drawer, settings and app action sheet
- `docs/ROADMAP.md`: planned next milestones

## Privacy

No analytics, remote services, internet permission, usage access or accessibility permission. Local preferences are stored in app-private SharedPreferences.

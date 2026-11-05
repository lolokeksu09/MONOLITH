# MONOLITH

Native, offline-first AMOLED Android HOME launcher written in Kotlin + Jetpack Compose.

**Current prerelease: 0.13.0-beta** · Android API 26–36 · Application ID: `dev.monolith.launcher`.

## Single Reference HOME

**0.13 visual redesign:** true-black minimal screen, an animated staggered 15-app orbit, 48dp hit targets, typographic rails, motion-aware scene transitions, original bundled monochrome rock wallpaper, and grayscale shortcuts on the editorial scene. Customize and reorder your orbit shortcuts from Settings; no root or network connection required.

MONOLITH now has one design, based on the supplied reference video:

- Minimal AMOLED-black landing page with clock and Android media play/pause and next-track controls.
- Tap or hold the centre to reveal an animated cluster of real installed applications.
- Swipe left to an editorial page with original monochrome mountain drawing, weekday typography and quick-launch icons.
- Open All Apps from the bottom of either page; search, sorting, hidden-app management and package-update refresh still work.
- Settings and the system HOME role prompt remain available; no root, internet or broad storage permission is needed.
- Shortcuts use your explicitly selected orbit favorites first, then the most frequently launched apps through MONOLITH; on first install they fall back to alphabetical ordering.

**The Classic HOME, legacy Dock, pinned desktop grid, folders, Android widget host and old home backup UI are removed from 0.12.** There is no switch back to Classic.

## Upgrade safety

Install the same-certificate signed APK from [GitHub Releases](https://github.com/lolokeksu09/MONOLITH/releases). Keep the original Android launcher installed, and do **not** uninstall MONOLITH to upgrade.

The update **does not erase** the previous on-device SharedPreferences or widget data; however, the 0.12 interface cannot edit or display Classic folders, Dock or widgets, and it cannot import/export old home-layout backups. **Export a home-layout JSON backup in MONOLITH 0.11 before upgrading** if you want a separate record of that old layout. Downgrading to older APKs is not guaranteed and normally requires special care because Android disallows decreases in `versionCode`.

Application sort and drawer search/grid choices are read from the same keys used by 0.11, and hidden apps and launch counts stay on-device.

## Build

CI (JDK 17, Gradle 8.13, Android SDK 36) executes unit tests, Android Lint, debug APK and a disposable-key release signing smoke test:

```sh
gradle --no-daemon :app:testDebugUnitTest :app:lintDebug :app:assembleDebug
```

Main-branch releases use the original private signing key from GitHub Actions Secrets and verify the pinned public certificate SHA-256. No private key is included in the repository. See [Signing](docs/SIGNING.md).

## Status

This is still **beta**. Real Motorola Android 16 validation is required for HOME, gestures, app launch, insets, media controls, rotation and update compatibility. See [device acceptance](docs/DEVICE_ACCEPTANCE.md) and [0.12 removal details](docs/REMOVAL_0.12.md).

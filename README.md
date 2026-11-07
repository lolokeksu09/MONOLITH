# MONOLITH

Native, offline-first AMOLED Android HOME launcher written in Kotlin + Jetpack Compose.

**Development version in PR #35: 0.16.0-beta** · Android API 26–36 · Application ID: `dev.monolith.launcher`. The latest published release remains 0.15 until a separately authorized 0.16 merge and release succeed.

## Single Reference HOME

**0.16 / INK candidate:** adds Original / Mono / INK application-icon appearances, density-aware icon decoding and persisted 8 / 12 / 15 orbit densities. Reducing density does not delete favorites. The editorial scene keeps its rock and one localized oversized day abbreviation; All Apps gains a compact editorial header, labeled search, coherent control symbols and two-line app names. Media controls identify system media keys and unknown playback status without fake metadata or new permissions. These changes are implemented in PR #35, but CI and physical acceptance are still separate gates.

**0.15 refinement:** keeps the true-black two-scene design and configurable 15-app orbit while making motion more coherent. Orbit opening and closing share one gentle spring, supporting elements crossfade, scene transitions use smaller travel, and orbit icons warm up before the first tap. Touch targets stay disabled until moving icons are separated. The page indicator crossfades, action hints meet normal-text contrast on black, accessibility labels are localized, and the clock updates at minute boundaries.

MONOLITH has one design, based on the supplied reference video:

- Minimal AMOLED-black landing page with clock and Android media play/pause and next-track controls.
- Tap or hold the centre to reveal an animated cluster of real installed applications.
- Swipe left to an editorial page with original monochrome mountain drawing, localized weekday typography and quick-launch icons.
- Open All Apps from the bottom of either page; search, sorting, hidden-app management and package-update refresh still work.
- Settings and the system HOME role prompt remain available; no root, internet or broad storage permission is needed.
- Shortcuts use explicitly selected orbit favorites first, then the most frequently launched apps through MONOLITH; first install falls back to alphabetical ordering.

**The Classic HOME, legacy Dock, pinned desktop grid, folders, Android widget host and old home backup UI are removed from 0.12.** There is no switch back to Classic.

The authoritative scope and verification states are in [the 0.16 requirement registry](docs/PLAN_0.16.md). Feature-branch code is not a claim that 0.16 is merged, released or device-accepted.

## Upgrade safety

Install the same-certificate signed APK from [GitHub Releases](https://github.com/lolokeksu09/MONOLITH/releases). Keep the original Android launcher installed, and do **not** uninstall MONOLITH to upgrade.

The update **does not erase** previous on-device SharedPreferences or widget data; however, the 0.12+ interface cannot edit or display Classic folders, Dock or widgets, and it cannot import/export old home-layout backups. **Export a home-layout JSON backup in MONOLITH 0.11 before upgrading** if you want a separate record of that old layout. Downgrading is not guaranteed and normally requires special care because Android disallows decreases in `versionCode`.

Application sort, drawer search/grid, icon appearance and orbit density use the existing `monolith_settings` file. Hidden apps, launch counts and orbit favorites retain their existing storage. New appearance/density keys default to Original / 15, so an upgrade does not silently change the current visual choices.

## Build

CI (JDK 17, Gradle 8.13, Android SDK 36) executes unit tests, Android Lint, debug APK and a disposable-key release signing smoke test:

```sh
gradle --no-daemon :app:testDebugUnitTest :app:lintDebug :app:assembleDebug
```

Main-branch releases use the original private signing key from GitHub Actions Secrets and verify the pinned public certificate SHA-256. No private key is included in the repository. See [Signing](docs/SIGNING.md).

## Status

This is still **beta**. Real Motorola Android 16 validation is required for HOME, gestures, app launch, insets, media controls, rotation, update compatibility, every icon appearance and every orbit density. See [device acceptance](docs/DEVICE_ACCEPTANCE.md) and [0.12 removal details](docs/REMOVAL_0.12.md).

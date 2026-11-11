# MONOLITH

Native, offline-first AMOLED Android HOME launcher written in Kotlin + Jetpack Compose.

**Latest verified signed prerelease: [0.19.0-beta.414](https://github.com/lolokeksu09/MONOLITH/releases/tag/v0.19.0-beta.414)** (published 2026-11-11). Android API 26–36 · production application ID: `dev.monolith.launcher` · source commit `cf4086c`. [Android CI #414](https://github.com/lolokeksu09/MONOLITH/actions/runs/43075481033) passed; pinned production signing verified. Physical Lenovo Legion Y70 acceptance remains open. The debug-only `dev.monolith.launcher.preview` installs separately and cannot update production MONOLITH.

## Single Reference HOME

**0.20 development candidate (not released):** drawer app filtering and locale-aware sorting now run in a cancellable background calculation, with request-identity guards so hidden or stale app results are not shown while processing. Search has an explicit empty-results clear action, and a tap on free space closes an *expanded* Orbit without making the closed HOME a giant invisible button. See [0.20 implementation and acceptance plan](docs/PLAN_0.20.md). Signed 0.19 remains the current installable beta until a separate 0.20 release is verified.


**0.19 / Interaction-first (merged and signed beta):** the central Orbit trigger is a real 48dp target instead of a 304dp invisible button; app shortcuts remain interactive, including one centered app. Drawer search can optionally move to the bottom for one-handed use, with the existing Top default and stored settings intact. Navigation returns through nested Settings, Editorial recovers from an empty shortcut list, and keyboard/semantic behavior is more predictable. [PR #41](https://github.com/lolokeksu09/MONOLITH/pull/41) merged, with targeted Compose/Robolectric tests. See [0.19 implementation and device acceptance criteria](docs/PLAN_0.19.md). Device gestures, TalkBack, speed and visual fidelity have **not** been accepted.


**0.18 / Reference polish (merged and signed prerelease):** removes ornamental numbered scene headers, tightens the Minimal media hierarchy, refines Editorial rock contrast, uses true-black launcher surfaces and centers partially populated Orbit arrangements (1–15 apps). It also incorporates the 0.17 search-debounce, immediate-clear, localized page-state and Compose UI-test changes. [PR #39](https://github.com/lolokeksu09/MONOLITH/pull/39) is merged; [CI run #400](https://github.com/lolokeksu09/MONOLITH/actions/runs/43060986709) completed successfully. Physical gestures, motion and visual fidelity have **not** been device-accepted.

**0.16 / INK (merged):** adds Original / Mono / INK application-icon appearances, density-aware icon decoding and persisted 8 / 12 / 15 orbit densities. Reducing density does not delete favorites. The editorial scene keeps its rock and one localized oversized day abbreviation; All Apps gains a compact editorial header, labeled search, coherent control symbols and two-line app names. Media controls identify system media keys and unknown playback status without fake metadata or new permissions. These changes were merged in PR #35 after successful exact-head PR CI. Physical-device acceptance remains open.

**0.15 refinement:** keeps the true-black two-scene design and configurable 15-app orbit while making motion more coherent. Orbit opening and closing share one gentle spring, supporting elements crossfade, scene transitions use smaller travel, and orbit icons warm up before the first tap. Touch targets stay disabled until moving icons are separated. The page indicator crossfades, action hints meet normal-text contrast on black, accessibility labels are localized, and the clock updates at minute boundaries.

MONOLITH has one design, based on the supplied reference video:

- Minimal AMOLED-black landing page with clock and Android media play/pause and next-track controls.
- Tap or hold the centre to reveal an animated cluster of real installed applications.
- Swipe left to an editorial page with original monochrome mountain drawing, localized weekday typography and quick-launch icons.
- Open All Apps from the bottom of either page; search, sorting, hidden-app management and package-update refresh still work.
- Settings and the system HOME role prompt remain available; no root, internet or broad storage permission is needed.
- Shortcuts use explicitly selected orbit favorites first, then the most frequently launched apps through MONOLITH; first install falls back to alphabetical ordering.

**The Classic HOME, legacy Dock, pinned desktop grid, folders, Android widget host and old home backup UI are removed from 0.12.** There is no switch back to Classic.

The shipped 0.18 implementation and future product direction are separated in the [roadmap](docs/ROADMAP.md) and [product direction](docs/PRODUCT_DIRECTION.md). The [0.16 requirement registry](docs/PLAN_0.16.md) remains a historical scope record, not the latest release status. The [visual system](docs/DESIGN.md) defines design constraints; physical acceptance is tracked separately.

## Upgrade safety

Install the same-certificate signed APK from [GitHub Releases](https://github.com/lolokeksu09/MONOLITH/releases). Keep the original Android launcher installed, and do **not** uninstall MONOLITH to upgrade.

The update **does not erase** previous on-device SharedPreferences or widget data; however, the 0.12+ interface cannot edit or display Classic folders, Dock or widgets, and it cannot import/export old home-layout backups. **Export a home-layout JSON backup in MONOLITH 0.11 before upgrading** if you want a separate record of that old layout. Downgrading is not guaranteed and normally requires special care because Android disallows decreases in `versionCode`.

Application sort, drawer search/grid, icon appearance and orbit density use the existing `monolith_settings` file. Hidden apps, launch counts and orbit favorites retain their existing storage. New appearance/density keys default to Original / 15, so an upgrade does not silently change the current visual choices.

## Build

CI (JDK 17, Gradle 8.13, Android SDK 36) executes unit tests, Android Lint, debug APK and a disposable-key release signing smoke test:

```sh
gradle --no-daemon :app:testDebugUnitTest :app:lintDebug :app:assembleDebug
```

Manually dispatched main-branch releases use the original private signing key from GitHub Actions Secrets and verify the pinned public certificate SHA-256. No private key is included in the repository. See [Signing](docs/SIGNING.md).

## Status

This is still **beta**. The signed 0.18.0-beta.400 release is published, but Lenovo Legion Y70 / Motorola Android 16 testing remains open for HOME, gestures, app launch, insets, media controls, rotation, update compatibility, icon appearance and Orbit populations. See [device acceptance](docs/DEVICE_ACCEPTANCE.md), [proposed next priorities](docs/PRODUCT_DIRECTION.md) and [0.12 removal details](docs/REMOVAL_0.12.md).

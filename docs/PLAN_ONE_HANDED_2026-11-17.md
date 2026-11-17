# MONOLITH — one-handed launcher execution plan

**Date:** 2026-11-17. **Target:** native Kotlin/Jetpack Compose phone launcher on Lenovo Legion Y70.  
**Product outcome:** fewer deliberate actions to open real installed apps while retaining the Minimal / Orbit / Editorial identity.

## Invariants

- **Frozen icons:** native application artwork, existing source dimensions (All Apps 44dp in a 51dp host, Orbit 38dp with a separate 48dp target); no masks, containers or recolors. See `AGENTS.md`.
- **Persistent data:** keep stored favorites, hidden IDs, search placement, sort history, launcher usage statistics and existing keys.
- **Safety:** preserve `dev.monolith.launcher` and its permanent signing certificate, stock HOME recovery, gestures, Android Back, reduced motion, RU/EN and app actions. Preview remains a different package.
- No new permissions, trackers, network dependencies, fake media metadata or placeholder buttons.

## Incremental code patches

| ID | Implementable change | Files | Acceptance criteria | State |
|---|---|---|---|---|
| P0 | Finish handheld hierarchy, error states, icon and accessible-selection fixes | `ReferenceHome.kt`, `MonolithScreen.kt`, existing UI tests | [PR #54](https://github.com/lolokeksu09/MONOLITH/pull/54) checks pass; physical-device test separate | Implemented on draft PR #54 |
| P1 | **Search IME quick launch:** when nonblank search is settled and exactly one visible app remains, Search opens it; otherwise Search only hides keyboard | `MonolithScreen.kt`, `MonolithScreenTest.kt` | Top and Bottom use one guarded handler; positive and negative tests; successful unit tests, Lint, debug APK and native-icon guard | Implemented; Android CI passed for code commit `d061e538` (device QA pending) |
| P2 | Optional press/drag/release **from the expanded Orbit's Collapse control** (not from collapsed center) | `ReferenceHome.kt`, `OrbitLayout.kt` and gesture tests | No launch outside a 20dp target, until animation is complete, or on cancellation. Existing tap and app icon actions retained; physical device gesture review | Implemented; Android CI #44025481548 passed for `e0cb52dd` (Lenovo gesture QA still pending) |
| P3 | Safe-area responsive Minimal and Editorial composition for compact screens or large text | `ReferenceHome.kt`, Compose UI tests | Clock remains readable, optional low-value labels/quick dock yield space, Editorial shows four primary shortcuts on compact height, original icons and navigation unchanged | Implemented; Android CI #44028160917 PASS for `cddd70ec` (physical visual QA pending) |
| P4 | Give Editorial meaningful recent shortcuts from real MONOLITH-only launch history, preserving Orbit favorites and providing fallback before history exists | `ReferenceFavorites.kt`, `MonolithScreen.kt`, `ReferenceHome.kt`, data and Compose tests | Correct recent ordering, no hidden/uninstalled apps, direct one-tap launch, RU/EN accurate label, no permissions or data changes | Implemented; Android CI #44030897190 PASS for `1e1e48e0` (physical Lenovo QA pending) |
| P5 | Off-main-thread catalog + history refresh, version-gated stale history protection, and opt-in on-device evidence collection | `MainActivity.kt`, `MainActivityRefreshTest.kt`, `scripts/collect-device-acceptance.sh`, CI, `docs/DEVICE_ACCEPTANCE.md` | Unit tests + Lint + debug build + icon guard; gather warm activity timing and gfxinfo without changing HOME; physical install-over, HOME fallback, TalkBack and cold-start are separate manual gates | Automated gate PASS on `ba2656e3` (CI #44031981612); physical Lenovo / production-signing gates OPEN |

## P5 reliability / hardware evidence contract (source implemented; release gate OPEN)

**Automated evidence:** source commit `ba2656e3a70de2c1d75ab4d67a52c2f3f9093760` passed [Android CI #44031981612](https://github.com/lolokeksu09/MONOLITH/actions/runs/44031981612): unit tests (including persistent history and stale-refresh cases), Android Lint, debug APK build, native icon fidelity, shell syntax validation, package-ID inspection of the actual built Preview (`dev.monolith.launcher.preview`) and temporary-key release smoke APK (`dev.monolith.launcher`), signing verification and Preview artifact upload. This is **not evidence** of permanent production signing, physical performance, HOME role or device acceptance.


1. App catalog enumeration and `LaunchStatsStore.read()` run together on `Dispatchers.IO`; Compose/UI thread only receives the ready result. Failed/cancelled scans retain the previous catalog and statistics.
2. A successful app launch or clearing history advances a local revision. A scan started before that revision may still update the installed-app list but **cannot overwrite** the newer launch history with an older snapshot.
3. `MainActivityRefreshTest` checks real SharedPreferences record/read/clear and stale-snapshot rejection. No storage key or signing/manifest identifier changes.
4. `scripts/collect-device-acceptance.sh` records device, installed package, HOME role holder and gfxinfo frame timing; optional `--launch` captures **warm** activity start (`am start -W`) without force-stop, install or changing default launcher. These observations must be collected on the actual Lenovo before accepting P5.
5. CI inspects the **built APK manifests**: Debug Preview must be `dev.monolith.launcher.preview`, and its disposable-key release smoke counterpart must be `dev.monolith.launcher`. The production-only signed workflow still pins the permanent SHA-256 certificate.
6. **Not automatically verified:** production-certificate install-over, persisted settings after update, HOME fallback after reboot, TalkBack, cold-start metrics and user-perceived gesture smoothness. No stable release while these gates are open.
## P4 Editorial utility contract (implemented source; device QA open)

**Verified in Android CI:** source commit `1e1e48e04a8547e60eebeddaab10790135c125f2` passed [Android CI #44030897190](https://github.com/lolokeksu09/MONOLITH/actions/runs/44030897190) with unit and Compose tests, Android Lint, debug APK, source icon guard and temporary-key signature verification. The workflow published `MONOLITH-CI-debug-NOT-a-stable-signature`; it is not signed with the permanent release certificate. Lenovo device acceptance is not recorded.


1. Editorial lists real applications launched **through MONOLITH** with a positive recorded `lastOpened` timestamp, most recent first; never infer system-wide usage or request Usage Access.
2. If no valid local recent launches exist (including fresh install, cleared history or only hidden/uninstalled history), Editorial keeps the existing real quick-access fallback; it never displays an invented recent list or fake music.
3. The label changes from `QUICK ACCESS` to localized `RECENT IN MONOLITH` **only** when the visible list consists of actual local recent apps. Layout still shows up to four entries on compact screens or eight on normal screens.
4. Each entry retains original 48dp touch region, tap-to-open and long-press actions. Orbit's separately saved favorite order is unchanged; All Apps remains available through the original footer and empty-state link. No new settings, permissions or data migrations.
5. CI covers sorting, hidden/uninstalled exclusion, empty fallback and an actual Compose shortcut tap; Lenovo real-device scrolling/gesture and accessibility checks remain open.

## P3 responsive layout contract (implemented source; device QA open)

1. Compute layout using the actual safeDrawing width and height plus the platform font scale. Normal 412 × 860dp / 1.0 scale retains the approved 32dp media top, 142dp clock top, 78sp clock, 215dp shortcut bottom and eight Editorial shortcuts.
2. When height is under 700dp, width under 360dp or font scale at least 1.25, reduce clock/Editorial typography and upper padding while keeping clock and native icon artwork. Time always remains visible.
3. On a safeDrawing area shorter than 590dp, remove the **duplicated** Minimal quick-access strip (Orbit and All Apps remain reachable). On compact Editorial show the first four real shortcuts; the rest stay in the ordinary drawer and Orbit. Drop only the decorative greeting in very short or extra-large-text layouts.
4. No new user setting, persistence migration or change to HOME gesture paths. CI asserts normal and compact layout properties and a rendered short-screen HOME; physical screenshot comparison and large-text/IME acceptance remain pending.

## P2 gesture contract (implemented source; device QA open)

1. Open Orbit using the existing 48dp central button. **Tap** the existing Collapse control to close as before.
2. **Hold** Collapse, drag onto an actual Orbit app icon and release to launch; the app-name caption is only visible during target selection.
3. Release away from all icons, cancel the gesture, or finish while Orbit is still animating: **no app opens**. Selection uses the real, current icon centers and a 20dp radius, not a new icon layer.
4. Original app icon tap/long-press, page swipes, All Apps swipe-up, Back, and TalkBack controls stay available. This is not claimed as a one-gesture launch from a fully collapsed HOME.
5. Real Lenovo acceptance requires checking conflicts with the parent's drag detector, gesture cancellation, short taps, text scale, one and fifteen apps, plus accessibility. CI cannot establish these physical behaviors.

## P1 behavior contract

1. Pressing keyboard Search on **Top or Bottom** search invokes the current search callback.
2. The callback may launch only if search is enabled, the entered query is nonblank and exactly matches the debounced query, and `matchingDrawerApps` already holds exactly one current result.
3. Any blank, unsettled, loading/projection-unavailable, disabled, zero-result or multi-result state **cannot launch**. Search still hides the IME in those states.
4. Successful launch uses existing `launchAndReturnHome`, which resets HOME and clears search; no changes to the app launch service, storage or HOME registration.

## Verification evidence and next gate

- Last **published** signed prerelease: [v0.24.0-beta.463](https://github.com/lolokeksu09/MONOLITH/releases/tag/v0.24.0-beta.463) from `62eb199cb43e927c1f6dcca6633a15b693244ab8`.
- Pre-P1 PR head `35849c79` passed [Android CI](https://github.com/lolokeksu09/MONOLITH/actions/runs/44015620875).
- P1 source commit `d061e538de6680c807fdd522c0dc691bcebda402` passed [Android CI #44021758030](https://github.com/lolokeksu09/MONOLITH/actions/runs/44021758030): unit tests, Android Lint, debug APK, native-icon fidelity and disposable signing all succeeded. The workflow published a debug Preview artifact; it is not a signed production update.
- P1 repository-required Gradle unit tests, Android Lint and debug APK build passed in CI against the exact source revision `d061e538`, with native-icon fidelity and disposable-key signing. Device testing remains a separate unpassed gate.
- P2 source + touch-injection test commit `e0cb52dd59b5ecf82d0fae3b32a05eddf4e7c2d1` passed [Android CI #44025481548](https://github.com/lolokeksu09/MONOLITH/actions/runs/44025481548): unit tests including gesture success/cancel and closest-target geometry, Android Lint, debug APK, native-icon fidelity and disposable-key signing. The workflow artifact is `MONOLITH-CI-debug-NOT-a-stable-signature`; this is **not** a production signing certificate.
- P3 responsive HOME source commit `cddd70ec6adddf486fa31c5ee83216ded5ead82a` passed [Android CI #44028160917](https://github.com/lolokeksu09/MONOLITH/actions/runs/44028160917): **74 unit tests, Android Lint, debug APK, native-icon fidelity, disposable-key signature verification and Preview artifact upload**. The test report includes the new compact-screen layout checks. A separate older test assumption about quick access on the short Robolectric viewport was corrected without changing P3 behavior.
- **Physical Lenovo validation NOT RUN by this patch:** keyboard layouts, TalkBack, animation feel, gesture conflicts, install-over, certificate continuity and stock HOME recovery must be observed directly and recorded in `DEVICE_ACCEPTANCE.md`.
- No PR merge, main release or production APK should be inferred from a green debug workflow.

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
| P2 | Evaluate optional one-stroke Orbit select (hold / drag / release) without breaking tap, long-press, scrolling or system gestures | `ReferenceHome.kt`, `OrbitLayout.kt` and gesture tests | Safe cancel/outside behavior, no accidental launch at 1/8/12/15 apps; physical device gesture review | **Not implemented** pending real touch evidence |
| P3 | Responsive component placement if layout fails at compact screens or large text | `ReferenceHome.kt`, screenshot comparison | No clipping with safeDrawing/IME, RU/EN and font scaling; icons stay unchanged | **Not implemented** |
| P4 | Clarify Editorial shortcuts and empty state based on real launches | `ReferenceHome.kt`, focused UI tests | Actual one-tap action; no decorative status or extra permissions | **Not implemented** |
| P5 | Shipping / performance gate | `docs/DEVICE_ACCEPTANCE.md`, CI, direct Lenovo observations | Record frame/startup measurements, settings preservation on signed update, HOME fallback and TalkBack before stable | **Not implemented** |

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
- **Physical Lenovo validation NOT RUN by this patch:** keyboard layouts, TalkBack, animation feel, gesture conflicts, install-over, certificate continuity and stock HOME recovery must be observed directly and recorded in `DEVICE_ACCEPTANCE.md`.
- No PR merge, main release or production APK should be inferred from a green debug workflow.

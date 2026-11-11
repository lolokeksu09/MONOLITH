# MONOLITH 0.19 / Interaction-first beta patch

Status: **PR #41 merged, signed beta [v0.19.0-beta.414](https://github.com/lolokeksu09/MONOLITH/releases/tag/v0.19.0-beta.414) published; installed-device acceptance remains open**.  
Base: signed beta [v0.18.0-beta.400](https://github.com/lolokeksu09/MONOLITH/releases/tag/v0.18.0-beta.400).  
Direction: [PRODUCT_DIRECTION.md](PRODUCT_DIRECTION.md) (P0/P1/P2 priorities).  
Scope: specific, testable code changes, not every suggested idea in the direction document.

**Delivery evidence:** merge commit `cf4086c3f0b4a36c0e12af28880262a094ee8ec4` on `main`; [CI run #414](https://github.com/lolokeksu09/MONOLITH/actions/runs/43075481033) completed successfully and published `MONOLITH-0.19.0-beta.414-signed.apk` after verifying the pinned production certificate. APK SHA-256: `e729700df3b59ebb04035f7019442626445867ea7a8cc460d596e0bac8cc97f7`. This does not establish physical HOME/IME/motion/TalkBack validation or install-over success on the user's phone.

## Goal and design decision

The reference clip is the visual baseline; app access and HOME reliability take precedence. Keep the established AMOLED black and real app artwork. For one-handed drawer search, **retain TOP as the default** and make BOTTOM explicitly opt-in: the document calls for evaluating reach, not unilaterally changing everyone's layout. Reuse `monolith_settings` to persist this new choice. Do not introduce another launcher mode, online services, media permissions or a new app identity.

## Implementation contract

| ID | Shipped in this candidate | Exact code path | Regression check | Physical state |
| --- | --- | --- | --- | --- |
| UX-019-01 | Orbit's clickable opening surface is 48dp, while the surrounding 304dp Box only positions shortcuts; central application at count 1 must remain tappable after expansion. | `ui/ReferenceHome.kt` | Compose semantics/touch-bound comparison and expanded single-app check; existing Orbit geometry tests. | **Pending** |
| UX-019-02 | Opening an app's long-press action sheet collapses Orbit; sheet owns Back. | `ui/ReferenceHome.kt`, `ui/MonolithScreen.kt` | Modal/Back navigation regression plus on-device actions. | **Pending** |
| UX-019-03 | Drawer search supports opt-in BOTTOM, retaining TOP for every previous installation; hidden app/sort/favorites data unchanged. | `data/LauncherSettingsStore.kt`, `ui/MonolithScreen.kt` | Parser fallback test; Compose Settings selection and filtered drawer. | **Pending** |
| UX-019-04 | Drawer search follows IME insets; Search keyboard action hides IME, and clear remains accessible in both positions. | `ui/MonolithScreen.kt` | Existing clear/debounce tests plus RU/EN physical IME checks. | **Pending** |
| UX-019-05 | Back moves from Orbit/HIDDEN settings to Settings and from Settings to its original HOME/drawer screen rather than forcing HOME. | `ui/MonolithScreen.kt` | Pure route table tests and physical gesture/back tests. | **Pending** |
| UX-019-06 | An empty Editorial shortcut list exposes a real All Apps action rather than decorative empty copy. | `ui/ReferenceHome.kt` | Compose click-through test and no-catalog/device-error tests. | **Pending** |
| UX-019-07 | App-grid icon artwork has no duplicate accessibility label next to the text label. | `ui/MonolithScreen.kt` | TalkBack single-label review on device. | **Pending** |

## Verified boundaries (not changes)

- Production `applicationId = dev.monolith.launcher`; release signing and the certificate pin are unchanged. Debug Preview continues to use `.preview`.
- No database or preference migration; one new string key is read with TOP fallback and written with the existing preference editor.
- No new permissions, network, analytics, system Usage Access or media metadata.
- No restoration of the removed Classic HOME, Dock, folders or widgets.
- Existing 8/12/15 Orbit geometry, user favourites and icon styles are preserved.
- Users continue to reach and recover the stock Android HOME using existing Android role/settings paths.

## Verification sequence

1. **Compile and regression:** exact-head GitHub Actions `:app:testDebugUnitTest :app:lintDebug :app:assembleDebug`, plus disposable CI-only signing check. Do not conflate CI with a physically installed release.
2. **On device:** install a correctly signed candidate over the prior release; observe existing settings/data and ability to restore stock HOME.
3. **Orbit:** count 1 and 8/12/15, tap outside central trigger, tap/hold center, select icons, interrupt opening/closing, long-press actions, sheet dismissal with Back and reduced motion.
4. **Drawer:** choose TOP, choose BOTTOM; restart, reboot and confirm persistence; test IME, clear, sorting, hidden apps, load error, both locales, large fonts and gesture navigation.
5. **Navigation:** HOME → Settings → Orbit customization → Back → Settings → Back → HOME; All Apps → Settings → Back → All Apps; Editorial (no shortcuts) → All Apps.
6. **Reference:** compare actual Minimal, expanded Orbit, Editorial and drawer against the supplied video with matched screenshot/recording conditions; record deviations rather than claiming a visual pass.

## Scope explicitly not resolved

- Physical hardware validation and installation are not possible from repository CI.
- No claimed reduction in frame time, faster search or fewer dropped frames without actual measurements.
- Bottom search is an **experiment available to the user**, not a decision that it is always more ergonomic.
- Further customization, alternative Editorial content, animation choreography and a stable 1.0 release require additional evidence and separate scope.

See [DEVICE_ACCEPTANCE.md](DEVICE_ACCEPTANCE.md) for the actual checkboxes.

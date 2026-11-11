# MONOLITH roadmap

Updated 2026-11-11. **Current signed beta: [v0.18.0-beta.400](https://github.com/lolokeksu09/MONOLITH/releases/tag/v0.18.0-beta.400)**, produced from main commit `cdbda9baa68946ffbba22e300e3997c8ca887866` by successful [Android CI #400](https://github.com/lolokeksu09/MONOLITH/actions/runs/43060986709). The pinned signing certificate was verified and the release asset was published. **Physical Lenovo Legion Y70 / Motorola Android 16 acceptance remains open.** See [product direction](PRODUCT_DIRECTION.md) for proposed—not shipped—improvements.

## Implemented

- Android HOME registration, app catalog, launches, sorting, hiding apps and launcher-only usage history.
- Signed beta releases with pinned certificate verification and no root/network requirement.
- Reference design: AMOLED minimal, animated orbital app cluster and editorial weekday screen.
- 0.12: removed Classic HOME, Dock, folders, widgets and old layout backup UI. Reference home is the only desktop.

## Delivered in 0.13 beta

- Rebuilt the reference-style editorial backdrop with an offline bundled rocky image and precise grayscale layout.
- Redesigned orbit motion, screen transitions, media buttons, accessibility and app shortcuts.
- Added a local favorite selector and ordering for up to 15 apps.

## Delivered in 0.14 beta

- Fixed clipped vertical rails and media text using safe measured layouts.
- Smoothed orbit and scene motion and reduced unnecessary visual weight around icons.

## Delivered in 0.15 beta

- Unified orbit opening/closing motion, synchronized supporting-element fades and softened scene transitions.
- Preloaded orbit icons and gated moving touch targets to avoid first-open pop-in and overlapping taps.
- Improved hint contrast, page-indicator motion, localized accessibility semantics and minute-aligned clock updates.
- Published `v0.15.0-beta.380` from main revision `5897868f`; CI and permanent signing succeeded. Physical-device acceptance remains open in [the checklist](DEVICE_ACCEPTANCE.md).

## Merged in 0.16 / INK (PR #35)

The authoritative scope, REQ-ID registry, decisions and evidence states live in [the 0.16 implementation plan](PLAN_0.16.md). The changes are in `main`; merge is not proof of a signed release or device acceptance:

1. Original / Mono / INK application-icon appearances with native Android 13+ monochrome layers and Original fallback.
2. Rounded launcher-control vectors and density-aware application icon decoding within a bounded cache.
3. Persisted 8 / 12 / 15 orbit densities while preserving favorites, hidden apps and safe touch targets.
4. Simplified editorial copy and a consistent All Apps header, labeled search and two-line names.
5. Honest media status without fake metadata or new permissions; existing HOME setup and stock recovery paths remain.
6. Russian/English resources and focused JVM tests, with physical accessibility and performance checks still open.

## Delivered in 0.17 scope, consolidated into 0.18 (PR #39)

- Debounced nonblank drawer queries; clearing search restores results without the input debounce, and list scroll follows the settled query.
- Added localized HOME page-state semantics, decorative-dot semantics cleanup and Robolectric Compose UI tests.
- Separate draft [PR #36](https://github.com/lolokeksu09/MONOLITH/pull/36) remains open, but its relevant improvements were incorporated into 0.18 by PR #39. Do not merge its old branch just to repeat them.

## Delivered in 0.18 beta (PR #39)

- Removed decorative `MONOLITH / 01/02/03` counters from Minimal HOME, Editorial and All Apps, while retaining Settings/navigation.
- Adjusted upper-left media presentation, made launcher-owned backgrounds true AMOLED black and refined the Editorial rock/typography hierarchy.
- Balanced Orbit arrangements for 1–15 visible apps without changing complete 8/12/15 layouts, favorites or hidden-app exclusion.
- Added independent debug-only `dev.monolith.launcher.preview` app label/package; the signed release remains the original `dev.monolith.launcher`.
- Published signed prerelease `v0.18.0-beta.400` after merging [PR #39](https://github.com/lolokeksu09/MONOLITH/pull/39) and passing [Android CI #400](https://github.com/lolokeksu09/MONOLITH/actions/runs/43060986709). Production signing certificate remained pinned.
- Separate draft [PR #38](https://github.com/lolokeksu09/MONOLITH/pull/38) remains open, but its partial-Orbit geometry changes were integrated through PR #39. Treat it as already incorporated, not additional release scope.
- **Not validated:** signed install-over on an actual device, visual fidelity, real gestures, motion performance, TalkBack and HOME recovery.

## Next work — verification first

1. **P0 device reliability:** test the exact `v0.18.0-beta.400` signed APK on Lenovo Legion Y70 / Motorola Android 16; record install-over continuity, app launch, saved data, HOME role and stock-launcher recovery. Follow [device acceptance](DEVICE_ACCEPTANCE.md).
2. **P0 visual reference acceptance:** compare Minimal, Orbit open/close, Editorial and All Apps against the supplied clip on the actual device; record screenshots/short videos and conditions before proposing further code changes.
3. **P1 interaction ergonomics:** investigate one-handed use, drawer search placement and extra Orbit actions; only change behavior if device evidence supports it.
4. **P1 motion/performance:** measure cold-start/icon readiness, interrupted animations, scrolling and reduced-motion behavior. Do not claim FPS improvements without measurements.
5. **P2 product identity:** evaluate usefulness of Editorial and evolve distinctive interaction/typography without replacing real shortcuts with decorative content.

The detailed rationale, scope boundaries and acceptance criteria are in [product direction](PRODUCT_DIRECTION.md). Proposed work above is **not implemented or guaranteed**.

## 0.19 interaction patch — implemented on candidate branch, not released

[PR #41](https://github.com/lolokeksu09/MONOLITH/pull/41) is the implementation candidate for bounded requirements in [PLAN_0.19.md](PLAN_0.19.md). These statements describe branch code, **not** physical acceptance or a signed production update:

1. Limit Orbit's tap/hold trigger to its actual 48dp center, keeping a centered app reachable and preventing a 304dp invisible touch region from swallowing interaction.
2. Add an explicit optional drawer search position (Top / Bottom), persistent without altering old defaults or saved data; keep IME Search action and safe drawing/IME insets.
3. Return Back through nested Settings or the original drawer entry screen; dismiss app-action sheets before the underlying navigation.
4. Give an empty Editorial shortcut list a real All Apps recovery action.
5. Preserve existing icon appearances, offline operation, hidden apps, favorites, permanent signing identity and original HOME fallback.

The 0.18 signed prerelease remains the published baseline until 0.19 is merged and deliberately released. CI and device checks for the new candidate must be tracked separately.

## Stable release gate

All CI checks plus physical-device behavior, update-signing continuity and a recovery path to stock HOME must pass before 1.0 stable.
